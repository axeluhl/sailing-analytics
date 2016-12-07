package com.sap.sailing.android.shared.util;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

public class ViewHelper {

    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<View>();
            view.setTag(viewHolder);
        }

        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }

        return (T) childView;
    }

    public static void disableSave(View view) {
        view.setSaveFromParentEnabled(false);
        view.setSaveEnabled(false);
    }

    public static void setSiblingsVisibility(View view, int visibility) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            int max = parent.getChildCount();
            View child;
            for (int i = 0; i < max; i++) {
                child = parent.getChildAt(i);
                if (!child.equals(view)) {
                    child.setVisibility(visibility);
                }
            }
        }
    }
}
