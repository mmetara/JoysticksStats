package com.joysticks.stats.engine

data class GameState(

    val roster: Roster? = null,
    val inning: Int = 1,
    val isTop: Boolean = true,
    val outs: Int = 0,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val gameStartTime: Long = 0L,
    val countdownSeconds: Long = 0L,
    val gameStarted: Boolean = false,
    val waitingOpponentScore: Boolean = false,
    val screenMode: GameScreenMode = GameScreenMode.PRE_GAME,
    val currentBatterIndex: Int = 0,
    val lastBatterCompletedIndex: Int = -1,
    val firstBatterIndexOfHalfInning: Int = 0,
    val atBatResults: Map<Int, BattingResult> = emptyMap(),
    val runnerOnFirst: Int = -1,
    val runnerOnSecond: Int = -1,
    val runnerOnThird: Int = -1,
    val playersWhoScored: Set<Int> = emptySet(),

    // Historique complet de la partie pour la feuille de match
    val gameHistory: List<AtBatEvent> = emptyList(),

    val runsThisHalfInning: Int = 0, // Points marqués dans la demi-manche actuelle
    val maxRunsReached: Boolean = false, // Indique si la limite de points est atteinte
    val threeOutsReached: Boolean = false, // Indique si 3 retraits sont atteints

    // Nouveaux champs pour la limite de circuits et la 9e manche
    val homeTeamHomeRuns: Int = 0,
    val awayTeamHomeRuns: Int = 0,
    val homeRunLimitReached: Boolean = false, // Vrai si l'équipe actuelle a atteint la limite
    val halfInningBattersBatted: Int = 0, // Nombre de frappeurs passés dans cette demi-manche
    val opponentRunsPerInning: Map<Int, Int> = emptyMap(), // Points de l'adversaire par manche
) {
    val currentBatter: PlayerStats?
        get() = roster?.players?.getOrNull(currentBatterIndex)

    val nextBatter: PlayerStats?
        get() = roster?.players?.getOrNull(
            (currentBatterIndex + 1) % (roster?.players?.size ?: 1)
        )

    // Helper pour savoir si c'est l'équipe locale qui bat
    fun isHomeTeamBatting(): Boolean {
        // L'équipe locale frappe toujours au bas de la manche.
        // L'équipe visiteuse frappe toujours au haut de la manche.
        return !isTop
    }
}

data class AtBatEvent(
    val playerIndex: Int,
    val inning: Int,
    val result: BattingResult,
    val finalBase: Int, // 0: Out, 1: 1B, 2: 2B, 3: 3B, 4: Marbre
    val isHomeTeam: Boolean,
    val rbi: Int = 0, // Points produits
    val retiredOnOptionel: Boolean = false, // Indique si un coureur a été retiré sur optionnel lors de cet événement
    val outNumber: Int = 0, // Le numéro du retrait (1, 2 ou 3) si cet événement a causé un retrait
    val isLastOfInning: Boolean = false // Indique si cet événement est le dernier de la demi-manche
)

data class AtBatRecord(
    val batterName: String,
    val result: String // "1B", "K", "Out", etc.
)

enum class GameScreenMode {
    PRE_GAME,
    OPPONENT_SCORING,
    BATTING,
    GAME_OVER
}

enum class BattingResult {
    Single, Double, Triple, HomeRun, Optionel,
    Strikeout, Out
}
