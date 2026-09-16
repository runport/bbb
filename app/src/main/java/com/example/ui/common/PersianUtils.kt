package com.example.ui.common

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PersianUtils {
    private val decimalFormat = DecimalFormat("#,###")

    fun toPersianDigits(text: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else if (ch == ',') {
                sb.append('،')
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatPrice(amount: Double, currency: String = "تومان"): String {
        val formatted = decimalFormat.format(amount.toLong())
        return "${toPersianDigits(formatted)} $currency"
    }

    fun formatNumber(number: Number): String {
        val str = if (number is Double || number is Float) {
            val d = number.toDouble()
            if (d == d.toLong().toDouble()) {
                decimalFormat.format(d.toLong())
            } else {
                String.format(Locale.US, "%.2f", d)
            }
        } else {
            decimalFormat.format(number.toLong())
        }
        return toPersianDigits(str)
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
        return toPersianDigits(sdf.format(Date(timestamp)))
    }

    fun formatDateOnly(timestamp: Long): String {
        if (timestamp <= 0) return "-"
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return toPersianDigits(sdf.format(Date(timestamp)))
    }
}
