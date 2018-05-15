package com.sap.sailing.gwt.autoplay.client.utils;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.CSS3Util;

public class LeaderBoardScaleHelper {
    /**
     * Useable for 50% split designs, assumes the leaderboard is on the left side and has full height(apart from header)
     */
    public static void scaleContentWidget(int headerHeight, Widget contentWidget, boolean independent) {
        int clientWidth = Window.getClientWidth();
        int contentWidth = contentWidget.getOffsetWidth();
        double scaleFactorX = clientWidth / (double) contentWidth;

        int clientHeight = Window.getClientHeight()-headerHeight;
        int contentHeight = contentWidget.getOffsetHeight();
        double scaleFactorY = clientHeight / (double)contentHeight;
        
        if(!independent) {
            scaleFactorX = Math.min(scaleFactorX, scaleFactorY);
            scaleFactorY = Math.min(scaleFactorX, scaleFactorY);
        }
        
        scaleContentWidget(contentWidget, scaleFactorX,scaleFactorY);
    }

    private static void scaleContentWidget(Widget contentWidget, double scaleFactorX, double scaleFactorY) {
        CSS3Util.setProperty(contentWidget.getElement().getStyle(), "transform",
                "scale(" + scaleFactorX + "," + scaleFactorY + ")");
        CSS3Util.setProperty(contentWidget.getElement().getStyle(), "transformOrigin", "top left");
    }
}
