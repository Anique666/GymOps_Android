package com.example.gymmanagement.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gymmanagement.data.local.entity.Member

@Dao
interface MemberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMember(member: Member): Long

    @Update
    fun updateMember(member: Member)

    @Delete
    fun deleteMember(member: Member)

    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): LiveData<List<Member>>

    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    fun getMemberById(memberId: Int): LiveData<Member?>

    @Query(
        "SELECT * FROM members WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC"
    )
    fun searchMembers(query: String): LiveData<List<Member>>

    @Query("SELECT COUNT(*) FROM members")
    fun getTotalMembersCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM members")
    fun getMembersCountImmediate(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM members WHERE phone = :phone LIMIT 1)")
    fun memberExistsByPhone(phone: String): Boolean

    @Query("SELECT COUNT(*) FROM members WHERE expiryDate >= :currentTime")
    fun getActiveMembersCount(currentTime: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM members WHERE expiryDate < :currentTime")
    fun getExpiredMembersCount(currentTime: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM members WHERE expiryDate >= :currentTime AND expiryDate <= :expiringThreshold")
    fun getExpiringSoonCount(currentTime: Long, expiringThreshold: Long): LiveData<Int>
}
