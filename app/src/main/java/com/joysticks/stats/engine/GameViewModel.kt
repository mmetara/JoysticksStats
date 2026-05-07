package com.joysticks.stats.engine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joysticks.stats.data.GameDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.joysticks.stats.utils.CsvExportUtils
import kotlinx.coroutines.flow.first

class GameViewModel(private val gameDataStore: GameDataStore? = null) : ViewModel() {

    val devMode = true
    var gameState by mutableStateOf(GameState())
        private set

    init {
        // Tenter de restaurer la partie au démarrage
        gameDataStore?.let { store ->
            viewModelScope.launch {
                val savedState = store.gameStateFlow.first()
                if (savedState != null) {
                    gameState = savedState
                    // Si on était en train de compter, on relance
                    if (gameState.countdownSeconds > 0) {
                        startCountdown()
                    }
                }
            }
        }
    }

    private fun saveState() {
        gameDataStore?.let { store ->
            viewModelScope.launch {
                if (gameState.screenMode == GameScreenMode.GAME_OVER) {
                    store.clearGame()
                } else {
                    store.saveGameState(gameState)
                }
            }
        }
    }

    fun loadRoster(newRoster: Roster) {
        resetGame()

        val startTime = newRoster.gameInfo.gameTimeMillis
        val now = System.currentTimeMillis()
        val seconds = maxOf(0, (startTime - now) / 1000)

        gameState = gameState.copy(
            roster = newRoster,
            gameStartTime = startTime,
            countdownSeconds = seconds,
            screenMode = GameScreenMode.PRE_GAME
        )
        saveState()
    }

    fun startGame() {
        val r = gameState.roster ?: return

        val mode = if (r.gameInfo.isHomeTeam)
            GameScreenMode.OPPONENT_SCORING
        else
            GameScreenMode.BATTING

        gameState = gameState.copy(
            gameStarted = true,
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
            gameHistory = emptyList(),
            outs = 0,
            homeScore = 0,
            awayScore = 0,
            runsThisHalfInning = 0,
            maxRunsReached = false,
            homeTeamHomeRuns = 0,
            awayTeamHomeRuns = 0,
            homeRunLimitReached = false,
            halfInningBattersBatted = 0,
        )
        saveState()
    }

    private fun endHalfInning() {
        val wasTop = gameState.isTop
        val nextIsTop = !wasTop
        val nextInning = if (!wasTop) gameState.inning + 1 else gameState.inning
        val currentInning = gameState.inning
        val isHomeBatting = gameState.isHomeTeamBatting()
        val r = gameState.roster ?: return

        // --- NOUVEAU : Marquage précis de la fin de manche sur le frappeur ---
        val history = gameState.gameHistory.toMutableList()
        val lastBatterIdx = gameState.lastBatterCompletedIndex
        
        // On cherche l'événement du frappeur qui vient de terminer, ou le dernier de la manche
        val eventIndex = if (lastBatterIdx != -1) {
            history.indexOfLast { it.playerIndex == lastBatterIdx && it.inning == currentInning && it.isHomeTeam == isHomeBatting }
        } else {
            history.indexOfLast { it.inning == currentInning && it.isHomeTeam == isHomeBatting }
        }
        
        if (eventIndex != -1) {
            history[eventIndex] = history[eventIndex].copy(isLastOfInning = true)
        }
        // --- Fin du marquage ---

        val nextBatterIndexForOurTeam = if (gameState.lastBatterCompletedIndex != -1) {
            (gameState.lastBatterCompletedIndex + 1) % r.players.size
        } else {
            0
        }

        val isHomeTeamUser = r.gameInfo.isHomeTeam
        val nextMode: GameScreenMode

        val isGameOver: Boolean
        if (nextInning > 9) { // If the next inning would be 10 or more, game over.
            isGameOver = true
        } else if (gameState.inning == 9) { // Current inning is the 9th
            if (wasTop) { // Top of 9th just finished. Going to bottom of 9th.
                isGameOver = if (isHomeTeamUser) {
                    gameState.homeScore > gameState.awayScore // Home team user wins if leading after top of 9th
                } else {
                    false // Away team user; game not over, goes to bottom of 9th
                }
            } else { // Bottom of 9th just finished. Going to top of 10th.
                isGameOver = true // Game always ends after bottom of 9th, no extra innings.
            }
        } else { // Before 9th inning, game is never over by inning count alone.
            isGameOver = false
        }

        if (isGameOver) {
            nextMode = GameScreenMode.GAME_OVER
        } else {
            nextMode = if (isHomeTeamUser) {
                if (nextIsTop) GameScreenMode.OPPONENT_SCORING else GameScreenMode.BATTING
            } else {
                if (nextIsTop) GameScreenMode.BATTING else GameScreenMode.OPPONENT_SCORING
            }
        }

        gameState = gameState.copy(
            outs = 0,
            isTop = nextIsTop,
            inning = nextInning,
            screenMode = nextMode,
            runnerOnFirst = -1,
            runnerOnSecond = -1,
            runnerOnThird = -1,
            currentBatterIndex = nextBatterIndexForOurTeam,
            firstBatterIndexOfHalfInning = nextBatterIndexForOurTeam,
            atBatResults = emptyMap(),
            playersWhoScored = emptySet(),
            runsThisHalfInning = 0,
            maxRunsReached = false,
            threeOutsReached = false,
            halfInningBattersBatted = 0,
            gameHistory = history
        )
        saveState()
    }

