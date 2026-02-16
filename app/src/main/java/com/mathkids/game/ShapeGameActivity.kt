package com.mathkids.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ShapeGameActivity : AppCompatActivity() {

    private lateinit var tvScore: TextView
    private lateinit var tvStars: TextView
    private lateinit var tvFeedback: TextView
    private lateinit var gridAnswers: GridLayout
    private lateinit var btnNext: View
    private lateinit var shapeContainer: FrameLayout

    private var score = 0
    private var currentQuestion = 0
    private var correctAnswer = ""
    private var answered = false

    data class Shape(
        val name: String,
        val nameFr: String,
        val color: Int,
        val drawFunction: (Canvas, Paint, Float, Float) -> Unit
    )

    private val shapes = listOf(
        Shape("Cercle", "Cercle", Color.parseColor("#EF4444")) { canvas, paint, w, h ->
            paint.color = Color.parseColor("#EF4444")
            canvas.drawCircle(w / 2, h / 2, minOf(w, h) / 2.5f, paint)
        },
        Shape("Carré", "Carré", Color.parseColor("#3B82F6")) { canvas, paint, w, h ->
            paint.color = Color.parseColor("#3B82F6")
            val size = minOf(w, h) / 2.5f
            canvas.drawRect(w / 2 - size, h / 2 - size, w / 2 + size, h / 2 + size, paint)
        },
        Shape("Triangle", "Triangle", Color.parseColor("#22C55E")) { canvas, paint, w, h ->
            paint.color = Color.parseColor("#22C55E")
            val path = Path()
            val size = minOf(w, h) / 2.5f
            path.moveTo(w / 2, h / 2 - size)
            path.lineTo(w / 2 + size, h / 2 + size)
            path.lineTo(w / 2 - size, h / 2 + size)
            path.close()
            canvas.drawPath(path, paint)
        },
        Shape("Rectangle", "Rectangle", Color.parseColor("#F59E0B")) { canvas, paint, w, h ->
            paint.color = Color.parseColor("#F59E0B")
            val sizeW = minOf(w, h) / 2f
            val sizeH = minOf(w, h) / 3.5f
            canvas.drawRect(w / 2 - sizeW, h / 2 - sizeH, w / 2 + sizeW, h / 2 + sizeH, paint)
        },
        Shape("Étoile", "Étoile", Color.parseColor("#8B5CF6")) { canvas, paint, w, h ->
            paint.color = Color.parseColor("#8B5CF6")
            val path = Path()
            val cx = w / 2
            val cy = h / 2
            val outerR = minOf(w, h) / 2.5f
            val innerR = outerR / 2.5f
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = Math.toRadians((i * 36 - 90).toDouble())
                val x = cx + (r * Math.cos(angle)).toFloat()
                val y = cy + (r * Math.sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            canvas.drawPath(path, paint)
        },
        Shape("Losange", "Losange", Color.parseColor("#EC4899")) { canvas, paint, w, h ->
            paint.color = Color.parseColor("#EC4899")
            val path = Path()
            val sizeX = minOf(w, h) / 3f
            val sizeY = minOf(w, h) / 2.2f
            path.moveTo(w / 2, h / 2 - sizeY)
            path.lineTo(w / 2 + sizeX, h / 2)
            path.lineTo(w / 2, h / 2 + sizeY)
            path.lineTo(w / 2 - sizeX, h / 2)
            path.close()
            canvas.drawPath(path, paint)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shape_game)

        SoundManager.init(this)

        tvScore = findViewById(R.id.tv_score)
        tvStars = findViewById(R.id.tv_stars)
        tvFeedback = findViewById(R.id.tv_feedback)
        gridAnswers = findViewById(R.id.grid_answers)
        btnNext = findViewById(R.id.btn_next)
        shapeContainer = findViewById<View>(R.id.shape_view).parent as FrameLayout

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

        val shape = shapes.random()
        correctAnswer = shape.nameFr

        // Draw the shape
        shapeContainer.removeAllViews()
        val shapeView = ShapeDrawView(this, shape)
        shapeContainer.addView(shapeView)

        val popIn = AnimationUtils.loadAnimation(this, R.anim.pop_in)
        shapeContainer.startAnimation(popIn)

        // Generate 4 answer choices
        val answerShapes = mutableListOf(shape)
        val otherShapes = shapes.filter { it.nameFr != shape.nameFr }.shuffled()
        answerShapes.addAll(otherShapes.take(3))
        val shuffled = answerShapes.shuffled()

        val colors = listOf(
            R.color.button_blue, R.color.button_purple,
            R.color.button_pink, R.color.button_orange
        )

        shuffled.forEachIndexed { index, s ->
            val btn = Button(this).apply {
                text = s.nameFr
                textSize = 18f
                setTextColor(Color.WHITE)
                setBackgroundColor(ContextCompat.getColor(this@ShapeGameActivity, colors[index]))
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 56f, resources.displayMetrics
                    ).toInt()
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(6, 6, 6, 6)
                }
                layoutParams = params
                setOnClickListener { onAnswerClicked(s.nameFr, this) }
            }
            gridAnswers.addView(btn)
        }
    }

    private fun onAnswerClicked(answer: String, button: Button) {
        if (answered) return
        answered = true

        if (answer == correctAnswer) {
            score++
            tvFeedback.text = getString(R.string.bravo)
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.correct_green))
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.correct_green))
            GameUtils.animateCorrect(tvFeedback)
            GameUtils.animateCorrect(shapeContainer)
            SoundManager.playCorrect()
        } else {
            tvFeedback.text = getString(R.string.try_again)
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.wrong_red))
            button.setBackgroundColor(ContextCompat.getColor(this, R.color.wrong_red))
            for (i in 0 until gridAnswers.childCount) {
                val child = gridAnswers.getChildAt(i) as Button
                if (child.text == correctAnswer) {
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
                GameUtils.showGameOverDialog(this, score, GameUtils.TOTAL_QUESTIONS, ShapeGameActivity::class.java)
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

    /**
     * Custom view that draws a geometric shape.
     */
    private class ShapeDrawView(context: Context, private val shape: Shape) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            shape.drawFunction(canvas, paint, width.toFloat(), height.toFloat())
        }
    }
}
