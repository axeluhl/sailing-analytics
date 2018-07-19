package com.sap.sailing.racecommittee.app.ui.adapters;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class BaseDraggableSwipeAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>
    implements DraggableItemAdapter<T>, SwipeableItemAdapter<T> {

    protected boolean hitTest(View v, int x, int y) {
        int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        int left = v.getLeft() + tx;
        int right = v.getRight() + tx;
        int top = v.getTop() + ty;
        int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }
}
