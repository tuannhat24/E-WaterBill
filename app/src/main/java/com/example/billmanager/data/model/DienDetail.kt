package com.example.billmanager.data.model

import java.io.Serializable

data class DienDetail(
    val soKwh: Int,
    val tienTruocThue: Long, // 👈 đổi tên cho rõ nghĩa
    val vat: Long,
    val tongTien: Long,
    val chiTiet: List<String>
) : Serializable
