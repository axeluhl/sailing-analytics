package com.sap.sailing.dashboards.gwt.client.bottomnotification;

public enum BottomNotificationType {
    
    NEW_STARTANALYSIS_AVAILABLE("New Start Analysis available.", "#F0AB00", "#000000", true);

    private String message;
    private String backgroundColorAsHex;
    private String textColorAsHex;
    private boolean shouldDisappearAfter20Seconds;

    private BottomNotificationType(String message, String backgroundColorAsHex, String textColorAsHex, boolean shouldDisappearAfter20Seconds) {
        this.message = message;
        this.backgroundColorAsHex = backgroundColorAsHex;
        this.textColorAsHex = textColorAsHex;
        this.shouldDisappearAfter20Seconds = shouldDisappearAfter20Seconds;
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

    public boolean isShouldDisappearAfter20Seconds() {
        return shouldDisappearAfter20Seconds;
    }
}
