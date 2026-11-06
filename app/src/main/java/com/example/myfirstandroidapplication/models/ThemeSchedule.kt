package com.example.myfirstandroidapplication.models

import com.example.myfirstandroidapplication.adapters.ReaderTheme

data class ThemeSchedule(
    val autoThemeEnabled: Boolean = true,
    val schedules: List<ThemeTimeSlot> = defaultSchedules()
) {
    companion object {
        fun defaultSchedules(): List<ThemeTimeSlot> {
            return listOf(
                ThemeTimeSlot(7, 9, ReaderTheme.LIGHT),
                ThemeTimeSlot(9, 19, ReaderTheme.SEPIA),
                ThemeTimeSlot(19, 7, ReaderTheme.DARK)
            )
        }
    }

    fun getThemeForCurrentHour(): ReaderTheme {
        if (!autoThemeEnabled) return ReaderTheme.SEPIA

        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        return schedules.firstOrNull { slot ->
            if (slot.startHour < slot.endHour) {
                currentHour in slot.startHour until slot.endHour
            } else {
                // Overnight slot (e.g., 19 to 7)
                currentHour >= slot.startHour || currentHour < slot.endHour
            }
        }?.theme ?: ReaderTheme.SEPIA
    }
}

data class ThemeTimeSlot(
    val startHour: Int,
    val endHour: Int,
    val theme: ReaderTheme
)