    fun forceEndHalfInningFromUI() {
        val adjustedRuns = if (gameState.inning < 9 && gameState.runsThisHalfInning > 3) 3 else gameState.runsThisHalfInning

        val runsToUnscore = gameState.runsThisHalfInning - adjustedRuns
        repeat(runsToUnscore) { unscoreRun(gameState.isHomeTeamBatting()) }

        gameState = gameState.copy(maxRunsReached = false, threeOutsReached = false)

        endHalfInning()
        saveState()
    }

    fun recordOut(playerIndex: Int = -1) {
        val newOuts = gameState.outs + 1
        var newState = gameState.copy(outs = newOuts)
        
        // Si un joueur est spécifié, on marque son retrait dans l'historique
        if (playerIndex != -1) {
            newState = newState.updateHistory(playerIndex, finalBase = 0, outNumber = newOuts)
        }

        if (newOuts >= 3) {
            newState = newState.copy(threeOutsReached = true)
        }
        gameState = newState
        saveState()
    }

    fun scoreRun(isHome: Boolean) {
        var newHomeScore = gameState.homeScore
        var newAwayScore = gameState.awayScore
        var newRunsThisHalfInning = gameState.runsThisHalfInning
        var newMaxRunsReached = gameState.maxRunsReached

        val canScoreTeamTotal = gameState.inning >= 9 || newRunsThisHalfInning < 3

        if (canScoreTeamTotal) {
            if (isHome) {
                newHomeScore++
            } else {
                newAwayScore++
            }
            newRunsThisHalfInning++

            if (gameState.inning < 9 && newRunsThisHalfInning >= 3) {
                newMaxRunsReached = true
            }
        } else {
            newRunsThisHalfInning++
            newMaxRunsReached = true
        }

        gameState = gameState.copy(
            homeScore = newHomeScore,
            awayScore = newAwayScore,
            runsThisHalfInning = newRunsThisHalfInning,
            maxRunsReached = newMaxRunsReached
        )
        saveState()
    }

    fun unscoreRun(isHome: Boolean) {
        gameState = if (isHome)
            gameState.copy(homeScore = maxOf(0, gameState.homeScore - 1), runsThisHalfInning = maxOf(0, gameState.runsThisHalfInning - 1))
        else
            gameState.copy(awayScore = maxOf(0, gameState.awayScore - 1), runsThisHalfInning = maxOf(0, gameState.runsThisHalfInning - 1))

        if (gameState.runsThisHalfInning < 3) {
            gameState = gameState.copy(maxRunsReached = false)
        }
        saveState()
    }

