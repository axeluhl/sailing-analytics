package com.sap.sailing.server.gateway.interfaces;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.common.Util;

public interface MasterDataImportResult {
    interface LeaderboardGroupWithEventIds extends NamedWithUUID {
        Iterable<UUID> getEventIds();
    }

    Iterable<LeaderboardGroupWithEventIds> getLeaderboardGroupsImported();
    
    default Iterable<UUID> getEventIdsImported() {
        final Set<UUID> result = new HashSet<>();
        getLeaderboardGroupsImported().forEach(lgweid->Util.addAll(lgweid.getEventIds(), result));
        return result;
    }

    String getRemoteServerUrl();

    boolean isOverride();

    boolean isImportWind();

    boolean isImportDeviceConfigurations();

    boolean isImportTrackedRacesAndStartTracking();
}
