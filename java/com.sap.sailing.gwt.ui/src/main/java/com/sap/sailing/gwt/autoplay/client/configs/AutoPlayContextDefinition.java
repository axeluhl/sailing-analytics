package com.sap.sailing.gwt.autoplay.client.configs;

import java.util.UUID;

public interface AutoPlayContextDefinition {
    
    AutoPlayType getType();
    UUID getEventId();
    String getLeaderboardName();

}