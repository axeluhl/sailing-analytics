package com.sap.sailing.dashboards.gwt.client.notifications;

import com.sap.sailing.gwt.ui.client.StringMessages;

public enum BottomNotificationType {
    
    NEW_STARTANALYSIS_AVAILABLE(StringMessages.INSTANCE.dashboardNewStartAnalysisAvailable(), "#F0AB00", "#000000", 20000);
    
    private String message;
    private String backgroundColorAsHex;
    private String textColorAsHex;
    private int timeToDisappearInMilliseconds;

    private BottomNotificationType(String message, String backgroundColorAsHex, String textColorAsHex, int timeToDisappearInMilliseconds) {
        this.message = message;
        this.backgroundColorAsHex = backgroundColorAsHex;
        this.textColorAsHex = textColorAsHex;
        this.timeToDisappearInMilliseconds = timeToDisappearInMilliseconds;    
    }

    public String getMessage() {
        return message;
    }

    public String getBackgroundColorAsHex() {
        return backgroundColorAsHex;
    }

    public String getTextColorAsHex() {
        return textColorAsHex;
    }

    public int timeToDisappearInMilliseconds() {
        return timeToDisappearInMilliseconds;
    }
}
