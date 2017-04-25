package com.sap.sailing.gwt.autoplay.client.app.sixtyinch;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public class SixtyInchSetting extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -5170519954446053233L;

    private UUIDSetting eventId;
    private StringSetting leaderBoardName;

    public SixtyInchSetting(UUID eventUuid, String selectedLeaderboardName) {
        eventId.setValue(eventUuid);
        leaderBoardName.setValue(selectedLeaderboardName);
    }

    public SixtyInchSetting() {
    }

    @Override
    protected void addChildSettings() {
        this.eventId = new UUIDSetting("eventUuid", this);
        this.leaderBoardName = new StringSetting("name", this);
    }

    public UUID getEventId() {
        return eventId.getValue();
    }

    public String getLeaderBoardName() {
        return leaderBoardName.getValue();
    }

}

