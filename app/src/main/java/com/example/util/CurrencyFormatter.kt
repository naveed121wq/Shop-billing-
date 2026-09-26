package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object CurrencyFormatter {

    fun formatPkr(amount: Double, currency: String = "PKR"): String {
        val format = NumberFormat.getNumberInstance(Locale.US)
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 2
        return "$currency ${format.format(amount)}"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun getDayString(timestamp: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(timestamp))
    }

    fun getStartOfDay(calendar: Calendar = Calendar.getInstance()): Long {
        val c = calendar.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun getEndOfDay(calendar: Calendar = Calendar.getInstance()): Long {
        val c = calendar.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        return c.timeInMillis
    }
}
