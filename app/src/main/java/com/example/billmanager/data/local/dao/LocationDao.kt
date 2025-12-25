package com.example.billmanager.data.local.dao

import androidx.room.*
import com.example.billmanager.data.local.entity.LocationEntity

@Dao
interface LocationDao {
    // Lấy địa điểm của User
    @Query("SELECT * FROM locations WHERE userEmail = :email")
    fun getLocationsByUser(email: String): List<LocationEntity>

    @Insert
    fun insert(location: LocationEntity)

    @Update
    fun update(location: LocationEntity)

    @Delete
    fun delete(location: LocationEntity)

    // Bỏ chọn tất cả của User này (để chọn cái mới)
    @Query("UPDATE locations SET isSelected = 0 WHERE userEmail = :email")
    fun unselectAllByUser(email: String)

    // Đếm số địa điểm của User
    @Query("SELECT COUNT(*) FROM locations WHERE userEmail = :email")
    fun countByUser(email: String): Int
}