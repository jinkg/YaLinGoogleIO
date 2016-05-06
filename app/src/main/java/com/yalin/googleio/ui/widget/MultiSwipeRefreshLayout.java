package com.yalin.googleio.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.yalin.googleio.R;

/**
 * 作者：YaLin
 * 日期：2016/5/6.
 */
public class MultiSwipeRefreshLayout extends SwipeRefreshLayout {
    private CanChildScrollUpCallback mCanChildScrollUpCallback;

    private Drawable mFroegroundDrawable;

    public MultiSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public MultiSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiSwipeRefreshLayout, 0, 0);
        mFroegroundDrawable = a.getDrawable(R.styleable.MultiSwipeRefreshLayout_foreground);
        if (mFroegroundDrawable != null) {
            mFroegroundDrawable.setCallback(this);
            setWillNotDraw(false);
        }
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mFroegroundDrawable != null) {
            mFroegroundDrawable.setBounds(0, 0, w, h);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mFroegroundDrawable != null) {
            mFroegroundDrawable.draw(canvas);
        }
    }

    @Override
    public boolean canChildScrollUp() {
        if (mCanChildScrollUpCallback != null) {
            return mCanChildScrollUpCallback.canSwipeRefreshChildScrollUp();
        }
        return super.canChildScrollUp();
    }

    public void setCanChildScrollUpCallback(CanChildScrollUpCallback canChildScrollUpCallback) {
        mCanChildScrollUpCallback = canChildScrollUpCallback;
    }

    public interface CanChildScrollUpCallback {
        boolean canSwipeRefreshChildScrollUp();
    }
}
