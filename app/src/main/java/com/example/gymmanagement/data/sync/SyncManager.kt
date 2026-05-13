package com.example.gymmanagement.data.sync

import android.content.Context
import com.example.gymmanagement.data.local.GymDatabase
import com.example.gymmanagement.data.local.entity.EquipmentEntity
import com.example.gymmanagement.data.local.entity.EquipmentStatus
import com.example.gymmanagement.data.local.entity.MaintenanceEntity
import com.example.gymmanagement.data.local.entity.MaintenanceStatus
import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.local.entity.PaymentEntity
import com.example.gymmanagement.data.local.entity.Plan
import com.example.gymmanagement.data.remote.model.EquipmentRemote
import com.example.gymmanagement.data.remote.model.MaintenanceRemote
import com.example.gymmanagement.data.remote.model.MemberRemote
import com.example.gymmanagement.data.remote.model.PaymentRemote
import com.example.gymmanagement.data.remote.model.PlanRemote
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SyncManager(
    private val context: Context,
    private val database: GymDatabase,
    private val providedFirestore: FirebaseFirestore? = null
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncPreferences = SyncPreferences(context)
    private val syncMutex = Mutex()

    private val _state = MutableStateFlow(SyncState())
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private var firestore: FirebaseFirestore? = null

    fun enqueueSync() {
        scope.launch {
            try {
                syncAll()
            } catch (_: Exception) {
                // Errors are exposed through SyncState.
            }
        }
    }

    suspend fun syncAll() {
        syncMutex.withLock {
            _state.value = _state.value.copy(inProgress = true, lastError = null)
            try {
                if (!ensureFirebaseReady()) {
                    _state.value = _state.value.copy(inProgress = false)
                    return
                }
                syncPlans()
                syncMembers()
                syncEquipment()
                syncMaintenance()
                syncPayments()
                _state.value = _state.value.copy(inProgress = false, lastSuccessAt = System.currentTimeMillis())
            } catch (ex: Exception) {
                _state.value = _state.value.copy(inProgress = false, lastError = ex.message ?: "Sync failed")
                throw ex
            }
        }
    }

    private fun ensureFirebaseReady(): Boolean {
        if (FirebaseApp.getApps(context).isEmpty()) {
            val app = FirebaseApp.initializeApp(context)
            if (app == null) {
                _state.value = _state.value.copy(
                    lastError = "Firebase not initialized. Check google-services.json"
                )
                return false
            }
        }
        if (firestore == null) {
            firestore = (providedFirestore ?: FirebaseFirestore.getInstance()).apply {
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
            }
        }
        return true
    }

    private suspend fun syncPlans() {
        val firestore = firestore ?: return
        val planDao = database.planDao()
        val pending = planDao.getPendingSyncPlans()
        if (pending.isNotEmpty()) {
            val syncedIds = mutableListOf<Int>()
            for (plan in pending) {
                val remoteId = ensureRemoteId(plan) { updated -> planDao.updatePlan(updated) }
                val normalized = if (plan.updatedAt == 0L) {
                    val now = System.currentTimeMillis()
                    val updated = plan.copy(updatedAt = now, synced = false)
                    planDao.updatePlan(updated)
                    updated
                } else {
                    plan
                }
                val remote = PlanRemote(
                    remoteId = remoteId,
                    name = normalized.name,
                    durationDays = normalized.durationDays,
                    price = normalized.price,
                    updatedAt = normalized.updatedAt,
                    deleted = normalized.deleted
                )
                firestore.collection(COLLECTION_PLANS)
                    .document(remoteId)
                    .set(remote, SetOptions.merge())
                    .await()
                syncedIds.add(plan.id)
            }
            planDao.markPlansSynced(syncedIds)
        }

        val lastSync = syncPreferences.getLastSync(SyncPreferences.LAST_SYNC_PLANS)
        val snapshot = firestore.collection(COLLECTION_PLANS)
            .whereGreaterThan(FIELD_UPDATED_AT, lastSync)
            .get()
            .await()

        var maxUpdatedAt = lastSync
        val toUpsert = mutableListOf<Plan>()
        for (doc in snapshot.documents) {
            val remote = doc.toObject(PlanRemote::class.java) ?: continue
            val remoteId = if (remote.remoteId.isBlank()) doc.id else remote.remoteId
            maxUpdatedAt = maxOf(maxUpdatedAt, remote.updatedAt)
            val existing = planDao.getPlanByRemoteId(remoteId)
            if (existing != null && existing.updatedAt > remote.updatedAt) {
                continue
            }
            val entity = Plan(
                id = existing?.id ?: 0,
                remoteId = remoteId,
                name = remote.name,
                durationDays = remote.durationDays,
                price = remote.price,
                updatedAt = remote.updatedAt,
                synced = true,
                deleted = remote.deleted
            )
            toUpsert.add(entity)
        }
        if (toUpsert.isNotEmpty()) {
            planDao.insertPlans(toUpsert)
        }
        syncPreferences.setLastSync(SyncPreferences.LAST_SYNC_PLANS, if (maxUpdatedAt == lastSync) System.currentTimeMillis() else maxUpdatedAt)
    }

    private suspend fun syncMembers() {
        val firestore = firestore ?: return
        val memberDao = database.memberDao()
        val planDao = database.planDao()
        val pending = memberDao.getPendingSyncMembers()
        if (pending.isNotEmpty()) {
            val syncedIds = mutableListOf<Int>()
            for (member in pending) {
                val remoteId = ensureRemoteId(member) { updated -> memberDao.updateMember(updated) }
                val planRemoteId = planDao.getPlanRemoteIdById(member.planId) ?: ""
                if (planRemoteId.isBlank()) {
                    continue
                }
                val normalized = if (member.updatedAt == 0L) {
                    val now = System.currentTimeMillis()
                    val updated = member.copy(updatedAt = now, synced = false)
                    memberDao.updateMember(updated)
                    updated
                } else {
                    member
                }
                val remote = MemberRemote(
                    remoteId = remoteId,
                    name = normalized.name,
                    phone = normalized.phone,
                    joinDate = normalized.joinDate,
                    expiryDate = normalized.expiryDate,
                    planRemoteId = planRemoteId,
                    paymentStatus = normalized.paymentStatus,
                    gender = normalized.gender,
                    dateOfBirth = normalized.dateOfBirth,
                    source = normalized.source,
                    updatedAt = normalized.updatedAt,
                    deleted = normalized.deleted
                )
                firestore.collection(COLLECTION_MEMBERS)
                    .document(remoteId)
                    .set(remote, SetOptions.merge())
                    .await()
                syncedIds.add(member.id)
            }
            memberDao.markMembersSynced(syncedIds)
        }

        val lastSync = syncPreferences.getLastSync(SyncPreferences.LAST_SYNC_MEMBERS)
        val snapshot = firestore.collection(COLLECTION_MEMBERS)
            .whereGreaterThan(FIELD_UPDATED_AT, lastSync)
            .get()
            .await()

        var maxUpdatedAt = lastSync
        val toUpsert = mutableListOf<Member>()
        for (doc in snapshot.documents) {
            val remote = doc.toObject(MemberRemote::class.java) ?: continue
            val remoteId = if (remote.remoteId.isBlank()) doc.id else remote.remoteId
            maxUpdatedAt = maxOf(maxUpdatedAt, remote.updatedAt)
            val existing = memberDao.getMemberByRemoteId(remoteId)
            if (existing != null && existing.updatedAt > remote.updatedAt) {
                continue
            }
            val planId = planDao.getPlanIdByRemoteId(remote.planRemoteId) ?: continue
            val entity = Member(
                id = existing?.id ?: 0,
                remoteId = remoteId,
                name = remote.name,
                phone = remote.phone,
                joinDate = remote.joinDate,
                expiryDate = remote.expiryDate,
                planId = planId,
                paymentStatus = remote.paymentStatus,
                gender = remote.gender,
                dateOfBirth = remote.dateOfBirth,
                source = remote.source,
                updatedAt = remote.updatedAt,
                synced = true,
                deleted = remote.deleted
            )
            toUpsert.add(entity)
        }
        if (toUpsert.isNotEmpty()) {
            memberDao.insertMembers(toUpsert)
        }
        syncPreferences.setLastSync(SyncPreferences.LAST_SYNC_MEMBERS, if (maxUpdatedAt == lastSync) System.currentTimeMillis() else maxUpdatedAt)
    }

    private suspend fun syncPayments() {
        val firestore = firestore ?: return
        val paymentDao = database.paymentDao()
        val memberDao = database.memberDao()
        val planDao = database.planDao()
        val pending = paymentDao.getPendingSyncPayments()
        if (pending.isNotEmpty()) {
            val syncedIds = mutableListOf<Int>()
            for (payment in pending) {
                val remoteId = ensureRemoteId(payment) { updated -> paymentDao.insertPayment(updated) }
                val memberRemoteId = memberDao.getMemberRemoteIdById(payment.memberId) ?: ""
                val planRemoteId = planDao.getPlanRemoteIdById(payment.planId) ?: ""
                if (memberRemoteId.isBlank() || planRemoteId.isBlank()) {
                    continue
                }
                val normalized = if (payment.updatedAt == 0L) {
                    val now = System.currentTimeMillis()
                    val updated = payment.copy(updatedAt = now, synced = false)
                    paymentDao.insertPayment(updated)
                    updated
                } else {
                    payment
                }
                val remote = PaymentRemote(
                    remoteId = remoteId,
                    memberRemoteId = memberRemoteId,
                    planRemoteId = planRemoteId,
                    amount = normalized.amount,
                    paymentMethod = normalized.paymentMethod,
                    paymentDate = normalized.paymentDate,
                    isRenewal = normalized.isRenewal,
                    status = normalized.status,
                    updatedAt = normalized.updatedAt,
                    deleted = normalized.deleted
                )
                firestore.collection(COLLECTION_PAYMENTS)
                    .document(remoteId)
                    .set(remote, SetOptions.merge())
                    .await()
                syncedIds.add(payment.id)
            }
            paymentDao.markPaymentsSynced(syncedIds)
        }

        val lastSync = syncPreferences.getLastSync(SyncPreferences.LAST_SYNC_PAYMENTS)
        val snapshot = firestore.collection(COLLECTION_PAYMENTS)
            .whereGreaterThan(FIELD_UPDATED_AT, lastSync)
            .get()
            .await()

        var maxUpdatedAt = lastSync
        val toUpsert = mutableListOf<PaymentEntity>()
        for (doc in snapshot.documents) {
            val remote = doc.toObject(PaymentRemote::class.java) ?: continue
            val remoteId = if (remote.remoteId.isBlank()) doc.id else remote.remoteId
            maxUpdatedAt = maxOf(maxUpdatedAt, remote.updatedAt)
            val existing = paymentDao.getPaymentByRemoteId(remoteId)
            if (existing != null && existing.updatedAt > remote.updatedAt) {
                continue
            }
            val memberId = memberDao.getMemberIdByRemoteId(remote.memberRemoteId) ?: continue
            val planId = planDao.getPlanIdByRemoteId(remote.planRemoteId) ?: continue
            val entity = PaymentEntity(
                id = existing?.id ?: 0,
                remoteId = remoteId,
                memberId = memberId,
                amount = remote.amount,
                paymentMethod = remote.paymentMethod,
                paymentDate = remote.paymentDate,
                planId = planId,
                isRenewal = remote.isRenewal,
                status = remote.status,
                updatedAt = remote.updatedAt,
                synced = true,
                deleted = remote.deleted
            )
            toUpsert.add(entity)
        }
        if (toUpsert.isNotEmpty()) {
            paymentDao.insertPayments(toUpsert)
        }
        syncPreferences.setLastSync(SyncPreferences.LAST_SYNC_PAYMENTS, if (maxUpdatedAt == lastSync) System.currentTimeMillis() else maxUpdatedAt)
    }

    private suspend fun syncEquipment() {
        val firestore = firestore ?: return
        val equipmentDao = database.equipmentDao()
        val pending = equipmentDao.getPendingSyncEquipment()
        if (pending.isNotEmpty()) {
            val syncedIds = mutableListOf<Int>()
            for (equipment in pending) {
                val remoteId = ensureRemoteId(equipment) { updated -> equipmentDao.updateEquipment(updated) }
                val normalized = if (equipment.updatedAt == 0L) {
                    val now = System.currentTimeMillis()
                    val updated = equipment.copy(updatedAt = now, synced = false)
                    equipmentDao.updateEquipment(updated)
                    updated
                } else {
                    equipment
                }
                val remote = EquipmentRemote(
                    remoteId = remoteId,
                    name = normalized.name,
                    serialNumber = normalized.serialNumber,
                    category = normalized.category,
                    status = normalized.status.name,
                    purchaseDate = normalized.purchaseDate,
                    lastServiceDate = normalized.lastServiceDate,
                    usageHours = normalized.usageHours,
                    notes = normalized.notes,
                    updatedAt = normalized.updatedAt,
                    deleted = normalized.deleted
                )
                firestore.collection(COLLECTION_EQUIPMENT)
                    .document(remoteId)
                    .set(remote, SetOptions.merge())
                    .await()
                syncedIds.add(equipment.id)
            }
            equipmentDao.markEquipmentSynced(syncedIds)
        }

        val lastSync = syncPreferences.getLastSync(SyncPreferences.LAST_SYNC_EQUIPMENT)
        val snapshot = firestore.collection(COLLECTION_EQUIPMENT)
            .whereGreaterThan(FIELD_UPDATED_AT, lastSync)
            .get()
            .await()

        var maxUpdatedAt = lastSync
        val toUpsert = mutableListOf<EquipmentEntity>()
        for (doc in snapshot.documents) {
            val remote = doc.toObject(EquipmentRemote::class.java) ?: continue
            val remoteId = if (remote.remoteId.isBlank()) doc.id else remote.remoteId
            maxUpdatedAt = maxOf(maxUpdatedAt, remote.updatedAt)
            val existing = equipmentDao.getEquipmentByRemoteId(remoteId)
            if (existing != null && existing.updatedAt > remote.updatedAt) {
                continue
            }
            val entity = EquipmentEntity(
                id = existing?.id ?: 0,
                remoteId = remoteId,
                name = remote.name,
                serialNumber = remote.serialNumber,
                category = remote.category,
                status = parseEquipmentStatus(remote.status),
                purchaseDate = remote.purchaseDate,
                lastServiceDate = remote.lastServiceDate,
                usageHours = remote.usageHours,
                notes = remote.notes,
                updatedAt = remote.updatedAt,
                synced = true,
                deleted = remote.deleted
            )
            toUpsert.add(entity)
        }
        if (toUpsert.isNotEmpty()) {
            equipmentDao.insertEquipments(toUpsert)
        }
        syncPreferences.setLastSync(SyncPreferences.LAST_SYNC_EQUIPMENT, if (maxUpdatedAt == lastSync) System.currentTimeMillis() else maxUpdatedAt)
    }

    private suspend fun syncMaintenance() {
        val firestore = firestore ?: return
        val maintenanceDao = database.maintenanceDao()
        val equipmentDao = database.equipmentDao()
        val pending = maintenanceDao.getPendingSyncMaintenance()
        if (pending.isNotEmpty()) {
            val syncedIds = mutableListOf<Int>()
            for (record in pending) {
                val remoteId = ensureRemoteId(record) { updated -> maintenanceDao.insertMaintenanceRecord(updated) }
                val equipmentRemoteId = equipmentDao.getEquipmentRemoteIdById(record.equipmentId) ?: ""
                if (equipmentRemoteId.isBlank()) {
                    continue
                }
                val normalized = if (record.updatedAt == 0L) {
                    val now = System.currentTimeMillis()
                    val updated = record.copy(updatedAt = now, synced = false)
                    maintenanceDao.insertMaintenanceRecord(updated)
                    updated
                } else {
                    record
                }
                val remote = MaintenanceRemote(
                    remoteId = remoteId,
                    equipmentRemoteId = equipmentRemoteId,
                    issueDescription = normalized.issueDescription,
                    reportedDate = normalized.reportedDate,
                    resolvedDate = normalized.resolvedDate,
                    status = normalized.status.name,
                    updatedAt = normalized.updatedAt,
                    deleted = normalized.deleted
                )
                firestore.collection(COLLECTION_MAINTENANCE)
                    .document(remoteId)
                    .set(remote, SetOptions.merge())
                    .await()
                syncedIds.add(record.id)
            }
            maintenanceDao.markMaintenanceSynced(syncedIds)
        }

        val lastSync = syncPreferences.getLastSync(SyncPreferences.LAST_SYNC_MAINTENANCE)
        val snapshot = firestore.collection(COLLECTION_MAINTENANCE)
            .whereGreaterThan(FIELD_UPDATED_AT, lastSync)
            .get()
            .await()

        var maxUpdatedAt = lastSync
        val toUpsert = mutableListOf<MaintenanceEntity>()
        for (doc in snapshot.documents) {
            val remote = doc.toObject(MaintenanceRemote::class.java) ?: continue
            val remoteId = if (remote.remoteId.isBlank()) doc.id else remote.remoteId
            maxUpdatedAt = maxOf(maxUpdatedAt, remote.updatedAt)
            val existing = maintenanceDao.getMaintenanceByRemoteId(remoteId)
            if (existing != null && existing.updatedAt > remote.updatedAt) {
                continue
            }
            val equipmentId = equipmentDao.getEquipmentIdByRemoteId(remote.equipmentRemoteId) ?: continue
            val entity = MaintenanceEntity(
                id = existing?.id ?: 0,
                remoteId = remoteId,
                equipmentId = equipmentId,
                issueDescription = remote.issueDescription,
                reportedDate = remote.reportedDate,
                resolvedDate = remote.resolvedDate,
                status = parseMaintenanceStatus(remote.status),
                updatedAt = remote.updatedAt,
                synced = true,
                deleted = remote.deleted
            )
            toUpsert.add(entity)
        }
        if (toUpsert.isNotEmpty()) {
            maintenanceDao.insertMaintenanceRecords(toUpsert)
        }
        syncPreferences.setLastSync(SyncPreferences.LAST_SYNC_MAINTENANCE, if (maxUpdatedAt == lastSync) System.currentTimeMillis() else maxUpdatedAt)
    }

    private fun ensureRemoteId(item: Plan, updater: (Plan) -> Unit): String {
        val now = System.currentTimeMillis()
        return ensureRemoteIdInternal(item.remoteId) { newId ->
            updater(item.copy(remoteId = newId, updatedAt = now, synced = false))
        }
    }

    private fun ensureRemoteId(item: Member, updater: (Member) -> Unit): String {
        val now = System.currentTimeMillis()
        return ensureRemoteIdInternal(item.remoteId) { newId ->
            updater(item.copy(remoteId = newId, updatedAt = now, synced = false))
        }
    }

    private fun ensureRemoteId(item: PaymentEntity, updater: (PaymentEntity) -> Unit): String {
        val now = System.currentTimeMillis()
        return ensureRemoteIdInternal(item.remoteId) { newId ->
            updater(item.copy(remoteId = newId, updatedAt = now, synced = false))
        }
    }

    private fun ensureRemoteId(item: EquipmentEntity, updater: (EquipmentEntity) -> Unit): String {
        val now = System.currentTimeMillis()
        return ensureRemoteIdInternal(item.remoteId) { newId ->
            updater(item.copy(remoteId = newId, updatedAt = now, synced = false))
        }
    }

    private fun ensureRemoteId(item: MaintenanceEntity, updater: (MaintenanceEntity) -> Unit): String {
        val now = System.currentTimeMillis()
        return ensureRemoteIdInternal(item.remoteId) { newId ->
            updater(item.copy(remoteId = newId, updatedAt = now, synced = false))
        }
    }

    private fun ensureRemoteIdInternal(remoteId: String, updater: (String) -> Unit): String {
        if (remoteId.isNotBlank()) {
            return remoteId
        }
        val newId = UUID.randomUUID().toString()
        updater(newId)
        return newId
    }

    private companion object {
        const val COLLECTION_PLANS = "plans"
        const val COLLECTION_MEMBERS = "members"
        const val COLLECTION_PAYMENTS = "payments"
        const val COLLECTION_EQUIPMENT = "equipment"
        const val COLLECTION_MAINTENANCE = "maintenance"
        const val FIELD_UPDATED_AT = "updatedAt"
    }

    private fun parseEquipmentStatus(value: String): EquipmentStatus {
        return runCatching { EquipmentStatus.valueOf(value) }.getOrDefault(EquipmentStatus.ACTIVE)
    }

    private fun parseMaintenanceStatus(value: String): MaintenanceStatus {
        return runCatching { MaintenanceStatus.valueOf(value) }.getOrDefault(MaintenanceStatus.OPEN)
    }
}
