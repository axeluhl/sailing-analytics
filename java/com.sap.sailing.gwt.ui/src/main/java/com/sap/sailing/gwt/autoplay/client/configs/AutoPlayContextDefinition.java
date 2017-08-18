package com.sap.sailing.gwt.autoplay.client.configs;

import java.util.UUID;

import com.sap.sse.common.settings.generic.GenericSerializableSettings;

public interface AutoPlayContextDefinition extends GenericSerializableSettings{
    
    AutoPlayType getType();
    UUID getEventId();
    String getLeaderboardName();

}