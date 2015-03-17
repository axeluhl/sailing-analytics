package com.sap.sailing.gwt.home.client.place.event.utils;

import com.google.gwt.user.client.Window;

public class WindowUtils extends EventParamUtils {

    public static boolean isSmallWidth() {
        int width = Window.getClientWidth();
        return width <= 720;
    }

}