    fun resetGame() {
        gameState = GameState(
            opponentRunsPerInning = emptyMap()
        )
        gameDataStore?.let {
            viewModelScope.launch {
                it.clearGame()
            }
        }
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
        val r = gameState.roster ?: return
        val adjustedRuns = if (gameState.inning < 9 && runs > 3) 3 else runs

        // Calculate scores based on who just batted
        val newHomeScoreAfterOpponent = if (!gameState.isTop) gameState.homeScore + adjustedRuns else gameState.homeScore
        val newAwayScoreAfterOpponent = if (gameState.isTop) gameState.awayScore + adjustedRuns else gameState.awayScore

        val newOpponentRunsPerInning = gameState.opponentRunsPerInning.toMutableMap()
        val opponentInning = gameState.inning // This is the inning the opponent just completed
        newOpponentRunsPerInning[opponentInning] = (newOpponentRunsPerInning[opponentInning] ?: 0) + adjustedRuns

        // Determine the state of the *next* half-inning (after opponent's turn is complete)
        val wasOpponentBattingInTopHalf = gameState.isTop // True if opponent was Away team, False if opponent was Home team
        val nextIsTop = !wasOpponentBattingInTopHalf // If opponent was in top, we move to bottom. If opponent was in bottom, we move to top of next inning.
        val nextInning = if (wasOpponentBattingInTopHalf) gameState.inning else gameState.inning + 1 // Increment inning only if opponent just finished bottom half.

        val isHomeTeamUser = r.gameInfo.isHomeTeam
        var nextScreenMode: GameScreenMode
        var isGameOverDetermined: Boolean = false

        // Determine if the game is over based on the *proposed next state*
        if (nextInning > 9) { // If the game would move to 10th inning or beyond, game is over (fixed 9 innings).
            isGameOverDetermined = true
        } else if (nextInning == 9) { // If the game is in (or moving to) the 9th inning
            if (nextIsTop) { // Moving to top of 9th (opponent batted in bottom of 8th, which means our team is visitor)
                isGameOverDetermined = false // Cannot be over yet, away team (user) needs to bat.
            } else { // Moving to bottom of 9th (opponent batted in top of 9th)
                // If user is Home team, and Home team is leading after opponent's (away) top of 9th, game over.
                isGameOverDetermined = if (isHomeTeamUser) {
                    newHomeScoreAfterOpponent > newAwayScoreAfterOpponent
                } else {
                    false // Away team user. Game is never over after top of 9th, home team always gets to bat.
                }
            }
        } else { // Before 9th inning, game is never over by inning count alone.
            isGameOverDetermined = false
        }

        if (isGameOverDetermined) {
            nextScreenMode = GameScreenMode.GAME_OVER
        } else {
            // If the game is not over, determine the next batting/opponent scoring mode
            nextScreenMode = if (isHomeTeamUser) {
                // If user is Home team: if next is Top, opponent scores. If next is Bottom, user bats.
                if (nextIsTop) GameScreenMode.OPPONENT_SCORING else GameScreenMode.BATTING
            } else {
                // If user is Away team: if next is Top, user bats. If next is Bottom, opponent scores.
                if (nextIsTop) GameScreenMode.BATTING else GameScreenMode.OPPONENT_SCORING
            }
        }

        // Calculate the next batter index for our team, regardless of whose turn it is now.
        // This is based on the last completed batter, so when it's our turn to bat, we continue the lineup.
        val nextOurTeamBatterIndex = if (gameState.lastBatterCompletedIndex != -1) {
            (gameState.lastBatterCompletedIndex + 1) % r.players.size
        } else {
            0
        }

        gameState = gameState.copy(
            homeScore = newHomeScoreAfterOpponent,
            awayScore = newAwayScoreAfterOpponent,
            isTop = nextIsTop,
            inning = nextInning,
            waitingOpponentScore = false,
            screenMode = nextScreenMode,
            runnerOnFirst = -1,
            runnerOnSecond = -1,
            runnerOnThird = -1,
            // Update batter index only if the next screen mode is BATTING (our team's turn to bat)
            currentBatterIndex = if (nextScreenMode == GameScreenMode.BATTING) nextOurTeamBatterIndex else gameState.currentBatterIndex,
            firstBatterIndexOfHalfInning = if (nextScreenMode == GameScreenMode.BATTING) nextOurTeamBatterIndex else gameState.firstBatterIndexOfHalfInning,
            atBatResults = emptyMap(),
            playersWhoScored = emptySet(),
            outs = 0,
            runsThisHalfInning = 0,
            maxRunsReached = false,
            halfInningBattersBatted = 0,
            opponentRunsPerInning = newOpponentRunsPerInning
        )
        saveState()
    }

