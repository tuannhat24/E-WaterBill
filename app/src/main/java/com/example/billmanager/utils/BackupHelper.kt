package com.example.billmanager.utils

import android.content.Context
import android.os.Environment
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupHelper {

    // Xuất danh sách hóa đơn ra file JSON
    fun exportData(context: Context): String {
        val db = AppDatabase.getInstance(context)
        val bills = db.hoaDonDao().getAll()

        if (bills.isEmpty()) return "Không có dữ liệu để sao lưu."

        val gson = Gson()
        val jsonString = gson.toJson(bills)

        // Tạo tên file theo thời gian: backup_bills_20251216_1430.json
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val fileName = "backup_bills_$timeStamp.json"

        // Lưu vào thư mục Download/UtilityBillBackup
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloadDir, "UtilityBillBackup")
        if (!appDir.exists()) appDir.mkdirs()

        val file = File(appDir, fileName)

        return try {
            val writer = FileWriter(file)
            writer.write(jsonString)
            writer.close()
            "Sao lưu thành công!\nĐường dẫn: ${file.absolutePath}"
        } catch (e: Exception) {
            e.printStackTrace()
            "Lỗi sao lưu: ${e.message}"
        }
    }

    // Nhập dữ liệu từ file JSON (Demo: Đọc file mới nhất)
    fun importData(context: Context): String {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloadDir, "UtilityBillBackup")

        if (!appDir.exists() || appDir.listFiles()?.isEmpty() == true) {
            return "Không tìm thấy file backup nào."
        }

        // Lấy file mới nhất
        val latestFile = appDir.listFiles()?.maxByOrNull { it.lastModified() } ?: return "Lỗi file."

        return try {
            val reader = FileReader(latestFile)
            val type = object : TypeToken<List<HoaDonEntity>>() {}.type
            val bills: List<HoaDonEntity> = Gson().fromJson(reader, type)
            reader.close()

            // Insert vào DB
            val db = AppDatabase.getInstance(context)
            bills.forEach { db.hoaDonDao().insert(it) } // Insert đè hoặc thêm mới tùy ID

            "Khôi phục thành công ${bills.size} hóa đơn từ file ${latestFile.name}!"
        } catch (e: Exception) {
            e.printStackTrace()
            "Lỗi khôi phục: ${e.message}"
        }
    }

    // Xóa dữ liệu cũ hơn 2 năm
    fun deleteOldData(context: Context): Int {
        val db = AppDatabase.getInstance(context)
        val allBills = db.hoaDonDao().getAll()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        var deletedCount = 0
        allBills.forEach { bill ->
            if (bill.nam < currentYear - 2) {
                db.hoaDonDao().delete(bill)
                deletedCount++
            }
        }
        return deletedCount
    }
}