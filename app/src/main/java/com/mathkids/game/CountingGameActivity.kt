package com.mathkids.game

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class CountingGameActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var tvStars: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvItems: TextView
    private lateinit var tvFeedback: TextView
    private lateinit var gridAnswers: GridLayout
    private lateinit var btnNext: View

    private var score = 0
    private var currentQuestion = 0
    private var correctAnswer = 0
    private var answered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_counting_game)

        SoundManager.init(this)

        tvScore = findViewById(R.id.tv_score)
        tvStars = findViewById(R.id.tv_stars)
        tvQuestion = findViewById(R.id.tv_question)
        tvItems = findViewById(R.id.tv_items)
        tvFeedback = findViewById(R.id.tv_feedback)
        gridAnswers = findViewById(R.id.grid_answers)
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
        gridAnswers.removeAllViews()

        // Generate a random count between 1 and 10
        correctAnswer = (1..10).random()
        val emoji = GameUtils.EMOJI_SETS.random()

        // Display the emoji items
        val items = StringBuilder()
        for (i in 1..correctAnswer) {
            items.append(emoji)
            if (i % 5 == 0 && i < correctAnswer) items.append("\n")
            else items.append(" ")
        }
        tvItems.text = items.toString().trim()

        // Animate items appearing
        val popIn = AnimationUtils.loadAnimation(this, R.anim.pop_in)
        tvItems.startAnimation(popIn)

        // Create answer buttons (1-10)
        for (i in 1..10) {
            val btn = Button(this).apply {
                text = i.toString()
                textSize = 22f
                setTextColor(Color.WHITE)
                val colorRes = GameUtils.ANSWER_COLORS[i - 1]
                setBackgroundColor(ContextCompat.getColor(this@CountingGameActivity, colorRes))
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics
                    ).toInt()
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(4, 4, 4, 4)
                }
                layoutParams = params
                setOnClickListener { onAnswerClicked(i, this) }
            }
            gridAnswers.addView(btn)
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
            GameUtils.animateCorrect(tvItems)
            SoundManager.playCorrect()
        } else {
            tvFeedback.text = getString(R.string.try_again)
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.wrong_red))
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.wrong_red))
            // Highlight the correct answer
            for (i in 0 until gridAnswers.childCount) {
                val child = gridAnswers.getChildAt(i) as Button
                if (child.text == correctAnswer.toString()) {
                    child.setBackgroundColor(ContextCompat.getColor(this, R.color.correct_green))
                }
            }
            GameUtils.animateWrong(button)
            SoundManager.playWrong()
        }

        tvFeedback.visibility = View.VISIBLE
        updateScore()

        currentQuestion++
        if (currentQuestion >= GameUtils.TOTAL_QUESTIONS) {
            btnNext.postDelayed({
                GameUtils.showGameOverDialog(this, score, GameUtils.TOTAL_QUESTIONS, CountingGameActivity::class.java)
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
