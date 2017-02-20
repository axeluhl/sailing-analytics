package com.sap.sailing.gwt.autoplay.client.place.player;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public class AutoPlayerContext extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 2880854263616658272L;

    private transient UUIDSetting eventUUID;
    private transient StringSetting leaderboardName;

    public AutoPlayerContext(UUID eventUUID, String leaderboardName) {
        this.eventUUID.setValue(eventUUID);
        this.leaderboardName.setValue(leaderboardName);
    }

    public AutoPlayerContext() {
    }

    @Override
    protected void addChildSettings() {
        eventUUID = new UUIDSetting("eventId", this);
        leaderboardName = new StringSetting("name", this);
    }

    public UUID getEventUidAsString() {
        return eventUUID.getValue();
    }

    public String getLeaderboardName() {
        return leaderboardName.getValue();
    }
}
