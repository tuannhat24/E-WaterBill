package com.example.billmanager.utils

import com.example.billmanager.data.model.DienDetail
import com.example.billmanager.data.model.NuocDetail

// --- Object tính toán hóa đơn điện và nước ---
object BillCalculator {

    // --- Hàm tính tiền điện ---
    fun tinhTienDien(chiSoCu: Int, chiSoMoi: Int): DienDetail {
        val soKwh = chiSoMoi - chiSoCu // Số kWh tiêu thụ
        // Mỗi bậc định mức (số kWh tối đa cho từng bậc)
        val bac = intArrayOf(50, 50, 100, 100, 100, Int.MAX_VALUE)
        // Giá tiền từng bậc (đồng/kWh)
        val gia = longArrayOf(1984, 2050, 2380, 2729, 3050, 3151)

        var remain = soKwh          // Số kWh còn lại để tính
        var totalNoTax = 0L         // Tổng tiền chưa VAT
        val chiTiet = mutableListOf<String>() // Chi tiết tính từng bậc

        for (i in bac.indices) {
            if (remain <= 0) break
            val use = minOf(remain, bac[i]) // Số kWh áp dụng cho bậc này
            val tien = use * gia[i]         // Tiền của bậc
            chiTiet.add("Bậc ${i + 1}: $use kWh × ${money(gia[i])} = ${money(tien)}")
            totalNoTax += tien
            remain -= use
        }

        val vat = totalNoTax * 8 / 100   // VAT 8%
        val tongTien = totalNoTax + vat  // Tổng tiền thanh toán

        // Trả về chi tiết hóa đơn điện
        return DienDetail(
            soKwh = soKwh,
            tienTruocThue = totalNoTax,
            vat = vat,
            tongTien = tongTien,
            chiTiet = chiTiet
        )
    }

    // --- Hàm tính tiền nước ---
    fun tinhTienNuoc(chiSoCu: Int, chiSoMoi: Int): NuocDetail {
        val soM3 = chiSoMoi - chiSoCu       // Số m³ nước sử dụng
        val tienNuoc = soM3 * 6700L         // Tiền nước
        val tienDVTN = soM3 * 2010L         // Tiền dịch vụ thoát nước
        val vatNuoc = tienNuoc * 5 / 100    // VAT nước 5%
        val vatDVTN = tienDVTN * 8 / 100    // VAT dịch vụ thoát nước 8%
        val tongTien = tienNuoc + tienDVTN + vatNuoc + vatDVTN // Tổng tiền

        // Trả về chi tiết hóa đơn nước
        return NuocDetail(
            soM3 = soM3,
            tienNuoc = tienNuoc,
            tienDVTN = tienDVTN,
            vatNuoc = vatNuoc,
            vatDVTN = vatDVTN,
            tongTien = tongTien
        )
    }

    // --- Hàm định dạng tiền ---
    private fun money(v: Long) = String.format("%,d", v) + " đ"
}
