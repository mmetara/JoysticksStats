package com.joysticks.stats.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    val devMode = true
    var gameState by mutableStateOf(GameState())
        private set

    fun loadRoster(newRoster: Roster) {
        val startTime = newRoster.gameInfo.gameTimeMillis
        val now = System.currentTimeMillis()
        val seconds = maxOf(0, (startTime - now) / 1000)

        gameState = gameState.copy(
            roster = newRoster,
            gameStartTime = startTime,
            countdownSeconds = seconds
        )
    }

    fun startGame() {
        val r = gameState.roster ?: return
        val engine = GameEngine(
            lineupSize = r.players.size,
            isHomeTeam = r.gameInfo.isHomeTeam
        )

        val mode = if (r.gameInfo.isHomeTeam)
            GameScreenMode.OPPONENT_SCORING
        else
            GameScreenMode.BATTING

        gameState = gameState.copy(
            engine = engine,
            inning = 1,
            isTop = true,
            waitingOpponentScore = r.gameInfo.isHomeTeam,
            screenMode = mode,
            currentBatterIndex = 0,
            firstBatterIndexOfHalfInning = 0,
            atBatResults = emptyMap(),
            runnerOnFirst = -1,
            runnerOnSecond = -1,
            runnerOnThird = -1,
            playersWhoScored = emptySet(),
            outs = 0,
            homeScore = 0,
            awayScore = 0
        )
    }

    fun recordOut() {
        val newOuts = gameState.outs + 1
        val r = gameState.roster ?: return

        if (newOuts >= 3) {
            val wasTop = gameState.isTop
            val nextIsTop = !wasTop
            val nextInning = if (!wasTop) gameState.inning + 1 else gameState.inning

            // On prépare le prochain frappeur pour le retour de cette équipe
            val nextBatterIndex = (gameState.currentBatterIndex + 1) % r.players.size

            val isHome = r.gameInfo.isHomeTeam
            val nextMode = if (isHome) {
                if (nextIsTop) GameScreenMode.OPPONENT_SCORING else GameScreenMode.BATTING
            } else {
                if (nextIsTop) GameScreenMode.BATTING else GameScreenMode.OPPONENT_SCORING
            }

            gameState = gameState.copy(
                outs = 0,
                isTop = nextIsTop,
                inning = nextInning,
                screenMode = nextMode,
                runnerOnFirst = -1,
                runnerOnSecond = -1,
                runnerOnThird = -1,
                currentBatterIndex = nextBatterIndex,
                firstBatterIndexOfHalfInning = nextBatterIndex,
                atBatResults = emptyMap(),
                playersWhoScored = emptySet()
            )
        } else {
            gameState = gameState.copy(outs = newOuts)
        }
    }

    fun scoreRun(isHome: Boolean) {
        gameState = if (isHome)
            gameState.copy(homeScore = gameState.homeScore + 1)
        else
            gameState.copy(awayScore = gameState.awayScore + 1)
    }

    fun resetGame() {
        gameState = GameState()
    }

    fun startCountdown() {
        viewModelScope.launch {
            while (gameState.countdownSeconds > 0) {
                delay(1000)
                gameState = gameState.copy(
                    countdownSeconds = gameState.countdownSeconds - 1
                )
            }
        }
    }

    fun endOpponentHalfInning(runs: Int) {
        val newScore = if (gameState.isTop) gameState.awayScore + runs else gameState.homeScore + runs

        gameState = if (gameState.isTop) {
            gameState.copy(
                awayScore = newScore,
                isTop = false,
                waitingOpponentScore = false,
                screenMode = GameScreenMode.BATTING,
                runnerOnFirst = -1,
                runnerOnSecond = -1,
                runnerOnThird = -1,
                firstBatterIndexOfHalfInning = gameState.currentBatterIndex,
                atBatResults = emptyMap(),
                playersWhoScored = emptySet(),
                outs = 0
            )
        } else {
            gameState.copy(
                homeScore = newScore,
                isTop = true,
                inning = gameState.inning + 1,
                waitingOpponentScore = false,
                screenMode = GameScreenMode.BATTING,
                runnerOnFirst = -1,
                runnerOnSecond = -1,
                runnerOnThird = -1,
                firstBatterIndexOfHalfInning = gameState.currentBatterIndex,
                atBatResults = emptyMap(),
                playersWhoScored = emptySet(),
                outs = 0
            )
        }
    }

    fun nextBatter() {
        val r = gameState.roster ?: return
        val nextIndex = (gameState.currentBatterIndex + 1) % r.players.size
        gameState = gameState.copy(currentBatterIndex = nextIndex)
    }

    fun previousBatter() {
        if (gameState.currentBatterIndex == gameState.firstBatterIndexOfHalfInning) return
        val r = gameState.roster ?: return
        val prevIndex = (gameState.currentBatterIndex - 1 + r.players.size) % r.players.size
        gameState = gameState.copy(currentBatterIndex = prevIndex)
    }

    private fun isHomeTeam(): Boolean = gameState.roster?.gameInfo?.isHomeTeam ?: false

    private fun advanceRunners(bases: Int) {
        val isHome = isHomeTeam()
        var r1 = gameState.runnerOnFirst
        var r2 = gameState.runnerOnSecond
        var r3 = gameState.runnerOnThird
        val scorers = gameState.playersWhoScored.toMutableSet()
        val batterIndex = gameState.currentBatterIndex

        repeat(bases) { step ->
            if (r3 != -1) scorers.add(r3)
            r3 = r2
            r2 = r1
            r1 = if (step == 0) batterIndex else -1
        }

        val newRuns = scorers.size - gameState.playersWhoScored.size
        repeat(newRuns) { scoreRun(isHome) }

        gameState = gameState.copy(
            runnerOnFirst = r1,
            runnerOnSecond = r2,
            runnerOnThird = r3,
            playersWhoScored = scorers
        )
    }

    fun handleBattingResult(result: BattingResult) {
        val newAtBatResults = gameState.atBatResults.toMutableMap()
        newAtBatResults[gameState.currentBatterIndex] = result
        gameState = gameState.copy(atBatResults = newAtBatResults)

        when (result) {
            BattingResult.Single -> {
                advanceRunners(1)
                nextBatter()
            }
            BattingResult.Double -> {
                advanceRunners(2)
                nextBatter()
            }
            BattingResult.Triple -> {
                advanceRunners(3)
                nextBatter()
            }
            BattingResult.HomeRun -> {
                val scorers = gameState.playersWhoScored.toMutableSet()
                if (gameState.runnerOnFirst != -1) scorers.add(gameState.runnerOnFirst)
                if (gameState.runnerOnSecond != -1) scorers.add(gameState.runnerOnSecond)
                if (gameState.runnerOnThird != -1) scorers.add(gameState.runnerOnThird)
                scorers.add(gameState.currentBatterIndex)
                
                val newRuns = scorers.size - gameState.playersWhoScored.size
                repeat(newRuns) { scoreRun(isHomeTeam()) }
                
                gameState = gameState.copy(
                    runnerOnFirst = -1,
                    runnerOnSecond = -1,
                    runnerOnThird = -1,
                    playersWhoScored = scorers
                )
                nextBatter()
            }
            BattingResult.Optionel -> {
                val batterIdx = gameState.currentBatterIndex
                recordOut()
                if (gameState.outs > 0 || gameState.screenMode == GameScreenMode.BATTING) {
                     // Si la manche n'est pas finie, le frappeur va au 1er
                     gameState = gameState.copy(runnerOnFirst = batterIdx)
                     nextBatter()
                }
            }
            BattingResult.Strikeout, BattingResult.Out -> {
                recordOut()
                if (gameState.outs > 0 || gameState.screenMode == GameScreenMode.BATTING) {
                    nextBatter()
                }
            }
        }
    }

    fun manualAdvancePlayer(playerIndex: Int) {
        val isHome = isHomeTeam()
        var r1 = gameState.runnerOnFirst
        var r2 = gameState.runnerOnSecond
        var r3 = gameState.runnerOnThird
        val scorers = gameState.playersWhoScored.toMutableSet()

        when (playerIndex) {
            r3 -> {
                scorers.add(playerIndex)
                r3 = -1
                scoreRun(isHome)
            }
            r2 -> {
                r3 = r2
                r2 = -1
            }
            r1 -> {
                r2 = r1
                r1 = -1
            }
        }

        gameState = gameState.copy(
            runnerOnFirst = r1,
            runnerOnSecond = r2,
            runnerOnThird = r3,
            playersWhoScored = scorers
        )
    }

    fun manualRemovePlayer(playerIndex: Int) {
        var r1 = gameState.runnerOnFirst
        var r2 = gameState.runnerOnSecond
        var r3 = gameState.runnerOnThird

        if (r1 == playerIndex) r1 = -1
        else if (r2 == playerIndex) r2 = -1
        else if (r3 == playerIndex) r3 = -1
        else return

        gameState = gameState.copy(
            runnerOnFirst = r1,
            runnerOnSecond = r2,
            runnerOnThird = r3
        )
        recordOut()
    }
}
