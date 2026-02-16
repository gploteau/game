package com.mathkids.game

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cardCounting = findViewById<MaterialCardView>(R.id.card_counting)
        val cardAddition = findViewById<MaterialCardView>(R.id.card_addition)
        val cardSubtraction = findViewById<MaterialCardView>(R.id.card_subtraction)
        val cardShapes = findViewById<MaterialCardView>(R.id.card_shapes)

        val bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce)

        cardCounting.setOnClickListener {
            it.startAnimation(bounceAnim)
            it.postDelayed({
                startActivity(Intent(this, CountingGameActivity::class.java))
            }, 300)
        }

        cardAddition.setOnClickListener {
            it.startAnimation(bounceAnim)
            it.postDelayed({
                startActivity(Intent(this, AdditionGameActivity::class.java))
            }, 300)
        }

        cardSubtraction.setOnClickListener {
            it.startAnimation(bounceAnim)
            it.postDelayed({
                startActivity(Intent(this, SubtractionGameActivity::class.java))
            }, 300)
        }

        cardShapes.setOnClickListener {
            it.startAnimation(bounceAnim)
            it.postDelayed({
                startActivity(Intent(this, ShapeGameActivity::class.java))
            }, 300)
        }

        // Animate cards on entry
        val popIn = AnimationUtils.loadAnimation(this, R.anim.pop_in)
        listOf(cardCounting, cardAddition, cardSubtraction, cardShapes).forEachIndexed { index, card ->
            card.alpha = 0f
            card.postDelayed({
                card.alpha = 1f
                card.startAnimation(popIn)
            }, (index * 150).toLong())
        }
    }
}
