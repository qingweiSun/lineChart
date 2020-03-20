package com.sun.myapplication

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import java.util.*
import kotlin.math.round

/**
 * 折线图，支持x,y轴 个数拓展
 * 支持最大值（注意结合y轴数据）,不然可能看起来是错的
 *
 */
internal class LineChart : LinearLayout {

    //四个方向的间距
    private val xStartOffset = dp2px(62f)
    private val xEndOffset = dp2px(34f)
    private val yOffsetEnd = dp2px(40f)
    private val yOffsetStart = dp2px(10f)

    //主题色
    var subjectColor = "FF4E25"
        set(value) {
            field = value.replace("#", "")
            invalidate()
        }


    //坐标系
    private val coordinatePaint = Paint().apply {
        color = Color.parseColor("#EEEEEE")
        style = Paint.Style.FILL;//充满
        strokeWidth = dp2px(1f)
        isAntiAlias = true;// 设置画笔的锯齿效果
    }

    //坐标系文字
    private val xTextPaint = Paint().apply {
        color = Color.parseColor("#999999")
        style = Paint.Style.FILL;//充满
        isAntiAlias = true;// 设置画笔的锯齿效果
        textSize = dp2px(10f)
    }

    //虚线
    private val dottedPaint = Paint().apply {
        reset()
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = dp2px(1f)
        color = Color.parseColor("#EEEEEE")
        val pathEffect: PathEffect = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
        this.pathEffect = pathEffect
    }

    //手势画笔
    private val gesturePaint = Paint().apply {
        reset()
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = dp2px(0.4f)
        this.pathEffect = null
    }

    //手势的x轴坐标
    private var gestureX = 0f;
    private var oldGestureX = 0f;

    //手势下标，默认最后一个
    private var gesturePos = 0;

    //数据源画笔
    private val dataPaint = Paint().apply {
        reset()
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.parseColor("#${subjectColor}")
        strokeWidth = dp2px(1f)
        this.pathEffect = null
    }

    //气泡画笔
    private val bubblePaint = Paint().apply {
        reset()
        isAntiAlias = true
        textSize = dp2px(14f)
        style = Paint.Style.FILL
    }

    private val shadowPaint = Paint()


    //横坐标数据
    private val xText = mutableListOf<String>().apply {
        add("02-26")
        add("02-27")
        add("02-28")
        add("02-29")
        add("03-01")
        add("03-02")
        add("03-03")
        add("03-04")
    }

    /**
     * x轴坐标显示数据
     */
    fun setXAxis(xAxis: MutableList<String>) {
        xText.clear()
        xText.addAll(xAxis)
        gesturePos = xText.size - 1
        invalidate()
    }

    //纵坐标坐标数据
    private val yText = mutableListOf<String>().apply {
        add("0")
        add("200")
        add("400")
        add("600")
        add("800")
        add("1000")
    }

    /**
     * y轴坐标显示数据
     */
    fun setYAxis(yAxis: MutableList<String>) {
        yText.clear()
        yText.addAll(yAxis)
        invalidate()
    }

    //坐标系内部的数据
    private var dataY = LinkedList<Float>()

    //原始数据,请保证和
    var yAxisData = LinkedList<Float>().apply {
        add(100f)
        add(300f)
        add(1000f)
        add(600f)
        add(510f)
        add(60f)
        add(100f)
        add(900f)
    }
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 设置y轴大值
     */
    var max = 1000f
        set(value) {
            field = value
            invalidate()
        }
    private var yItemHeight = 0f
    private var xItemWidth = 0f;


    //虚线
    private val dottedPath = Path()

    //数据线
    private val dataPath = Path()

    //三角形
    private val mBubbleLegPrototype = Path()

    //渐变色，闭合
    private val shadowPath = Path()

