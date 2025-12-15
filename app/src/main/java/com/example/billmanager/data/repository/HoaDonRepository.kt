package com.example.billmanager.data.repository

import com.example.billmanager.data.local.dao.HoaDonDao
import com.example.billmanager.data.local.entity.HoaDonEntity


class HoaDonRepository(private val dao: HoaDonDao) {

    fun getAll(): List<HoaDonEntity> = dao.getAll()

    fun insert(entity: HoaDonEntity) = dao.insert(entity)

    fun update(entity: HoaDonEntity) = dao.update(entity)

    fun delete(entity: HoaDonEntity) = dao.delete(entity)

    fun clear() = dao.clearAll()
}



