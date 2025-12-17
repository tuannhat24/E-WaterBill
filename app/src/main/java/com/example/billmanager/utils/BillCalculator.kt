package com.example.billmanager.utils

import com.example.billmanager.data.model.DienDetail
import com.example.billmanager.data.model.NuocDetail

object BillCalculator {

    // Bảng giá điện sinh hoạt (Mặc định - có thể thay đổi sau này từ Settings)
    private val BAC_DIEN = intArrayOf(50, 50, 100, 100, 100, Int.MAX_VALUE)
    private val GIA_DIEN = longArrayOf(1806, 1866, 2167, 2729, 3050, 3151) // Giá cập nhật mới nhất (tham khảo)

    // Giá nước (Mặc định)
    private const val GIA_NUOC_SINH_HOAT = 6700L
    private const val PHI_DVTN = 2010L // 30% giá nước

    fun tinhTienDien(start: Int, end: Int): DienDetail {
        val soKwh = end - start
        if (soKwh < 0) return DienDetail(0, 0, 0, 0, emptyList())

        var remain = soKwh
        var totalNoTax = 0L
        val details = mutableListOf<String>()

        for (i in BAC_DIEN.indices) {
            if (remain <= 0) break
            val use = minOf(remain, BAC_DIEN[i])
            val cost = use * GIA_DIEN[i]
            totalNoTax += cost
            details.add("Bậc ${i + 1}: $use kWh x ${GIA_DIEN[i]}đ = ${cost}đ")
            remain -= use
        }

        val vat = (totalNoTax * 0.08).toLong() // VAT 8% hoặc 10% tùy thời điểm
        val total = totalNoTax + vat

        return DienDetail(soKwh, totalNoTax, vat, total, details)
    }

    fun tinhTienNuoc(start: Int, end: Int): NuocDetail {
        val soM3 = end - start
        if (soM3 < 0) return NuocDetail(0, 0, 0, 0, 0, 0)

        val tienNuoc = soM3 * GIA_NUOC_SINH_HOAT
        val tienDVTN = soM3 * PHI_DVTN
        val vatNuoc = (tienNuoc * 0.05).toLong() // VAT nước 5%
        val vatDVTN = (tienDVTN * 0.08).toLong() // VAT phí BVMT 8% or 10%

        val total = tienNuoc + tienDVTN + vatNuoc + vatDVTN

        return NuocDetail(soM3, tienNuoc, tienDVTN, vatNuoc, vatDVTN, total)
    }
}