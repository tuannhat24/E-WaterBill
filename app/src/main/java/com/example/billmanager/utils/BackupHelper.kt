package com.example.billmanager.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.billmanager.data.local.database.AppDatabase
import com.example.billmanager.data.local.entity.HoaDonEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupHelper {

    // EXPORT: lọc theo User
    fun exportData(context: Context): String {
        val db = AppDatabase.getInstance(context)
        val session = UserSession(context)
        val email = session.getUserEmail() ?: return "Chưa đăng nhập!"

        // Lấy hóa đơn của User hiện tại
        val bills = try {
            db.hoaDonDao().getBillsByUser(email)
        } catch (e: Exception) {
            // Fallback nếu chưa update DAO
            db.hoaDonDao().getAll().filter { it.userEmail == email }
        }

        if (bills.isEmpty()) return "Không có dữ liệu để sao lưu."

        val gson = Gson()
        val jsonString = gson.toJson(bills)

        // Đặt tên file kèm Email để phân biệt
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val safeEmail = email.replace("@", "_at_").replace(".", "_")
        val fileName = "backup_${safeEmail}_$timeStamp.json"

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

    //IMPORT TỪ URI
    fun importFromUri(context: Context, uri: Uri): String {
        val session = UserSession(context)
        val currentUserEmail = session.getUserEmail() ?: return "Chưa đăng nhập!"

        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val jsonString = inputStream?.bufferedReader().use { it?.readText() }

            if (jsonString.isNullOrEmpty()) return "File rỗng hoặc lỗi!"

            val type = object : TypeToken<List<HoaDonEntity>>() {}.type
            val bills: List<HoaDonEntity> = Gson().fromJson(jsonString, type)

            if (bills.isEmpty()) return "Không tìm thấy hóa đơn trong file."

            val db = AppDatabase.getInstance(context)
            var count = 0

            bills.forEach { bill ->
                // QUAN TRỌNG: Gán lại chủ sở hữu là người đang đăng nhập
                // Để tránh việc import nhầm data của người khác nhưng vẫn giữ nguyên chủ cũ
                val newBill = bill.copy(
                    id = 0, // Reset ID để tạo mới
                    userEmail = currentUserEmail // Gán về chính chủ hiện tại
                )
                db.hoaDonDao().insert(newBill)
                count++
            }

            "Đã khôi phục thành công $count hóa đơn!"
        } catch (e: Exception) {
            e.printStackTrace()
            "Lỗi import: ${e.message}"
        }
    }

    //XÓA DỮ LIỆU CŨ
    fun deleteOldData(context: Context): Int {
        val db = AppDatabase.getInstance(context)
        val session = UserSession(context)
        val email = session.getUserEmail() ?: return 0

        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        // Lấy list của user
        val bills = try {
            db.hoaDonDao().getBillsByUser(email)
        } catch (e: Exception) {
            db.hoaDonDao().getAll().filter { it.userEmail == email }
        }

        var deletedCount = 0
        bills.forEach { bill ->
            // Xóa nếu cũ hơn 2 năm
            if (bill.nam < currentYear - 2) {
                db.hoaDonDao().delete(bill)
                deletedCount++
            }
        }
        return deletedCount
    }
}