    private var liGradient: LinearGradient? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
            context,
            attrs
    ) {
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        yItemHeight = (height - yOffsetEnd - yOffsetStart) / (yText.size)
        xItemWidth = (width - xEndOffset - xStartOffset) / (xText.size - 1)
        oldGestureX = width - xEndOffset;
        if (yAxisData.isNotEmpty()) {
            dataY.clear()
            yAxisData.forEach {
                dataY.add(height - (it / max) * (yText.size - 1) * yItemHeight - yOffsetEnd)
            }
        }
        gesturePaint.color = Color.parseColor("#80${subjectColor}")
        dataPaint.color = Color.parseColor("#${subjectColor}")
    }

    constructor(
            context: Context?,
            attrs: AttributeSet?,
            defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        //坐标系
        canvas?.drawLine(
                xStartOffset,
                height - yOffsetEnd,
                width.toFloat() - xEndOffset,
                height - yOffsetEnd,
                coordinatePaint
        )
        //虚线
        dottedPath.reset()
        for (i in 0 until yText.size) {
            dottedPath.moveTo(xStartOffset, height - yOffsetEnd - (i * yItemHeight))
            dottedPath.lineTo(width.toFloat() - xEndOffset, height - yOffsetEnd - i * yItemHeight)
        }
        dottedPath.close()
        canvas?.drawPath(dottedPath, dottedPaint)
        //x轴数据
        xText.forEachIndexed { index, s ->
            measureTextHW(xTextPaint, s) { textWidth, textHeight ->
                canvas?.drawText(
                        s,
                        xStartOffset + xItemWidth * index - textWidth / 2,
                        height - yOffsetEnd + textHeight + dp2px(11f),
                        xTextPaint
                )
            }
        }
        //y轴数据
        yText.forEachIndexed { index, s ->
            measureTextHW(xTextPaint, s) { textWidth, textHeight ->
                canvas?.drawText(
                        s,
                        xStartOffset - dp2px(18f) - textWidth,
                        height - yOffsetEnd - yItemHeight * index + textHeight / 2,
                        xTextPaint
                )
            }
        }
        shadowPath.reset()
        shadowPath.moveTo(xStartOffset, height - yOffsetEnd)
        dataY.forEachIndexed { index, fl ->
            if (index == 0) {
                dataPath.moveTo(xStartOffset + index * xItemWidth, fl)
            } else {
                dataPath.lineTo(xStartOffset + index * xItemWidth, fl)
            }
            shadowPath.lineTo(xStartOffset + index * xItemWidth, fl)
        }
        shadowPath.lineTo(xStartOffset + (dataY.size - 1) * xItemWidth, height - yOffsetEnd)
        shadowPath.close()
        canvas?.drawPath(dataPath, dataPaint)
        //绘制渐变的颜色
        if (liGradient == null) {
            shadowPaint.shader = LinearGradient(
                    0f,
                    0f,
                    0f,
                    height - yOffsetEnd,
                    Color.parseColor("#55${subjectColor}"),
                    Color.parseColor("#00${subjectColor}"),
                    Shader.TileMode.REPEAT
            ).apply {
                liGradient = this
            }
        }
        canvas?.drawPath(shadowPath, shadowPaint);

        if (gestureX >= xStartOffset) {
            //画手势放上去的竖线
            canvas?.drawLine(
                    gestureX,
                    height - yOffsetEnd,
                    gestureX,
                    0f + yOffsetStart + yItemHeight,
                    gesturePaint
            );
        }
        if (gesturePos in 0 until xText.size) {
            addBubble(canvas)
        }
    }

    /**
     * 添加气泡
     */
    private fun addBubble(canvas: Canvas?) {
        if (dataY.isNotEmpty() && gesturePos in 0 until dataY.size) {
            //上下间距
            val yBubbleGap = dp2px(18f)
            //要 显示数据,拿到y轴的位置
            val yPoint = dataY[gesturePos]
            val text = "${yAxisData[gesturePos]}"
            bubblePaint.color = Color.parseColor("#80000000")
            measureTextHW(bubblePaint, text) { textWidth, textHeight ->
                canvas?.drawRoundRect(
                        oldGestureX - textWidth / 2 - dp2px(6f),
                        yPoint - yBubbleGap - textHeight - dp2px(7f),
                        oldGestureX + textWidth / 2 + dp2px(8f),
                        yPoint - yBubbleGap + dp2px(7f),
                        dp2px(2f),
                        dp2px(2f),
                        bubblePaint
                )
                //绘制三角形
                mBubbleLegPrototype.reset()
                mBubbleLegPrototype.moveTo(
                        oldGestureX - dp2px(3f),
                        yPoint - yBubbleGap + dp2px(7f)
                );
                mBubbleLegPrototype.lineTo(
                        oldGestureX + dp2px(3f),
                        yPoint - yBubbleGap + dp2px(7f)
                );
                mBubbleLegPrototype.lineTo(oldGestureX, yPoint - dp2px(7f));
                mBubbleLegPrototype.close();
                canvas?.drawPath(mBubbleLegPrototype, bubblePaint)
                //绘制文字
                bubblePaint.color = Color.parseColor("#FFFFFF")
                canvas?.drawText(
                        text,
                        oldGestureX - textWidth / 2,
                        yPoint - yBubbleGap,
                        bubblePaint
                )
            }
        }

    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("LineChart", "dispatchTouchEvent->ACTION_DOWN")
                getXCoordinate(event.x)
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d("LineChart", "dispatchTouchEvent->ACTION_MOVE")
                getXCoordinate(event.x)
            }
            MotionEvent.ACTION_UP -> {
                Log.d("LineChart", "dispatchTouchEvent->ACTION_UP")
                dismissGestureLine()
            }
            MotionEvent.ACTION_CANCEL -> {
                Log.d("LineChart", "dispatchTouchEvent->ACTION_CANCEL")
                dismissGestureLine()
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    /**
     * 消失手势线
     */
    private fun dismissGestureLine() {
        postDelayed({
            gestureX = -1f;
            invalidate()
        }, 200)
    }

    /**
     * 获取临近的x轴坐标
     */
    private fun getXCoordinate(x: Float) {
        //触摸发生在坐标系内
        if (x >= xStartOffset && x <= width - xEndOffset) {
            //获取坐标系坐标(触摸x坐标-开始坐标) /间隔大小
            val newGesturePos = round((x - xStartOffset) / xItemWidth.toDouble()).toInt();
            if (newGesturePos != gesturePos) {
                gesturePos = newGesturePos
                gestureX = gesturePos * xItemWidth + xStartOffset
                oldGestureX = gestureX
                invalidate()
            }
        }
    }

    private fun dp2px(dpValue: Float): Float {
        return (0.5f + dpValue * Resources.getSystem().displayMetrics.density)
    }

    /**
     * 测量文字的高度和宽度
     * @param paint
     * @param textTemp
     * @return
     */
    private fun measureTextHW(
            paint: Paint,
            textTemp: String,
            resData: (width: Float, height: Float) -> Unit
    ) {
        val rectFont = Rect()
        paint.getTextBounds(textTemp, 0, textTemp.length, rectFont)
        resData(rectFont.width().toFloat(), rectFont.height().toFloat())
    }
}