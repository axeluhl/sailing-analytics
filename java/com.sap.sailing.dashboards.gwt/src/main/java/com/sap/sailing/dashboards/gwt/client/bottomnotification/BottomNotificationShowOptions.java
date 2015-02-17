package com.sap.sailing.dashboards.gwt.client.bottomnotification;

public class BottomNotificationShowOptions {
    
    private String message;
    private String backgroundColorAsHex;
    private String textColorAsHex;
    private boolean shouldDisappearAfter20Seconds;
    
    public BottomNotificationShowOptions(String message, String backgroundColorAsHex, String textColorAsHex,
            boolean shouldDisappearAfter20Seconds) {
        super();
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
