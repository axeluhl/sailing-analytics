package com.sap.sailing.server.gateway.interfaces;

import java.util.UUID;

import com.sap.sse.common.NamedWithUUID;

public interface MasterDataImportResult {
    interface LeaderboardGroupWithEventIds extends NamedWithUUID {
        Iterable<UUID> getEventIds();
    }

    Iterable<LeaderboardGroupWithEventIds> getLeaderboardGroupsImported();

    String getRemoteServerUrl();

    boolean isOverride();

    boolean isImportWind();

    boolean isImportDeviceConfigurations();

    boolean isImportTrackedRacesAndStartTracking();
}
