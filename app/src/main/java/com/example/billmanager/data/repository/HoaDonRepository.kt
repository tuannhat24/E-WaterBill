package com.example.billmanager.data.repository

import com.example.billmanager.data.local.dao.HoaDonDao
import com.example.billmanager.data.local.entity.HoaDonEntity


class HoaDonRepository(private val dao: HoaDonDao) {

    fun getAll(): List<HoaDonEntity> = dao.getAll()

    fun getBillsByUser(email: String): List<HoaDonEntity> = dao.getBillsByUser(email)

    fun insert(entity: HoaDonEntity) = dao.insert(entity)

    fun update(entity: HoaDonEntity) = dao.update(entity)

    fun delete(entity: HoaDonEntity) = dao.delete(entity)

    fun getBillByMonth(loai: String, thang: Int, nam: Int) = dao.getBillByMonth(loai, thang, nam)
    fun getBillsByYear(loai: String, nam: Int) = dao.getBillsByYear(loai, nam)
    fun getLast3Bills(loai: String) = dao.getLast3Bills(loai)

    fun clear() = dao.clearAll()
}



