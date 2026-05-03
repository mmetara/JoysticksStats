package com.joysticks.stats.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.joysticks.stats.engine.AtBatEvent
import com.joysticks.stats.engine.BattingResult
import com.joysticks.stats.engine.GameState
import com.joysticks.stats.engine.Roster
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExportUtils {

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun exportStatsSheetToPdf(context: Context, uri: Uri, gameState: GameState, roster: Roster?) {
        if (roster == null) return

        val pdfDocument = PdfDocument()
        // US Letter Landscape: 792 x 612 points
        val pageInfo = PdfDocument.PageInfo.Builder(792, 612, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        // --- Configuration des dimensions (Format Lettre) ---
        val margin = 20f
        val startY = 55f
        val rowHeight = 36f 
        val nameWidth = 180f 
        val posWidth = 35f
        val inningWidth = 55f
        val maxInnings = 9
        val totalTableWidth = nameWidth + posWidth + (maxInnings * inningWidth)

        // --- Titre et Infos (Date + Heure) ---
        textPaint.textSize = 16f
        textPaint.isFakeBoldText = true
        // Format standard: Visiteur vs Receveur (Away vs Home)
        canvas.drawText("${roster.gameInfo.awayTeamName} vs ${roster.gameInfo.homeTeamName}".uppercase(), margin, 30f, textPaint)
        
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = false
        val displayDate = roster.gameInfo.gameDate
        // Extraction robuste de l'heure (supporte "17:40", "2026-04-30 17:40" ou "T17:40")
        val rawTime = roster.gameInfo.gameTime.trim()
        val timePart = rawTime.split(Regex("[\\sT]")).last()
        val displayTime = timePart.replace(":", "h")
        canvas.drawText("Partie du : $displayDate $displayTime | Joysticks Stats Pro", margin, 45f, textPaint)

        // --- En-tête du tableau ---
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(235, 235, 235)
        canvas.drawRect(margin, startY, margin + totalTableWidth, startY + 20f, paint)
        
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
        paint.strokeWidth = 1f
        canvas.drawRect(margin, startY, margin + totalTableWidth, startY + 20f, paint)

        textPaint.isFakeBoldText = true
        textPaint.textSize = 9f
        canvas.drawText("NOM DU JOUEUR", margin + 5f, startY + 14f, textPaint)
        canvas.drawText("POS", margin + nameWidth + 5f, startY + 14f, textPaint)
        
        canvas.drawLine(margin + nameWidth, startY, margin + nameWidth, startY + 20f, paint)
        canvas.drawLine(margin + nameWidth + posWidth, startY, margin + nameWidth + posWidth, startY + 20f, paint)

        for (i in 1..maxInnings) {
            val x = margin + nameWidth + posWidth + ((i - 1) * inningWidth)
            canvas.drawText("$i", x + (inningWidth / 2) - 3f, startY + 14f, textPaint)
            canvas.drawLine(x, startY, x, startY + 20f, paint)
        }

        // --- Lignes des joueurs ---
        var currentY = startY + 20f
        roster.players.forEach { player ->
            canvas.drawRect(margin, currentY, margin + totalTableWidth, currentY + rowHeight, paint)
            canvas.drawLine(margin + nameWidth, currentY, margin + nameWidth, currentY + rowHeight, paint)
            canvas.drawLine(margin + nameWidth + posWidth, currentY, margin + nameWidth + posWidth, currentY + rowHeight, paint)

            textPaint.isFakeBoldText = true
            textPaint.textSize = 10f
            canvas.drawText(player.playerName.uppercase(), margin + 5f, currentY + (rowHeight / 2) + 4f, textPaint)
            
            textPaint.isFakeBoldText = false
            textPaint.color = Color.DKGRAY
            canvas.drawText(player.posDef, margin + nameWidth + 8f, currentY + (rowHeight / 2) + 4f, textPaint)
            textPaint.color = Color.BLACK

            for (inning in 1..maxInnings) {
                val cellX = margin + nameWidth + posWidth + ((inning - 1) * inningWidth)
                val events = gameState.gameHistory.filter { 
                    it.playerIndex == player.index && it.inning == inning && it.isHomeTeam == roster.gameInfo.isHomeTeam 
                }
                if (events.isNotEmpty()) {
                    drawDiamondResult(canvas, events, cellX, currentY, inningWidth, rowHeight)
                }
                canvas.drawLine(cellX, currentY, cellX, currentY + rowHeight, paint)
            }
            currentY += rowHeight
        }

        // --- Lignes de résumé (Visiteur en haut, Receveur en bas) ---
        val summaryRowHeight = 25f
        val userRunsPerInning = (1..maxInnings).map { inv ->
            gameState.gameHistory.count { it.inning == inv && it.finalBase == 4 && it.isHomeTeam == roster.gameInfo.isHomeTeam }
        }
        val oppRunsPerInning = (1..maxInnings).map { inv -> gameState.opponentRunsPerInning[inv] ?: 0 }

        // La ligne du haut est TOUJOURS l'équipe visiteuse (Away)
        val awayName = roster.gameInfo.awayTeamName
        val awayRuns = if (roster.gameInfo.isHomeTeam) oppRunsPerInning else userRunsPerInning
        drawSummaryRow(canvas, margin, currentY, totalTableWidth, nameWidth + posWidth, inningWidth, awayName.uppercase(), awayRuns, paint, textPaint)
        currentY += summaryRowHeight
        
        // La ligne du bas est TOUJOURS l'équipe locale (Home)
        val homeName = roster.gameInfo.homeTeamName
        val homeRuns = if (roster.gameInfo.isHomeTeam) userRunsPerInning else oppRunsPerInning
        drawSummaryRow(canvas, margin, currentY, totalTableWidth, nameWidth + posWidth, inningWidth, homeName.uppercase(), homeRuns, paint, textPaint)

        pdfDocument.finishPage(page)

        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
                Toast.makeText(context, "Feuille de match PDF générée !", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawSummaryRow(
        canvas: Canvas, x: Float, y: Float, totalWidth: Float, labelWidth: Float, 
        inningWidth: Float, label: String, runs: List<Int>, paint: Paint, textPaint: Paint
    ) {
        val rowH = 25f
        paint.style = Paint.Style.STROKE
        canvas.drawRect(x, y, x + totalWidth, y + rowH, paint)
        textPaint.isFakeBoldText = true
        textPaint.textSize = 10f
        val labelX = x + labelWidth - textPaint.measureText(label) - 10f
        canvas.drawText(label, labelX, y + (rowH / 2) + 5f, textPaint)
        canvas.drawLine(x + labelWidth, y, x + labelWidth, y + rowH, paint)

        var cumulative = 0
        for (i in 0 until 9) {
            val cellX = x + labelWidth + (i * inningWidth)
            val r = if (i < runs.size) runs[i] else 0
            cumulative += r
            val scoreText = "$r / $cumulative"
            textPaint.isFakeBoldText = false
            textPaint.textSize = 9f
            val textW = textPaint.measureText(scoreText)
            canvas.drawText(scoreText, cellX + (inningWidth / 2) - (textW / 2), y + (rowH / 2) + 4f, textPaint)
            canvas.drawLine(cellX, y, cellX, y + rowH, paint)
        }
    }

    private fun drawDiamondResult(canvas: Canvas, events: List<AtBatEvent>, x: Float, y: Float, width: Float, height: Float) {
        // On cherche le résultat principal du passage au bâton
        val primaryEvent = events.lastOrNull { 
            it.result in listOf(BattingResult.Single, BattingResult.Double, BattingResult.Triple, BattingResult.HomeRun, BattingResult.Optionel, BattingResult.Strikeout) 
        } ?: events.last()

        val isRetiredOnOptionel = events.any { it.retiredOnOptionel }
        
        val diamondPadding = 6f
        val centerX = x + (width * 0.42f)
        val centerY = y + (height / 2f)
        val size = (height / 2.2f) - diamondPadding

        val paint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
            color = Color.BLACK
            isAntiAlias = true
        }

        val path = Path().apply {
            moveTo(centerX, centerY + size)
            lineTo(centerX + size, centerY)
            lineTo(centerX, centerY - size)
            lineTo(centerX - size, centerY)
            close()
        }
        canvas.drawPath(path, paint)

        // 1. Remplissage si point marqué (on le fait en premier)
        val maxBase = events.maxOf { it.finalBase }
        if (maxBase == 4) {
            val fillPaint = Paint().apply {
                style = Paint.Style.FILL
                color = Color.rgb(210, 210, 210)
                isAntiAlias = true
            }
            canvas.drawPath(path, fillPaint)
        }

        // 2. Tracé des sentiers (épais) - Dessiné après le remplissage pour être visible
        val pathPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2.4f // Un peu plus épais pour assurer la visibilité
            color = Color.BLACK
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        if (maxBase >= 1) {
            val runnerPath = Path()
            runnerPath.moveTo(centerX, centerY + size) // Marbre
            runnerPath.lineTo(centerX + size, centerY) // 1B
            if (maxBase >= 2) runnerPath.lineTo(centerX, centerY - size) // 2B
            if (maxBase >= 3) runnerPath.lineTo(centerX - size, centerY) // 3B
            if (maxBase >= 4) {
                runnerPath.lineTo(centerX - size, centerY) // Assure la 3B avant le retour
                runnerPath.lineTo(centerX, centerY + size) // Retour au Marbre (Point)
                runnerPath.close()
            }
            canvas.drawPath(runnerPath, pathPaint)
        }

        val textPaint = Paint().apply {
            textSize = 8f
            isFakeBoldText = true
            color = Color.BLACK
        }

        // 3. Résultat (R géant pour OUT, ou texte normal sinon)
        if (primaryEvent.result == BattingResult.Out) {
            val outRPaint = Paint().apply {
                textSize = 28f
                isFakeBoldText = false // Moins gras pour le PDF
                color = Color.BLACK
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("R", centerX, centerY + 10f, outRPaint)
        } else {
            val resultStr = getShortResult(primaryEvent.result)
            // On remonte le texte (y + height - 10f au lieu de - 6f) pour éclaircir
            canvas.drawText(resultStr, x + width - 16f, y + height - 10f, textPaint)
        }

        // 4. Ligne oblique de fin de manche (basée sur le marqueur isLastOfInning)
        val isEndOfInning = events.any { it.isLastOfInning }
        if (isEndOfInning) {
            val linePaint = Paint().apply {
                color = Color.BLACK
                strokeWidth = 2.5f
                isAntiAlias = true
            }
            // Ligne oblique nette dans le coin
            canvas.drawLine(x + width, y + height - 12f, x + width - 12f, y + height, linePaint)
        }

        // 5. Affichage du "R" discret si le joueur a été retiré par OPT (retrait forcé)
        if (isRetiredOnOptionel) {
            val rPaint = Paint().apply {
                textSize = 12f
                isFakeBoldText = true
                color = Color.BLACK // Demandé noir pour le PDF
                isAntiAlias = true
            }
            // Position en haut à gauche de la case (un peu en dehors du losange)
            canvas.drawText("R", x + 5f, y + 15f, rPaint)
        }

        val totalRbi = events.sumOf { it.rbi }
        if (totalRbi > 0) {
            // Cercle blanc avec bordure noire
            val rbiBgPaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL; isAntiAlias = true }
            val rbiStrokePaint = Paint().apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 0.5f; isAntiAlias = true }
            
            canvas.drawCircle(x + width - 8f, y + 8f, 5f, rbiBgPaint)
            canvas.drawCircle(x + width - 8f, y + 8f, 5f, rbiStrokePaint)
            
            textPaint.color = Color.BLACK
            textPaint.textSize = 6f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(totalRbi.toString(), x + width - 8f, y + 10.2f, textPaint)
            textPaint.textAlign = Paint.Align.LEFT // Reset
        }
    }

    private fun getShortResult(result: BattingResult): String {
        return when (result) {
            BattingResult.Single -> "1B"
            BattingResult.Double -> "2B"
            BattingResult.Triple -> "3B"
            BattingResult.HomeRun -> "CC"
            BattingResult.Strikeout -> "K"
            BattingResult.Out -> "R"
            BattingResult.Optionel -> "Opt"
        }
    }
}
