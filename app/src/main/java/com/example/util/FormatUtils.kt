package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val idLocale = Locale("id", "ID")

    /**
     * Formats number with Indonesian dot thousand separator: 1.000, 10.000, 100.000
     */
    fun formatNumber(amount: Double): String {
        return try {
            val symbols = DecimalFormatSymbols(idLocale).apply {
                groupingSeparator = '.'
                decimalSeparator = ','
            }
            val df = DecimalFormat("#,##0", symbols)
            df.format(amount)
        } catch (_: Exception) {
            String.format(Locale.US, "%,.0f", amount).replace(",", ".")
        }
    }

    fun formatNumber(amount: Long): String = formatNumber(amount.toDouble())
    fun formatNumber(amount: Int): String = formatNumber(amount.toDouble())

    fun formatRupiah(amount: Double): String {
        return "Rp " + formatNumber(amount)
    }

    /**
     * Parses numeric text containing dots like "10.000", "100.000", or "Rp 10.000" into Double
     */
    fun parseAmount(text: String): Double {
        val clean = text.replace(".", "")
            .replace(",", ".")
            .replace("Rp", "", ignoreCase = true)
            .trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    fun getTodayDateIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getYesterdayDateIso(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    fun formatDateDisplay(isoDate: String, isEnglish: Boolean = false): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(isoDate) ?: return isoDate
            val locale = if (isEnglish) Locale.ENGLISH else idLocale
            val formatter = SimpleDateFormat("dd MMMM yyyy", locale)
            formatter.format(date)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun formatShortDate(isoDate: String, isEnglish: Boolean = false): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(isoDate) ?: return isoDate
            val locale = if (isEnglish) Locale.ENGLISH else idLocale
            val formatter = SimpleDateFormat("dd MMM", locale)
            formatter.format(date)
        } catch (_: Exception) {
            isoDate
        }
    }

    fun shiftDays(isoDate: String, days: Int): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(isoDate) ?: Date()
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, days)
            }
            parser.format(cal.time)
        } catch (_: Exception) {
            isoDate
        }
    }
}
