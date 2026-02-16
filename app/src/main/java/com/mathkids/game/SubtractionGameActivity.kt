package com.mathkids.game

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SubtractionGameActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var tvStars: TextView
    private lateinit var tvExpression: TextView
    private lateinit var tvVisual: TextView
    private lateinit var tvFeedback: TextView
    private lateinit var layoutAnswers: LinearLayout
    private lateinit var btnNext: View

    private var score = 0
    private var currentQuestion = 0
    private var correctAnswer = 0
    private var answered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_math_game)

        SoundManager.init(this)

        tvScore = findViewById(R.id.tv_score)
        tvStars = findViewById(R.id.tv_stars)
        tvExpression = findViewById(R.id.tv_expression)
        tvVisual = findViewById(R.id.tv_visual)
        tvFeedback = findViewById(R.id.tv_feedback)
        layoutAnswers = findViewById(R.id.layout_answers)
        btnNext = findViewById(R.id.btn_next)

        findViewById<Button>(R.id.btn_back).setOnClickListener { finish() }
        btnNext.setOnClickListener { nextQuestion() }

        updateScore()
        generateQuestion()
    }

    private fun updateScore() {
        tvScore.text = getString(R.string.score_label, score)
        tvStars.text = GameUtils.getStarsDisplay(score)
    }

    private fun generateQuestion() {
        answered = false
        tvFeedback.visibility = View.INVISIBLE
        btnNext.visibility = View.INVISIBLE
        layoutAnswers.removeAllViews()

        // Generate subtraction where a >= b and both are small
        val a = (3..10).random()
        val b = (1..a).random()
        correctAnswer = a - b

        tvExpression.text = "$a - $b = ?"

        // Visual: show 'a' items with 'b' crossed out
        val emoji = GameUtils.EMOJI_SETS.random()
        val remaining = emoji.repeat(a - b)
        val removed = "❌".repeat(b)
        tvVisual.text = "$remaining $removed"

        val popIn = AnimationUtils.loadAnimation(this, R.anim.pop_in)
        tvExpression.startAnimation(popIn)

        // Generate 4 answer choices
        val answers = mutableSetOf(correctAnswer)
        while (answers.size < 4) {
            val wrong = (0..9).random()
            if (wrong != correctAnswer) answers.add(wrong)
        }

        val shuffled = answers.toList().shuffled()
        val colors = GameUtils.ANSWER_COLORS.shuffled()

        shuffled.forEachIndexed { index, answer ->
            val btn = Button(this).apply {
                text = answer.toString()
                textSize = 28f
                setTextColor(Color.WHITE)
                setBackgroundColor(ContextCompat.getColor(this@SubtractionGameActivity, colors[index]))
                val params = LinearLayout.LayoutParams(
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72f, resources.displayMetrics).toInt(),
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72f, resources.displayMetrics).toInt()
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                layoutParams = params
                setOnClickListener { onAnswerClicked(answer, this) }
            }
            layoutAnswers.addView(btn)
        }
    }

    private fun onAnswerClicked(answer: Int, button: Button) {
        if (answered) return
        answered = true

        if (answer == correctAnswer) {
            score++
            tvFeedback.text = getString(R.string.bravo)
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.correct_green))
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.correct_green))
            GameUtils.animateCorrect(tvFeedback)
            GameUtils.animateCorrect(tvExpression)
            SoundManager.playCorrect()

            val parts = tvExpression.text.toString().split(" - ")
            val a = parts[0].toInt()
            val b = parts[1].split(" = ")[0].toInt()
            tvExpression.text = "$a - $b = $correctAnswer"
        } else {
            tvFeedback.text = getString(R.string.try_again)
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.wrong_red))
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.wrong_red))
            for (i in 0 until layoutAnswers.childCount) {
                val child = layoutAnswers.getChildAt(i) as Button
                if (child.text == correctAnswer.toString()) {
                    child.setBackgroundColor(ContextCompat.getColor(this, R.color.correct_green))
                }
            }
            GameUtils.animateWrong(button)
            SoundManager.playWrong()

            val parts = tvExpression.text.toString().split(" - ")
            val a = parts[0].toInt()
            val b = parts[1].split(" = ")[0].toInt()
            tvExpression.text = "$a - $b = $correctAnswer"
        }

        tvFeedback.visibility = View.VISIBLE
        updateScore()

        currentQuestion++
        if (currentQuestion >= GameUtils.TOTAL_QUESTIONS) {
            btnNext.postDelayed({
                GameUtils.showGameOverDialog(this, score, GameUtils.TOTAL_QUESTIONS, SubtractionGameActivity::class.java)
            }, 1500)
        } else {
            btnNext.visibility = View.VISIBLE
            val bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce)
            btnNext.startAnimation(bounceAnim)
        }
    }

    private fun nextQuestion() {
        SoundManager.playClick()
        generateQuestion()
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundManager.release()
    }
}
