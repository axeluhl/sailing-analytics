package com.sap.sailing.domain.common;

import java.io.Serializable;

public interface MasterDataImportObjectCreationCount extends Serializable {
    int getLeaderboardCount();

    int getLeaderboardGroupCount();

    int getEventCount();
    
    int getRegattaCount();

    int getMediaTrackCount();

    Iterable<String> getNamesOfOverwrittenRegattaNames();

    int getTrackedRacesCount();
}