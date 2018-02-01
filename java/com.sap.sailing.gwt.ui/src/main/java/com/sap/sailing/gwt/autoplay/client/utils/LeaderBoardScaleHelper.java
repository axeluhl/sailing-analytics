package com.sap.sailing.gwt.autoplay.client.utils;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.CSS3Util;

public class LeaderBoardScaleHelper {
    /**
     * Useable for 50% split designs, assumes the leaderboard is on the left side and has full height(apart from header)
     */
    public static void scaleContentWidget(int headerHeight, Widget contentWidget) {
        int clientWidth = Window.getClientWidth();
        int contentWidth = contentWidget.getOffsetWidth();
        double scaleFactorX = clientWidth / (double) contentWidth;
        // if(scaleFactorX > 1){
        // scaleFactorX = 1;
        // }
        
        int clientHeight = Window.getClientHeight()-headerHeight;
        int contentHeight = contentWidget.getOffsetHeight();
        double scaleFactorY = clientHeight / (double)contentHeight;
//        if(scaleFactorY < 1){
//            scaleFactorY = 1;
//        }
        
        scaleContentWidget(contentWidget, scaleFactorX,scaleFactorY);
    }

    private static void scaleContentWidget(Widget contentWidget, double scaleFactorX, double scaleFactorY) {
        CSS3Util.setProperty(contentWidget.getElement().getStyle(), "transform",
                "scale(" + scaleFactorX + "," + scaleFactorY + ")");
        CSS3Util.setProperty(contentWidget.getElement().getStyle(), "transformOrigin", "top left");
    }
}
