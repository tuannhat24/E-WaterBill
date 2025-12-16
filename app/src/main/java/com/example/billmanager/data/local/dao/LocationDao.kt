package com.example.billmanager.data.local.dao

import androidx.room.*
import com.example.billmanager.data.local.entity.LocationEntity

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations")
    fun getAll(): List<LocationEntity>

    @Query("SELECT * FROM locations WHERE isSelected = 1 LIMIT 1")
    fun getSelectedLocation(): LocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: LocationEntity)

    @Update
    fun update(location: LocationEntity)

    @Delete
    fun delete(location: LocationEntity)

    // Reset tất cả về không chọn
    @Query("UPDATE locations SET isSelected = 0")
    fun unselectAll()

    // Đếm số lượng
    @Query("SELECT COUNT(*) FROM locations")
    fun count(): Int
}