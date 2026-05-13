package com.example.gymmanagement.data.local

import com.example.gymmanagement.data.local.entity.Member
import com.example.gymmanagement.data.local.entity.EquipmentEntity
import com.example.gymmanagement.data.local.entity.EquipmentStatus
import com.example.gymmanagement.data.local.entity.PaymentEntity
import com.example.gymmanagement.data.local.entity.Plan
import com.example.gymmanagement.data.local.dao.MemberDao
import com.example.gymmanagement.data.local.dao.PaymentDao
import com.example.gymmanagement.data.local.model.PaymentMethod
import com.example.gymmanagement.data.local.model.PaymentStatus
import com.example.gymmanagement.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.atomic.AtomicBoolean

object DemoDataSeeder {

    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    private const val HOUR_MILLIS = 60L * 60L * 1000L
    private const val MIN_PAST_MILLIS = 30L * 60L * 1000L
    private const val TARGET_MEMBER_COUNT = 50
    private val seedStarted = AtomicBoolean(false)

    fun seedIfNeeded(database: GymDatabase) {
        if (!seedStarted.compareAndSet(false, true)) return

        CoroutineScope(Dispatchers.IO).launch {
            database.runInTransaction {
                val plans = ensurePlans(database)
                if (plans.isEmpty()) {
                    return@runInTransaction
                }

                val memberDao = database.memberDao()
                val paymentDao = database.paymentDao()
                var currentCount = memberDao.getMembersCountImmediate()
                if (currentCount < TARGET_MEMBER_COUNT) {
                    buildMemberSeedData().forEachIndexed { index, seed ->
                        if (currentCount >= TARGET_MEMBER_COUNT) {
                            return@forEachIndexed
                        }
                        if (memberDao.memberExistsByPhone(seed.phone)) {
                            return@forEachIndexed
                        }

                        insertSeededMember(
                            memberDao = memberDao,
                            paymentDao = paymentDao,
                            plans = plans,
                            seed = seed,
                            seedIndex = index
                        )
                        currentCount += 1
                    }
                }

                ensureEquipment(database)
            }
        }
    }

    private fun insertSeededMember(
        memberDao: MemberDao,
        paymentDao: PaymentDao,
        plans: List<Plan>,
        seed: MemberSeed,
        seedIndex: Int
    ) {
        val now = System.currentTimeMillis()
        val plan = plans[seed.planSlot % plans.size]
        val joinDate = daysAgoAtHour(seed.joinDaysAgo, 6 + (seedIndex % 8))
        val expiryDate = DateUtils.calculateExpiryDate(joinDate, plan.durationDays)
        val initialPaymentDate = (joinDate + (2L * HOUR_MILLIS)).coerceAtMost(now - (10L * 60L * 1000L))

        val memberId = memberDao.insertMember(
            Member(
                remoteId = seedMemberRemoteId(seed.phone),
                name = seed.name,
                phone = seed.phone,
                joinDate = joinDate,
                expiryDate = expiryDate,
                planId = plan.id,
                paymentStatus = seed.initialPaymentStatus == PaymentStatus.PAID,
                gender = seed.gender,
                dateOfBirth = birthDateToMillis(seed.birthYear, seed.birthMonth, seed.birthDay),
                source = seed.source,
                updatedAt = now,
                synced = false,
                deleted = false
            )
        ).toInt()

        paymentDao.insertPayment(
            PaymentEntity(
                remoteId = seedPaymentRemoteId(seed.phone, initialPaymentDate, false),
                memberId = memberId,
                amount = plan.price,
                paymentMethod = seed.initialPaymentMethod.name,
                paymentDate = initialPaymentDate,
                planId = plan.id,
                isRenewal = false,
                status = seed.initialPaymentStatus.name,
                updatedAt = now,
                synced = false,
                deleted = false
            )
        )

        if (seed.renewalDaysAgo != null) {
            val renewalPaymentDate = daysAgoAtHour(seed.renewalDaysAgo, 17)
                .coerceAtMost(now - (5L * 60L * 1000L))
            paymentDao.insertPayment(
                PaymentEntity(
                    remoteId = seedPaymentRemoteId(seed.phone, renewalPaymentDate, true),
                    memberId = memberId,
                    amount = plan.price,
                    paymentMethod = (seed.renewalMethod ?: seed.initialPaymentMethod).name,
                    paymentDate = renewalPaymentDate,
                    planId = plan.id,
                    isRenewal = true,
                    status = (seed.renewalStatus ?: PaymentStatus.PAID).name,
                    updatedAt = now,
                    synced = false,
                    deleted = false
                )
            )
        }
    }

