package com.example.billmanager.data.local.dao

import androidx.room.*
import com.example.billmanager.data.local.entity.HoaDonEntity


@Dao
interface HoaDonDao {

    @Query("SELECT * FROM hoadon ORDER BY nam DESC, thang DESC")
    fun getAll(): List<HoaDonEntity>

    @Query("SELECT * FROM hoadon WHERE userEmail = :email ORDER BY nam DESC, thang DESC")
    fun getBillsByUser(email: String): List<HoaDonEntity>

    @Insert
    fun insert(entity: HoaDonEntity)

    @Update
    fun update(entity: HoaDonEntity)

    @Delete
    fun delete(entity: HoaDonEntity)

    @Query("DELETE FROM hoadon")
    fun clearAll()

    // Lấy hóa đơn theo loại và thời gian (để so sánh tháng trước)
    @Query("SELECT * FROM hoadon WHERE loai = :loai AND thang = :thang AND nam = :nam LIMIT 1")
    fun getBillByMonth(loai: String, thang: Int, nam: Int): HoaDonEntity?

    // Lấy tất cả hóa đơn của 1 loại trong năm (để vẽ biểu đồ)
    @Query("SELECT * FROM hoadon WHERE loai = :loai AND nam = :nam ORDER BY thang ASC")
    fun getBillsByYear(loai: String, nam: Int): List<HoaDonEntity>

    // Lấy 3 hóa đơn gần nhất của 1 loại (để tính trung bình)
    @Query("SELECT * FROM hoadon WHERE loai = :loai ORDER BY nam DESC, thang DESC LIMIT 3")
    fun getLast3Bills(loai: String): List<HoaDonEntity>
}


