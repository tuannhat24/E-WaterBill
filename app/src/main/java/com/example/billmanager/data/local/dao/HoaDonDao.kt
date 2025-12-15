package com.example.billmanager.data.local.dao

import androidx.room.*
import com.example.billmanager.data.local.entity.HoaDonEntity


@Dao
interface HoaDonDao {

    @Query("SELECT * FROM hoadon ORDER BY nam DESC, thang DESC")
    fun getAll(): List<HoaDonEntity>

    @Insert
    fun insert(entity: HoaDonEntity)

    @Update
    fun update(entity: HoaDonEntity)

    @Delete
    fun delete(entity: HoaDonEntity)

    @Query("DELETE FROM hoadon")
    fun clearAll()
}


