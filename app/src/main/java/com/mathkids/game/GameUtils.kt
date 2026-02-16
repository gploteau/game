package com.mathkids.game

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView

object GameUtils {

    const val TOTAL_QUESTIONS = 10

    // Emoji items for visual counting
    val EMOJI_SETS = listOf(
        "🍎", "🌟", "🐱", "🦋", "🌸",
        "🐶", "🍓", "🎈", "🐠", "🌈",
        "🍒", "🐝", "🦄", "🍕", "⚽"
    )

    fun getStarsDisplay(score: Int): String {
        return "⭐".repeat(score)
    }

    fun showGameOverDialog(
        context: Context,
        score: Int,
        total: Int,
        replayClass: Class<*>
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_game_over, null)
        val dialog = AlertDialog.Builder(context, R.style.Theme_MathKids)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.findViewById<TextView>(R.id.tv_final_score).text =
            context.getString(R.string.game_over_message, score, total)
        dialogView.findViewById<TextView>(R.id.tv_final_stars).text =
            getStarsDisplay(score)

        dialogView.findViewById<Button>(R.id.btn_play_again).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(context, replayClass)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            if (context is android.app.Activity) context.finish()
        }

        dialogView.findViewById<Button>(R.id.btn_menu).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            if (context is android.app.Activity) context.finish()
        }

        dialog.show()
        SoundManager.playSuccess()
    }

    fun animateCorrect(view: View) {
        val anim = AnimationUtils.loadAnimation(view.context, R.anim.pulse)
        view.startAnimation(anim)
    }

    fun animateWrong(view: View) {
        val anim = AnimationUtils.loadAnimation(view.context, R.anim.shake)
        view.startAnimation(anim)
    }

    val ANSWER_COLORS = listOf(
        R.color.button_blue,
        R.color.button_purple,
        R.color.button_pink,
        R.color.button_orange,
        R.color.card_counting,
        R.color.card_addition,
        R.color.card_subtraction,
        R.color.card_shapes,
        R.color.primary,
        R.color.star_gold
    )
}
