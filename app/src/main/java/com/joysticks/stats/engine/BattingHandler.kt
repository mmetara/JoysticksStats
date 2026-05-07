package com.joysticks.stats.engine

object BattingHandler {

    fun handleBattingResult(
        gameState: GameState,
        result: BattingResult,
        isHomeTeam: () -> Boolean
    ): GameState {
        var currentState = gameState
        val batterIdx = currentState.currentBatterIndex
        val isHome = currentState.isHomeTeamBatting()
        val r = currentState.roster ?: return currentState

        var rbiProduced = 0
        var shouldAdvanceToNextBatter = true
        var finalBaseOfBatter = 0

        // Sauvegarde des coureurs actuels pour le calcul
        var r1 = currentState.runnerOnFirst
        var r2 = currentState.runnerOnSecond
        var r3 = currentState.runnerOnThird
        val scorers = currentState.playersWhoScored.toMutableSet()
        val initialScorersCount = scorers.size

        when (result) {
            BattingResult.Single, BattingResult.Double, BattingResult.Triple -> {
                val bases = when(result) {
                    BattingResult.Single -> 1
                    BattingResult.Double -> 2
                    else -> 3
                }
                finalBaseOfBatter = bases
                
                // Logique d'avancement des coureurs
                repeat(bases) { step ->
                    if (r3 != -1) {
                        scorers.add(r3)
                        currentState = currentState.updateHistory(r3, finalBase = 4)
                    }
                    r3 = r2
                    if (r3 != -1) currentState = currentState.updateHistory(r3, finalBase = 3)
                    
                    r2 = r1
                    if (r2 != -1) currentState = currentState.updateHistory(r2, finalBase = 2)
                    
                    r1 = if (step == 0) batterIdx else -1
                    if (r1 != -1) currentState = currentState.updateHistory(r1, finalBase = 1)
                }
            }

            BattingResult.HomeRun -> {
                val isHomeTeamBattingNow = isHomeTeam()
                currentState = if (isHomeTeamBattingNow) {
                    currentState.copy(homeTeamHomeRuns = currentState.homeTeamHomeRuns + 1)
                } else {
                    currentState.copy(awayTeamHomeRuns = currentState.awayTeamHomeRuns + 1)
                }

                // Si limite de HR atteinte (ex: 5), on ne donne que 2 bases (règle spécifique possible)
                if (currentState.homeTeamHomeRuns > 5 || currentState.awayTeamHomeRuns > 5) {
                    finalBaseOfBatter = 2
                    repeat(2) { step ->
                        if (r3 != -1) { 
                            scorers.add(r3)
                            currentState = currentState.updateHistory(r3, finalBase = 4)
                        }
                        r3 = r2; if (r3 != -1) currentState = currentState.updateHistory(r3, finalBase = 3)
                        r2 = r1; if (r2 != -1) currentState = currentState.updateHistory(r2, finalBase = 2)
                        r1 = if (step == 0) batterIdx else -1; if (r1 != -1) currentState = currentState.updateHistory(r1, finalBase = 1)
                    }
                } else {
                    finalBaseOfBatter = 4
                    if (r1 != -1) { scorers.add(r1); currentState = currentState.updateHistory(r1, finalBase = 4) }
                    if (r2 != -1) { scorers.add(r2); currentState = currentState.updateHistory(r2, finalBase = 4) }
                    if (r3 != -1) { scorers.add(r3); currentState = currentState.updateHistory(r3, finalBase = 4) }
                    scorers.add(batterIdx)
                    r1 = -1; r2 = -1; r3 = -1
                }
            }

            BattingResult.Optionel -> {
                finalBaseOfBatter = 1
                val r1Before = r1
                val r2Before = r2
                
                val newOuts = currentState.outs + 1
                currentState = currentState.copy(outs = newOuts)
                
                // Avancement forcé d'une base
                if (r3 != -1) { scorers.add(r3); currentState = currentState.updateHistory(r3, finalBase = 4) }
                r3 = r2; if (r3 != -1) currentState = currentState.updateHistory(r3, finalBase = 3)
                r2 = r1; if (r2 != -1) currentState = currentState.updateHistory(r2, finalBase = 2)
                r1 = batterIdx; currentState = currentState.updateHistory(r1, finalBase = 1)

                // Retrait sur optionnel (souvent le coureur le plus avancé)
                if (r3 != -1 && r2Before != -1 && !scorers.contains(r3)) {
                    currentState = currentState.updateHistory(r3, retiredOnOptionel = true, outNumber = newOuts)
                    r3 = -1
                } else if (r2 != -1 && r1Before != -1 && !scorers.contains(r2)) {
                    currentState = currentState.updateHistory(r2, retiredOnOptionel = true, outNumber = newOuts)
                    r2 = -1
                } else if (r1Before != -1) {
                    currentState = currentState.updateHistory(r1Before, retiredOnOptionel = true, outNumber = newOuts)
                    // Note: ici r1 est déjà le frappeur, donc on ne met pas r1 à -1
                }
            }

            BattingResult.Strikeout, BattingResult.Out -> {
                val newOuts = currentState.outs + 1
                currentState = currentState.copy(outs = newOuts)
                currentState = currentState.updateHistory(batterIdx, result = result, outNumber = newOuts)
                shouldAdvanceToNextBatter = (newOuts < 3)
            }
        }

        // Calcul des points produits (RBI)
        rbiProduced = scorers.size - initialScorersCount
        
        // Mise à jour des scores dans l'état
        var newHomeScore = currentState.homeScore
        var newAwayScore = currentState.awayScore
        var newRunsThisHalfInning = currentState.runsThisHalfInning
        
        repeat(rbiProduced) {
            if (currentState.inning < 9 && newRunsThisHalfInning < 3) {
                if (isHome) newHomeScore++ else newAwayScore++
            } else if (currentState.inning >= 9) {
                if (isHome) newHomeScore++ else newAwayScore++
            }
            newRunsThisHalfInning++
        }

        val maxRunsReached = currentState.inning < 9 && newRunsThisHalfInning >= 3
        val threeOutsReached = currentState.outs >= 3

        // Mise à jour finale de l'historique pour le frappeur
        currentState = currentState.updateHistory(batterIdx, result = result, finalBase = finalBaseOfBatter, rbi = rbiProduced)

        // Préparation du prochain état
        var nextBatterIdx = batterIdx
        var battersBatted = currentState.halfInningBattersBatted
        if (shouldAdvanceToNextBatter && !maxRunsReached && !threeOutsReached) {
            nextBatterIdx = (batterIdx + 1) % r.players.size
            battersBatted++
        }

        return currentState.copy(
            runnerOnFirst = r1,
            runnerOnSecond = r2,
            runnerOnThird = r3,
            playersWhoScored = scorers,
            homeScore = newHomeScore,
            awayScore = newAwayScore,
            runsThisHalfInning = newRunsThisHalfInning,
            maxRunsReached = maxRunsReached,
            threeOutsReached = threeOutsReached,
            currentBatterIndex = nextBatterIdx,
            halfInningBattersBatted = battersBatted,
            lastBatterCompletedIndex = batterIdx
        )
    }
}
