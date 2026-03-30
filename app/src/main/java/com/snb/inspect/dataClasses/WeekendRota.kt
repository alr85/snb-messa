package com.snb.inspect.dataClasses

import com.google.gson.annotations.SerializedName

data class WeekendRotaResponse(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("engineerId")
    val engineerId: Int?,
    @SerializedName("engineerName")
    val engineerName: String?,
    @SerializedName("rotaDate")
    val rotaDate: String?,
    @SerializedName("isWeekend")
    val isWeekend: Boolean?,
    @SerializedName("isBankHoliday")
    val isBankHoliday: Boolean?
)