    private fun ensurePlans(database: GymDatabase): List<Plan> {
        val existingPlans = database.planDao().getAllPlansImmediate()
        if (existingPlans.isNotEmpty()) {
            return existingPlans
        }

        val now = System.currentTimeMillis()

        val defaultPlans = listOf(
            Plan(remoteId = seedPlanRemoteId("Starter Monthly"), name = "Starter Monthly", durationDays = 30, price = 1499.0, updatedAt = now, synced = false, deleted = false),
            Plan(remoteId = seedPlanRemoteId("Strength Quarterly"), name = "Strength Quarterly", durationDays = 90, price = 3999.0, updatedAt = now, synced = false, deleted = false),
            Plan(remoteId = seedPlanRemoteId("Transformation Half-Yearly"), name = "Transformation Half-Yearly", durationDays = 180, price = 7299.0, updatedAt = now, synced = false, deleted = false),
            Plan(remoteId = seedPlanRemoteId("Annual Pro"), name = "Annual Pro", durationDays = 365, price = 12999.0, updatedAt = now, synced = false, deleted = false),
            Plan(remoteId = seedPlanRemoteId("Student Sprint"), name = "Student Sprint", durationDays = 60, price = 2499.0, updatedAt = now, synced = false, deleted = false),
            Plan(remoteId = seedPlanRemoteId("Weekend Flex"), name = "Weekend Flex", durationDays = 30, price = 1099.0, updatedAt = now, synced = false, deleted = false)
        )

        defaultPlans.forEach { plan ->
            database.planDao().insertPlan(plan)
        }

        return database.planDao().getAllPlansImmediate()
    }

    private fun ensureEquipment(database: GymDatabase) {
        val equipmentDao = database.equipmentDao()
        if (equipmentDao.getEquipmentCountImmediate() > 0) {
            return
        }

        val now = System.currentTimeMillis()
        val seedData = buildEquipmentSeedData(now)
        seedData.forEach { equipment ->
            equipmentDao.insertEquipment(equipment)
        }
    }

    private fun buildEquipmentSeedData(now: Long): List<EquipmentEntity> = listOf(
        EquipmentEntity(
            remoteId = seedEquipmentRemoteId("TX-99201-B"),
            name = "Skillrun Unity 7000",
            serialNumber = "TX-99201-B",
            category = "Cardio",
            status = EquipmentStatus.ACTIVE,
            purchaseDate = now - (420L * DAY_MILLIS),
            lastServiceDate = now - (14L * DAY_MILLIS),
            usageHours = 2410,
            notes = "Installed in cardio wing",
            updatedAt = now,
            synced = false,
            deleted = false
        ),
        EquipmentEntity(
            remoteId = seedEquipmentRemoteId("OR-7721-RA"),
            name = "Olympus HD Power Rack",
            serialNumber = "OR-7721-RA",
            category = "Strength",
            status = EquipmentStatus.IN_REPAIR,
            purchaseDate = now - (520L * DAY_MILLIS),
            lastServiceDate = now + (2L * DAY_MILLIS),
            usageHours = 1890,
            notes = "Cable pulley replacement in progress",
            updatedAt = now,
            synced = false,
            deleted = false
        ),
        EquipmentEntity(
            remoteId = seedEquipmentRemoteId("DB-SET-001"),
            name = "Chromium DB Set (5-50kg)",
            serialNumber = "DB-SET-001",
            category = "Strength",
            status = EquipmentStatus.ACTIVE,
            purchaseDate = now - (300L * DAY_MILLIS),
            lastServiceDate = now - (20L * DAY_MILLIS),
            usageHours = 1540,
            notes = "Monthly audit complete",
            updatedAt = now,
            synced = false,
            deleted = false
        ),
        EquipmentEntity(
            remoteId = seedEquipmentRemoteId("ABX2-3309"),
            name = "AirBike X2 Pro",
            serialNumber = "ABX2-3309",
            category = "Cardio",
            status = EquipmentStatus.MAINTENANCE_DUE,
            purchaseDate = now - (250L * DAY_MILLIS),
            lastServiceDate = now - (48L * DAY_MILLIS),
            usageHours = 1120,
            notes = "Preventive servicing scheduled",
            updatedAt = now,
            synced = false,
            deleted = false
        ),
        EquipmentEntity(
            remoteId = seedEquipmentRemoteId("FR-CBL-208"),
            name = "FlexRow Seated Cable",
            serialNumber = "FR-CBL-208",
            category = "Functional",
            status = EquipmentStatus.ACTIVE,
            purchaseDate = now - (610L * DAY_MILLIS),
            lastServiceDate = now - (11L * DAY_MILLIS),
            usageHours = 2760,
            notes = "High use during evening peak",
            updatedAt = now,
            synced = false,
            deleted = false
        )
    )

