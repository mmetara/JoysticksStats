package com.joysticks.stats.utils

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.graphics.Paint
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.joysticks.stats.engine.GameState
import com.joysticks.stats.engine.Roster
import com.joysticks.stats.ui.screens.StatsSheetScreen
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference

object PdfExportUtils {

    @RequiresApi(Build.VERSION_CODES.KITKAT) // PdfDocument nécessite API 19
    fun exportStatsSheetToPdf(context: Context, uri: Uri, gameState: GameState, roster: Roster?) {
        if (roster == null) {
            Toast.makeText(context, "Le roster n'est pas chargé pour l'exportation PDF.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentRoster = roster // Smart-cast to non-nullable Roster

        val pdfDocument = PdfDocument()
        val pageInfo = PageInfo.Builder(842, 595, 1).create() // A4 Landscape (595x842 portrait) => 842x595 landscape
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Pour dessiner sur le PDF, nous devons rendre le composable sur un Canvas Android.
        // Cela ne peut pas être fait directement dans une fonction utilitaire statique sans un environnement Compose.
        // La méthode la plus simple est de rendre le composable hors écran dans une View,
        // puis de dessiner cette View sur le Canvas du PDF.
        // Cependant, cela est complexe et demande un contexte de View ou d'Activity.

        // Une approche plus simple (mais moins flexible) est de dessiner manuellement les éléments du tableau.
        // Ou, si nous voulons rendre le Composable, nous devrions être dans un Composable lui-même
        // ou utiliser un mécanisme de rendu de Composable à une bitmap/Canvas.

        // Pour l'exemple ici, je vais simuler le dessin avec Paint.
        // Rendre un composable sur un Canvas PDF est *très* complexe sans un outil dédié.
        // Pour un rendu WYSIWYG de Compose, il faudrait capturer le rendu du composable en bitmap
        // puis dessiner cette bitmap sur le canvas PDF, ce qui est une autre complexité.

        // Simplement pour que le PDF ne soit pas vide, nous allons dessiner un texte.
        val paint = Paint().apply {
            textSize = 24f
            color = android.graphics.Color.BLACK
        }
        canvas.drawText("Feuille de Statistiques JoysticksStats", 40f, 40f, paint)
        canvas.drawText("Version PDF (simplifiée)", 40f, 80f, paint)

        // Exemple simple de dessin des scores (vous devrez adapter cela pour tout le tableau)
        var yOffset = 120f
        val textPaint = Paint().apply { textSize = 16f; color = android.graphics.Color.DKGRAY }

        canvas.drawText("Scores par manche:", 40f, yOffset, paint)
        yOffset += 30f

        // Boucler sur les manches pour les scores (similaire à InningScoreSummaryRow)
        var runningTotalUser = 0
        var runningTotalOpponent = 0

        val userRunsPerInning = (1..currentRoster.gameInfo.maxInnings).associateWith { inning ->
            gameState.gameHistory.count { event ->
                event.inning == inning && event.finalBase == 4 && event.isHomeTeam == !gameState.isTop
            }
        }

        val opponentRunsPerInning = gameState.opponentRunsPerInning

        for (inningNum in 1..currentRoster.gameInfo.maxInnings) {
            val userRuns = userRunsPerInning[inningNum] ?: 0
            val opponentRuns = opponentRunsPerInning[inningNum] ?: 0

            runningTotalUser += userRuns
            runningTotalOpponent += opponentRuns

            canvas.drawText("Manche $inningNum:", 60f, yOffset, textPaint)
            canvas.drawText("  Total: $userRuns / $runningTotalUser", 180f, yOffset, textPaint)
            canvas.drawText("  Adv.: $opponentRuns / $runningTotalOpponent", 380f, yOffset, textPaint)
            yOffset += 20f
        }


        pdfDocument.finishPage(page)

        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
                Toast.makeText(context, "Feuille de statistiques exportée en PDF !", Toast.LENGTH_LONG).show()
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Erreur d'exportation PDF: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }
}
