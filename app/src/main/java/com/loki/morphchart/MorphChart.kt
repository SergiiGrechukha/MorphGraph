package com.loki.morphchart

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.properties.Delegates
import android.graphics.Shader
import android.graphics.LinearGradient



class MorphChart : View {

    private val DEFAULT_STRETCH_FACTOR = 0.05f
    private val DEFAULT_DENSITY_FACTOR = 20f
    private val DEFAULT_GRAPH_TYPE = GraphType.SIN
    private val DEFAULT_DEVIATION_RATIO = 0.1f
    private val DEFAULT_MORPH_RATIO = 2f

    private val ANIMATION_FPS = 12L

    private var layoutWidth: Int by Delegates.notNull<Int>()
    private var layoutHeight: Int by Delegates.notNull<Int>()
    private val graphPath = Path()
    private var graphPaint = Paint()

    private var densityFactor = DEFAULT_DENSITY_FACTOR
    private var stretchFactor = DEFAULT_STRETCH_FACTOR
    private var deviationRatio = DEFAULT_DEVIATION_RATIO
    private var graphType = DEFAULT_GRAPH_TYPE

    private var deviationMultiplication = ArrayList<Float>()
    private var morphMultiplication = ArrayList<Float>()
    private var animationHandler: Handler by Delegates.notNull<Handler>()

    private val random = Random()
    private var frames: Int = 1
    private var currentFrames: Int = 0

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        graphPaint.strokeWidth = 3f
        graphPaint.style = Paint.Style.STROKE
        graphPaint.flags = Paint.ANTI_ALIAS_FLAG
        this.animationHandler = Handler()
    }

    constructor(
            context: Context,
            densityFactor: Float,
            stretchFactor: Float,
            deviationRatio: Float,
            graphType: GraphType) : super(context, null) {
        this.densityFactor = densityFactor
        this.stretchFactor = stretchFactor
        this.deviationRatio = deviationRatio
        this.graphType = graphType
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        layoutWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        layoutHeight = View.MeasureSpec.getSize(heightMeasureSpec)

        morphMultiplication.clear()
        deviationMultiplication.clear()
        var currentStack = 0
        var currentDeviationFactor = 0f
        for (i in 0..layoutWidth) {
            if (currentStack == i) {
                currentStack += (layoutWidth * deviationRatio).toInt()
                currentDeviationFactor = (if (random.nextBoolean()) 1 else -1) * random.nextInt(100) * deviationRatio / 100
            }
            deviationMultiplication.add(currentDeviationFactor)
            morphMultiplication.add(1f)
        }

        setUpPaint()
    }

    private fun setUpPaint() {

        val gradientColors = intArrayOf(Color.RED, Color.YELLOW, Color.GREEN)
        val gradientPosition = floatArrayOf(0.33f, .66f, 1f)
        graphPaint.shader = SweepGradient((layoutWidth / 2).toFloat(), (layoutHeight / 2).toFloat(),
                gradientColors, gradientPosition)

        graphPaint.shader = LinearGradient(0f, 0f, layoutWidth.toFloat(), 0f, gradientColors, gradientPosition, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        graphPath.reset()

        for (i in 0..layoutWidth) {
            if (i == 0) {
                graphPath.moveTo(i.toFloat(), getFunctionOfX(i))
            }
            graphPath.lineTo(i.toFloat(), getFunctionOfX(i))
        }

        canvas?.drawPath(graphPath, graphPaint)

        if (currentFrames != 0) {
            handler.postDelayed(animationRunnable, ANIMATION_FPS)
        }
    }

    private fun getFunctionOfX(i: Int): Float {
        when (this.graphType) {
            GraphType.SIN -> return sin(i)
            GraphType.COS -> return cos(i)
        }
    }

    fun sin(value: Int): Float {
        val animationFactor = (1 - currentFrames.toFloat() / frames.toFloat())
        val A = layoutHeight * stretchFactor
        //get y from function
        var y = A * Math.sin(value.toDouble() / densityFactor).toFloat() * animationFactor
        //add morph ratio
        y -= Math.abs(y) * (1 - morphMultiplication[value])
        //translate into screen coordinates
        y = layoutHeight / 2 - y
        //add deviationRatio and animation
        return y * (1 - deviationMultiplication[value] * animationFactor)
    }

    fun cos(value: Int): Float {
        return layoutHeight / 2 - layoutHeight * stretchFactor * Math.cos((value / densityFactor).toDouble()).toFloat()
    }


    fun animate(time: Long, timeUnits: TimeUnit = TimeUnit.MILLISECONDS) {
        frames = (timeUnits.toMillis(time) / 1000 * 60).toInt()
        currentFrames = frames
        handler.post(animationRunnable)
    }

    fun morphSector(sector: Int,
                    ofSectors: Int,
                    morphUp: Boolean = true,
                    morphRatio: Float = DEFAULT_MORPH_RATIO,
                    animationTime: Long = android.R.integer.config_mediumAnimTime.toLong(),
                    timeUnits: TimeUnit = TimeUnit.MILLISECONDS) {
        val regionRange = layoutWidth / ofSectors
        val from = regionRange * sector
        val to = regionRange * (sector + 1)
        val middle = regionRange / 2 + from
        for (i in from..to) {
            var y = 1 + morphRatio * (1 - Math.pow(((middle - i).toFloat() * 2 / regionRange.toFloat()).toDouble(), 2.toDouble())).toFloat()
            y *= (if (morphUp) 1 else -1)
            morphMultiplication[i] = y
        }
//        animate(animationTime, timeUnits)
        invalidate()
        // y = morphRatio(1 - ((middle - i)/region)^2)
    }

    var animationRunnable = Runnable {
        currentFrames--
        if (currentFrames != 0) {
            invalidate()
        } else {
            frames = 0
        }
    }
}


enum class GraphType {
    SIN,
    COS
}
