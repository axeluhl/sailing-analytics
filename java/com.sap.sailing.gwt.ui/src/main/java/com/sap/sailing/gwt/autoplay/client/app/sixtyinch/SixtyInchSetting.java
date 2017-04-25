package com.sap.sailing.gwt.autoplay.client.app.sixtyinch;

import java.util.UUID;

import com.sap.sailing.gwt.autoplay.client.app.AutoPlaySettings;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public class SixtyInchSetting extends AbstractGenericSerializableSettings implements AutoPlaySettings {
    private static final long serialVersionUID = -5170519954446053233L;

    private UUIDSetting eventId;
    private StringSetting leaderboardName;

    public SixtyInchSetting(UUID eventUuid, String selectedLeaderboardName) {
        eventId.setValue(eventUuid);
        leaderboardName.setValue(selectedLeaderboardName);
    }

    public SixtyInchSetting() {
    }

    @Override
    protected void addChildSettings() {
        this.eventId = new UUIDSetting("eventUuid", this);
        this.leaderboardName = new StringSetting("name", this);
    }

    public UUID getEventId() {
        return eventId.getValue();
    }

    @Override
    public String getLeaderboardName() {
        return leaderboardName.getValue();
    }

}

