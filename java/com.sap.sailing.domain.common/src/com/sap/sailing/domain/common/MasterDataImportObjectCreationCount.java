package com.sap.sailing.domain.common;

import java.io.Serializable;
import java.util.Set;

public interface MasterDataImportObjectCreationCount extends Serializable {
    int getLeaderboardCount();

    int getLeaderboardGroupCount();

    int getEventCount();

    int getRegattaCount();

	int getMediaTrackCount();

    Set<String> getOverwrittenRegattaNames();

	void addOneMediaTrack();
}