package com.sap.sailing.gwt.autoplay.client.app.classic;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public class ClassicSetting extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 2880854263616658272L;

    private transient UUIDSetting eventUUID;
    private transient StringSetting leaderboardName;

    public ClassicSetting(UUID eventUUID, String leaderboardName) {
        this.eventUUID.setValue(eventUUID);
        this.leaderboardName.setValue(leaderboardName);
    }

    public ClassicSetting() {
    }

    @Override
    protected void addChildSettings() {
        eventUUID = new UUIDSetting("eventId", this);
        leaderboardName = new StringSetting("name", this);
    }

    public UUID getEventId() {
        return eventUUID.getValue();
    }

    public String getLeaderboardName() {
        return leaderboardName.getValue();
    }
}
