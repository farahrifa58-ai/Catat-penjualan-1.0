package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppStrings
import com.example.model.PaymentStatus
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun PaymentStatusChip(
    status: PaymentStatus,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, textColor) = when (status) {
        PaymentStatus.PAID_IN_FULL -> Triple(
            strings.statusLunas,
            StatusGreen.copy(alpha = 0.15f),
            StatusGreen
        )
        PaymentStatus.PARTIALLY_PAID -> Triple(
            strings.statusDibayarSebagian,
            StatusAmber.copy(alpha = 0.15f),
            StatusAmber
        )
        PaymentStatus.UNPAID -> Triple(
            strings.statusBelumLunas,
            StatusRed.copy(alpha = 0.15f),
            StatusRed
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConditionStatusChip(
    isComplete: Boolean,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val (label, bgColor, textColor) = if (isComplete) {
        Triple(
            strings.statusAlokasiSisaLengkap,
            StatusGreen.copy(alpha = 0.15f),
            StatusGreen
        )
    } else {
        Triple(
            strings.statusAlokasiSisaBelumLengkap,
            StatusAmber.copy(alpha = 0.15f),
            StatusAmber
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false
        )
    }
}
