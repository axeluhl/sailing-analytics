package com.sap.sailing.gwt.autoplay.client.app;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.IntegerSetting;

public class AutoplayPerspectiveOwnSettings extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -9013901094746556797L;

    BooleanSetting fullscreen;
    BooleanSetting switchToLive;
    IntegerSetting timeToSwitchBeforeRaceStart;
    IntegerSetting waitTimeAfterRaceEndInMillis;

    public AutoplayPerspectiveOwnSettings() {
    }

    public AutoplayPerspectiveOwnSettings(boolean fullscreen, boolean switchToLive, int timeToSwitchBeforeRaceStart, int waitTimeAfterRaceEndInMillis) {
        super();
        this.fullscreen.setValue(fullscreen);
        this.switchToLive.setValue(switchToLive);
        this.timeToSwitchBeforeRaceStart.setValue(timeToSwitchBeforeRaceStart);
        this.waitTimeAfterRaceEndInMillis.setValue(waitTimeAfterRaceEndInMillis);
    }

    @Override
    protected void addChildSettings() {
        fullscreen = new BooleanSetting("fullscreen", this, true);
        switchToLive = new BooleanSetting("switchToLive", this, true);
        timeToSwitchBeforeRaceStart = new IntegerSetting("timeToSwitchBeforeRaceStart", this, 180);
        waitTimeAfterRaceEndInMillis = new IntegerSetting("waitTimeAfterRaceEndInMillis", this, 60);

    }

    public boolean isFullscreen() {
        return fullscreen.getValue();
    }

    public boolean isSwitchToLive() {
        return switchToLive.getValue();
    }

    public int getTimeToSwitchBeforeRaceStart() {
        return timeToSwitchBeforeRaceStart.getValue();
    }

    public int getWaitTimeAfterRaceEndInMillis() {
        return waitTimeAfterRaceEndInMillis.getValue();
    }
}
