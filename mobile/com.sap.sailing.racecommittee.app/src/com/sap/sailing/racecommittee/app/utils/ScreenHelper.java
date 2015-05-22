package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

public class ScreenHelper {

    private Context mContext;

    private ScreenHelper(Context context) {
        mContext = context;
    }

    public static ScreenHelper on(Context context) {
        ScreenHelper helper = new ScreenHelper(context);
        return helper;
    }

    private Point getScreenSize() {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public int getScreenHeight() {
        return getScreenSize().y;
    }

    public int getScreenWidth() {
        return getScreenSize().x;
    }
}
