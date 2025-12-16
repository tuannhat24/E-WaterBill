package com.example.billmanager.data.model

import java.io.Serializable

data class NuocDetail(
    val soM3: Int,
    val tienNuoc: Long,
    val tienDVTN: Long,
    val vatNuoc: Long,
    val vatDVTN: Long,
    val tongTien: Long
) : Serializable
