package com.example.billmanager.data.model

import com.example.billmanager.data.local.entity.HoaDonEntity


data class HoaDonThang(
    val thang: Int,
    val nam: Int,
    var dien: HoaDonEntity?,
    var nuoc: HoaDonEntity?
)

