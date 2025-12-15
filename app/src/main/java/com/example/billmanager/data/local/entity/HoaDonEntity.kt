package com.example.billmanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "hoadon")
data class HoaDonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val loai: String,      // "Điện" / "Nước"
    val thang: Int,
    val nam: Int,
    val chiSoDau: Int,
    val chiSoCuoi: Int,
    val soLuong: Int,
    val tongTien: Long,
    val gio: String,
    val ngay: String,
    var trangThai: String  // "Chưa thanh toán" / "Đã thanh toán"
) : Serializable
