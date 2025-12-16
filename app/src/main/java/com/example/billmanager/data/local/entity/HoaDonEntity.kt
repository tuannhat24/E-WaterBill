package com.example.billmanager.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "hoadon")
data class HoaDonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val loai: String,         // "Điện" / "Nước"
    val thang: Int,
    val nam: Int,
    val chiSoDau: Int,
    val chiSoCuoi: Int,
    val soLuong: Int,         // soKwh hoặc soM3
    val tongTien: Long,
    val gio: String,
    val ngay: String,
    var trangThai: String,
    val ghiChu: String = "",
    val phuongThucThanhToan: String = ""
) : Parcelable