    fun nextBatter() {
        val r = gameState.roster ?: return
        
        // Avant d'avancer, on vérifie si on est en fin de manche (9ème+)
        if (gameState.inning >= 9 && gameState.halfInningBattersBatted >= r.players.size) {
            endHalfInning()
            return
        }

        val nextIndex = (gameState.currentBatterIndex + 1) % r.players.size
        gameState = gameState.copy(
            currentBatterIndex = nextIndex,
            halfInningBattersBatted = gameState.halfInningBattersBatted + 1
        )
        saveState()
    }

    fun previousBatter() {
        if (gameState.halfInningBattersBatted <= 0) return
        val r = gameState.roster ?: return
        val prevIndex = (gameState.currentBatterIndex - 1 + r.players.size) % r.players.size
        gameState = gameState.copy(
            currentBatterIndex = prevIndex,
            halfInningBattersBatted = gameState.halfInningBattersBatted - 1
        )
        saveState()
    }

    private fun isHomeTeam(): Boolean = gameState.roster?.gameInfo?.isHomeTeam ?: false

    private fun updateHistoryEvent(
        playerIndex: Int,
        result: BattingResult? = null,
        finalBase: Int? = null,
        rbi: Int? = null,
        retiredOnOptionel: Boolean = false,
        outNumber: Int = 0
    ) {
        gameState = gameState.updateHistory(playerIndex, result, finalBase, rbi, retiredOnOptionel, outNumber)
    }

    fun handleBattingResult(result: BattingResult) {
        gameState = BattingHandler.handleBattingResult(
            gameState = gameState,
            result = result,
            isHomeTeam = { isHomeTeam() }
        )
        saveState()
    }

    fun manualAdvancePlayer(playerIndex: Int) {
        val isHome = gameState.isHomeTeamBatting()
        var r1 = gameState.runnerOnFirst
        var r2 = gameState.runnerOnSecond
        var r3 = gameState.runnerOnThird
        val scorers = gameState.playersWhoScored.toMutableSet()

        if (playerIndex == r3) {
            scorers.add(playerIndex)
            r3 = -1
            scoreRun(isHome)
            gameState = gameState.updateHistory(playerIndex, finalBase = 4)
        } else if (playerIndex == r2) {
            r3 = r2
            r2 = -1
            gameState = gameState.updateHistory(playerIndex, finalBase = 3)
        } else if (playerIndex == r1) {
            r2 = r1
            r1 = -1
            gameState = gameState.updateHistory(playerIndex, finalBase = 2)
        } else {
            return
        }

        gameState = gameState.copy(
            runnerOnFirst = r1,
            runnerOnSecond = r2,
            runnerOnThird = r3,
            playersWhoScored = scorers
        )
        if (gameState.inning < 9 && gameState.runsThisHalfInning >= 3) {
            gameState = gameState.copy(maxRunsReached = true)
        }
        saveState()
    }

