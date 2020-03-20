package com.sun.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by YH on 2017/10/10.
 */

public class RecyclerScrollView extends ScrollView {
    private int oldY;
    private int oldX;

    public RecyclerScrollView(Context context) {
        super(context);
    }

    public RecyclerScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 是否intercept当前的触摸事件
     *
     * @param ev 触摸事件
     * @return true：调用onMotionEvent()方法，并完成滑动操作
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                oldY = (int) ev.getY();
                oldX = (int) ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                int y = (int) ev.getY();
                int x = (int) ev.getX();
                return (Math.abs(y - oldY) > 30 && Math.abs(x - oldX) < 60);
        }
        return super.onInterceptTouchEvent(ev);
    }


}