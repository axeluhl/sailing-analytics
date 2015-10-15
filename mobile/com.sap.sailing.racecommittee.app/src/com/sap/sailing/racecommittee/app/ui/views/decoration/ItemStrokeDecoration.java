package com.sap.sailing.racecommittee.app.ui.views.decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ItemStrokeDecoration extends PaddingItemDecoration {

    private Paint mLinePaint;

    public ItemStrokeDecoration(int padding, float strokeWidth, int color) {
        super(padding);

        mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(strokeWidth);
        mLinePaint.setColor(color);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        for (int i = 0; i < parent.getChildCount(); i++) {
            final View child = parent.getChildAt(i);
            c.drawRect(
                layoutManager.getDecoratedLeft(child) + mPadding,
                layoutManager.getDecoratedTop(child) + mPadding,
                layoutManager.getDecoratedRight(child) - mPadding, layoutManager.getDecoratedBottom(child) - mPadding, mLinePaint);
        }
    }
}