    fun manualRetreatPlayer(playerIndex: Int) {
        val isHome = gameState.isHomeTeamBatting()
        var r1 = gameState.runnerOnFirst
        var r2 = gameState.runnerOnSecond
        var r3 = gameState.runnerOnThird
        val scorers = gameState.playersWhoScored.toMutableSet()
        var rbiAdjustment = 0

        if (scorers.contains(playerIndex)) {
            if (r3 == -1) {
                scorers.remove(playerIndex)
                r3 = playerIndex
                unscoreRun(isHome)
                gameState = gameState.updateHistory(playerIndex, finalBase = 3)
                rbiAdjustment = -1
            }
        } else if (playerIndex == r3) {
            if (r2 == -1) {
                r2 = r3
                r3 = -1
                gameState = gameState.updateHistory(playerIndex, finalBase = 2)
            }
        } else if (playerIndex == r2) {
            if (r1 == -1) {
                r1 = r2
                r2 = -1
                gameState = gameState.updateHistory(playerIndex, finalBase = 1)
            }
        }

        gameState = gameState.copy(
            runnerOnFirst = r1,
            runnerOnSecond = r2,
            runnerOnThird = r3,
            playersWhoScored = scorers
        )
        val currentBatterEventIndex = gameState.gameHistory.indexOfLast { it.playerIndex == gameState.currentBatterIndex && it.inning == gameState.inning && it.isHomeTeam == isHomeTeam() }
        if (currentBatterEventIndex != -1) {
            val oldEvent = gameState.gameHistory[currentBatterEventIndex]
            updateHistoryEvent(gameState.currentBatterIndex, rbi = oldEvent.rbi + rbiAdjustment)
        }
        saveState()
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
        updateHistoryEvent(playerIndex, finalBase = 0)
        recordOut(playerIndex)
        saveState()
    }
    fun manualAddRBI(playerIndex: Int) {
        val currentInning = gameState.inning
        val isHome = gameState.isHomeTeamBatting()
        val history = gameState.gameHistory.toMutableList()
        val index = history.indexOfLast { it.playerIndex == playerIndex && it.inning == currentInning && it.isHomeTeam == isHome }
        if (index != -1) {
            val oldEvent = history[index]
            history[index] = oldEvent.copy(rbi = oldEvent.rbi + 1)
            gameState = gameState.copy(gameHistory = history)
        }
        saveState()
    }
    fun manualRemoveRBI(playerIndex: Int) {
        val currentInning = gameState.inning
        val isHome = gameState.isHomeTeamBatting()
        val history = gameState.gameHistory.toMutableList()
        val index = history.indexOfLast { it.playerIndex == playerIndex && it.inning == currentInning && it.isHomeTeam == isHome }
        if (index != -1) {
            val oldEvent = history[index]
            if (oldEvent.rbi > 0) {
                history[index] = oldEvent.copy(rbi = oldEvent.rbi - 1)
                gameState = gameState.copy(gameHistory = history)
            }
        }
        saveState()
    }
    fun generateCsv(): String {
        return CsvExportUtils.generateCsv(gameState)
    }

    // New function to edit a historical AtBatEvent
    fun editAtBatEvent(editedEvent: AtBatEvent) {
        val history = gameState.gameHistory.toMutableList()
        val index = history.indexOfFirst {
            it.playerIndex == editedEvent.playerIndex &&
            it.inning == editedEvent.inning &&
            it.isHomeTeam == editedEvent.isHomeTeam
        }

        if (index != -1) {
            val oldEvent = history[index]
            val rbiDifference = editedEvent.rbi - oldEvent.rbi

            // Update the event in history
            history[index] = editedEvent

            // Adjust scores based on RBI difference
            gameState = if (editedEvent.isHomeTeam) {
                gameState.copy(homeScore = gameState.homeScore + rbiDifference)
            } else {
                gameState.copy(awayScore = gameState.awayScore + rbiDifference)
            }

            gameState = gameState.copy(gameHistory = history)
            saveState()
        }
    }
}