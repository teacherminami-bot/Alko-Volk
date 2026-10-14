package com.forestwolf.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.sin

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {
    private var isPlaying = false
    private var gameThread: Thread? = null

    // Размеры экрана и камера
    private var screenWidth = 1080f
    private var screenHeight = 1920f
    private var cameraX = 0f
    private var cameraY = 0f
    private var gravity = 1.4f

    // Главный герой: Волк в шортах и дл. носках
    private var wolfX = 120f
    private var wolfY = 300f
    private var wolfVX = 300f
    private var wolfVY = 110f
    private var wolfVy = 0f
    private var facingRight = true
    private var isJumping = false
    private var moveLeft = false
    private var moveRight = false

    // Статистика и запасы газа (от скутера)
    private var score = 0
    private var lives = 3
    private var gasMeter = 100 // Максимум 100
    private var levelCompleted = false
    private var levelNum = 1

    // Типы ящиков: 0 – Сине-белая банка; 1 – Сыр Камамбер (бафф здоровья); 2 – Пачка чипсов (очки + ускорение); 3 – Сушеная рыбка (газ + бонус); 4 – Вороная пышка (секрет); 5 – Корона принцессы (финиш)
    data class Snack(var x: Float, var y: Float, var type: Int, var collected: Boolean = false)
    private var snacks = mutableListOf<Snack>()

    // Препятствия и облака
    data class Cloud(var x: Float, var y: Float, var size: Float)
    private var clouds = mutableListOf<Cloud>()

    private val paint = Paint()
    private var btnLeft = RectF()
    private var btnRight = RectF()
    private var btnJump = RectF()
    private var btnBurp = RectF() // Спец-кнопка РЫГАНУТЬ

    private var burpCooldown = 0f
    private var burpActive = false

    init {
        holder.addCallback(this)
        isFocusable = true
        initLevel()
    }

    private fun initLevel() {
        snacks.clear()
        clouds.clear()
        // Раскидываем облака на фоне
        for (i in 0..15) {
            clouds.add(Cloud((i * 400).toFloat(), (Math.random() * 800).toFloat(), (100 + Math.random() * 200).toFloat()))
        }

        // Спавним ящики со снеками по уровню
        for (i in 1..25) {
            val type = if (i == 25) 5 else (0..4).random()
            snacks.add(Snack((i * 350).toFloat(), (600 + Math.random() * 400).toFloat(), type))
        }
        levelCompleted = false
        wolfX = 120f
        wolfY = 600f
        wolfVy = 0f
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isPlaying = true
        gameThread = Thread(this)
        gameThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width
