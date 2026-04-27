package com.joysticks.stats.utils

import com.joysticks.stats.engine.BattingResult

fun getShortResult(result: BattingResult): String {
    return when(result) {
        BattingResult.Single -> "1B"
        BattingResult.Double -> "2B"
        BattingResult.Triple -> "3B"
        BattingResult.HomeRun -> "CC"
        BattingResult.Optionel -> "Opt"
        BattingResult.Strikeout -> "K"
        BattingResult.Out -> "OUT"
    }
}

fun getDisplayPriority(result: BattingResult): Int {
    return when (result) {
        BattingResult.HomeRun -> 6
        BattingResult.Triple -> 5
        BattingResult.Double -> 4
        BattingResult.Single -> 3
        BattingResult.Optionel -> 2 // Le frappeur a atteint une base
        BattingResult.Strikeout -> 1
        BattingResult.Out -> 1
    }
}