    private fun seedMemberRemoteId(phone: String): String = "seed-member-$phone"

    private fun seedPlanRemoteId(name: String): String = "seed-plan-${slugify(name)}"

    private fun seedEquipmentRemoteId(serialNumber: String): String = "seed-equipment-${slugify(serialNumber)}"

    private fun seedPaymentRemoteId(phone: String, paymentDate: Long, isRenewal: Boolean): String =
        "seed-payment-$phone-$paymentDate-${if (isRenewal) "renewal" else "initial"}"

    private fun slugify(value: String): String = value
        .trim()
        .lowercase()
        .replace(" ", "-")
        .replace("/", "-")
        .replace("_", "-")

    private fun buildMemberSeedData(): List<MemberSeed> = listOf(
        MemberSeed("Aarav Mehta", "9001001001", 0, 0, "MALE", 1994, 5, 12, "Instagram", PaymentMethod.UPI, PaymentStatus.PAID, renewalDaysAgo = 0, renewalMethod = PaymentMethod.UPI),
        MemberSeed("Priya Nair", "9001001002", 1, 1, "FEMALE", 1996, 11, 3, "Referral", PaymentMethod.CARD, PaymentStatus.PAID),
        MemberSeed("Zoya Khan", "9001001003", 2, 4, "FEMALE", 2001, 2, 18, "College Camp", PaymentMethod.CASH, PaymentStatus.PAID),
        MemberSeed("Rohan Iyer", "9001001004", 5, 5, "MALE", 1989, 9, 27, "Walk-in", PaymentMethod.CASH, PaymentStatus.PAID),
        MemberSeed("Kabir Singh", "9001001005", 7, 0, "MALE", 1998, 7, 8, "Google", PaymentMethod.CARD, PaymentStatus.PAID),
        MemberSeed("Ananya Das", "9001001006", 9, 2, "FEMALE", 1995, 12, 14, "Instagram", PaymentMethod.UPI, PaymentStatus.PAID),
        MemberSeed("Ishan Verma", "9001001007", 11, 0, "MALE", 2003, 4, 22, "Corporate", PaymentMethod.UPI, PaymentStatus.PAID),
        MemberSeed("Meera Patel", "9001001008", 13, 3, "FEMALE", 1992, 1, 30, "Referral", PaymentMethod.CARD, PaymentStatus.PAID),
        MemberSeed("Dev Malhotra", "9001001009", 15, 5, "MALE", 1987, 8, 9, "Walk-in", PaymentMethod.CASH, PaymentStatus.PAID),
        MemberSeed("Sana Shaikh", "9001001010", 18, 4, "FEMALE", 1999, 6, 16, "WhatsApp", PaymentMethod.UPI, PaymentStatus.PAID),
        MemberSeed("Neel Joshi", "9001001011", 20, 1, "MALE", 2000, 3, 11, "Google", PaymentMethod.CARD, PaymentStatus.PAID),
        MemberSeed("Kavya Reddy", "9001001012", 22, 2, "FEMALE", 1997, 10, 24, "Instagram", PaymentMethod.UPI, PaymentStatus.PAID),
        MemberSeed("Arjun Kapoor", "9001001013", 24, 0, "MALE", 1991, 2, 5, "Referral", PaymentMethod.CASH, PaymentStatus.PAID),
        MemberSeed("Diya Bansal", "9001001014", 26, 5, "FEMALE", 2002, 12, 2, "College Camp", PaymentMethod.UPI, PaymentStatus.PAID),
        MemberSeed("Vivek Rao", "9001001015", 28, 0, "MALE", 1985, 4, 19, "Corporate", PaymentMethod.CARD, PaymentStatus.PAID),
        MemberSeed("Tara Dutta", "9001001016", 30, 4, "NON_BINARY", 1993, 9, 1, "Instagram", PaymentMethod.UPI, PaymentStatus.PENDING),
        MemberSeed("Omkar Kulkarni", "9001001017", 31, 1, "MALE", 1990, 5, 28, "Walk-in", PaymentMethod.CASH, PaymentStatus.PAID, renewalDaysAgo = 1, renewalMethod = PaymentMethod.CARD),
        MemberSeed("Nisha Bose", "9001001018", 33, 0, "FEMALE", 1988, 7, 13, "Referral", PaymentMethod.CARD, PaymentStatus.PAID, renewalDaysAgo = 2, renewalMethod = PaymentMethod.UPI),
        MemberSeed("Farhan Ali", "9001001019", 34, 5, "MALE", 1996, 1, 21, "Google", PaymentMethod.UPI, PaymentStatus.PAID, renewalDaysAgo = 3, renewalMethod = PaymentMethod.UPI, renewalStatus = PaymentStatus.FAILED),
        MemberSeed("Ritu Chandra", "9001001020", 35, 5, "UNSPECIFIED", 1994, 11, 29, "WhatsApp", PaymentMethod.CASH, PaymentStatus.FAILED),

        MemberSeed("Yash Jain", "9001001021", 36, 0, "MALE", 1999, 3, 7, "Fitness Expo", PaymentMethod.CARD, PaymentStatus.PAID, renewalDaysAgo = 2, renewalMethod = PaymentMethod.CARD),
        MemberSeed("Pooja Menon", "9001001022", 37, 5, "FEMALE", 1995, 6, 3, "Local Ads", PaymentMethod.UPI, PaymentStatus.PAID, renewalDaysAgo = 4, renewalMethod = PaymentMethod.UPI, renewalStatus = PaymentStatus.PENDING),
        MemberSeed("Harsh Vora", "9001001023", 38, 0, "MALE", 1992, 10, 19, "Google", PaymentMethod.CASH, PaymentStatus.PAID),
        MemberSeed("Simran Gill", "9001001024", 39, 5, "FEMALE", 1998, 2, 11, "Instagram", PaymentMethod.CARD, PaymentStatus.PAID, renewalDaysAgo = 6),
        MemberSeed("Aditya Paul", "9001001025", 40, 4, "MALE", 1991, 12, 21, "Corporate", PaymentMethod.UPI, PaymentStatus.PAID),
        MemberSeed("Leena Roy", "9001001026", 42, 0, "FEMALE", 1989, 4, 15, "Referral", PaymentMethod.CASH, PaymentStatus.PAID, renewalDaysAgo = 5),
        MemberSeed("Karan Thakur", "9001001027", 44, 5, "MALE", 1997, 8, 2, "Walk-in", PaymentMethod.CARD, PaymentStatus.PAID),
        MemberSeed("Bhavna Sethi", "9001001028", 46, 0, "FEMALE", 1993, 9, 26, "Local Ads", PaymentMethod.UPI, PaymentStatus.PENDING),
        MemberSeed("Rahul Sen", "9001001029", 48, 5, "MALE", 1994, 1, 9, "Google", PaymentMethod.CASH, PaymentStatus.PAID, renewalDaysAgo = 7),
        MemberSeed("Mitali Ghosh", "9001001030", 50, 4, "FEMALE", 1990, 7, 17, "WhatsApp", PaymentMethod.UPI, PaymentStatus.PAID),

        MemberSeed("Nakul Arora", "9001001031", 52, 0, "MALE", 1986, 11, 6, "Referral", PaymentMethod.CARD, PaymentStatus.PAID, renewalDaysAgo = 10),
        MemberSeed("Shreya Puri", "9001001032", 54, 5, "FEMALE", 1996, 5, 1, "Instagram", PaymentMethod.UPI, PaymentStatus.PAID, renewalDaysAgo = 8, renewalStatus = PaymentStatus.FAILED),
        MemberSeed("Uday Nambiar", "9001001033", 56, 4, "MALE", 1984, 3, 13, "Corporate", PaymentMethod.CASH, PaymentStatus.PAID),
        MemberSeed("Aisha Mirza", "9001001034", 58, 5, "FEMALE", 2000, 9, 22, "College Camp", PaymentMethod.CARD, PaymentStatus.PAID),
        MemberSeed("Pranav Deshpande", "9001001035", 60, 4, "MALE", 1992, 6, 10, "Google", PaymentMethod.UPI, PaymentStatus.PAID, renewalDaysAgo = 12),
        MemberSeed("Ritika Kohli", "9001001036", 62, 4, "FEMALE", 1995, 1, 14, "Referral", PaymentMethod.CARD, PaymentStatus.PAID, renewalDaysAgo = 14),
        MemberSeed("Siddharth Kumar", "9001001037", 64, 4, "MALE", 1988, 8, 28, "Walk-in", PaymentMethod.CASH, PaymentStatus.PAID, renewalDaysAgo = 9, renewalStatus = PaymentStatus.PENDING),
        MemberSeed("Fatima Noor", "9001001038", 66, 5, "FEMALE", 1999, 10, 5, "Instagram", PaymentMethod.UPI, PaymentStatus.PAID, renewalDaysAgo = 11),
        MemberSeed("Lokesh Yadav", "9001001039", 68, 0, "MALE", 1987, 12, 30, "Corporate", PaymentMethod.CARD, PaymentStatus.PAID),
        MemberSeed("Sneha Pillai", "9001001040", 70, 0, "FEMALE", 1991, 2, 27, "WhatsApp", PaymentMethod.UPI, PaymentStatus.FAILED),

        MemberSeed("Manav Batra", "9001001041", 75, 1, "MALE", 1993, 4, 18, "Local Ads", PaymentMethod.CASH, PaymentStatus.PAID),
        MemberSeed("Ira Banerjee", "9001001042", 82, 4, "FEMALE", 1997, 7, 4, "Referral", PaymentMethod.UPI, PaymentStatus.PAID),
        MemberSeed("Gautam Khanna", "9001001043", 90, 1, "MALE", 1985, 9, 16, "Google", PaymentMethod.CARD, PaymentStatus.PAID, renewalDaysAgo = 15),
        MemberSeed("Palak Arjun", "9001001044", 96, 2, "FEMALE", 1992, 3, 8, "Fitness Expo", PaymentMethod.UPI, PaymentStatus.PAID),
        MemberSeed("Imran Qureshi", "9001001045", 105, 3, "MALE", 1984, 6, 26, "Corporate", PaymentMethod.CASH, PaymentStatus.PAID),
        MemberSeed("Heena Kaur", "9001001046", 115, 2, "FEMALE", 1990, 11, 12, "Instagram", PaymentMethod.CARD, PaymentStatus.PENDING),
        MemberSeed("Rakesh Mondal", "9001001047", 125, 3, "MALE", 1983, 5, 23, "Walk-in", PaymentMethod.UPI, PaymentStatus.PAID, renewalDaysAgo = 20),
        MemberSeed("Saloni Trivedi", "9001001048", 135, 2, "FEMALE", 1998, 1, 31, "WhatsApp", PaymentMethod.CARD, PaymentStatus.PAID),
        MemberSeed("Danish Sheikh", "9001001049", 145, 3, "MALE", 1981, 8, 11, "Google", PaymentMethod.CASH, PaymentStatus.PAID),
        MemberSeed("Mona Francis", "9001001050", 160, 3, "FEMALE", 1986, 10, 2, "Referral", PaymentMethod.UPI, PaymentStatus.PAID, renewalDaysAgo = 25)
    )

    private fun daysAgoAtHour(daysAgo: Int, hourOfDay: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis.coerceAtMost(now - MIN_PAST_MILLIS)
    }

    private fun birthDateToMillis(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private data class MemberSeed(
        val name: String,
        val phone: String,
        val joinDaysAgo: Int,
        val planSlot: Int,
        val gender: String,
        val birthYear: Int,
        val birthMonth: Int,
        val birthDay: Int,
        val source: String,
        val initialPaymentMethod: PaymentMethod,
        val initialPaymentStatus: PaymentStatus,
        val renewalDaysAgo: Int? = null,
        val renewalMethod: PaymentMethod? = null,
        val renewalStatus: PaymentStatus? = null
    )
}
