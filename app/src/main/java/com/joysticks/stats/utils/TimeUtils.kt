package com.joysticks.stats.utils

fun getCountdownText(gameTimeMillis: Long, now: Long): String {
        val diff = gameTimeMillis - now

        if (diff <= 0) return "00h 00m 00s"

        val totalSeconds = diff / 1000

        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (days > 0)
            "%dj %02dh".format(days, hours)
        else
            "%02dh %02dm %02ds".format(hours, minutes, seconds)
    }
