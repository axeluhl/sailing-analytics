package com.sap.sailing.server.gateway.dto;

import java.util.Map;
import java.util.UUID;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.gateway.interfaces.MasterDataImportResult;
import com.sap.sse.common.NamedWithUUID;
import com.sap.sse.common.Util;

public class MasterDataImportResultImpl implements MasterDataImportResult {
    private final Iterable<LeaderboardGroupWithEventIds> leaderboardGroupsImported;
    private final String remoteServerUrl;
    private final boolean override;
    private final boolean importWind;
    private final boolean importDeviceConfigurations;
    private final boolean importTrackedRacesAndStartTracking;

    private static class LeaderboardGroupWithEventIdsImpl implements MasterDataImportResult.LeaderboardGroupWithEventIds {
        private static final long serialVersionUID = -2215673636716792130L;
        private final NamedWithUUID leaderboardGroup;
        private final Iterable<UUID> eventIds;

        public LeaderboardGroupWithEventIdsImpl(NamedWithUUID leaderboardGroup, Iterable<UUID> eventIds) {
            super();
            this.leaderboardGroup = leaderboardGroup;
            this.eventIds = eventIds;
        }
        
        @Override
        public UUID getId() {
            return leaderboardGroup.getId();
        }

        @Override
        public String getName() {
            return leaderboardGroup.getName();
        }

        @Override
        public Iterable<UUID> getEventIds() {
            return eventIds;
        }
    }

    public MasterDataImportResultImpl(Map<LeaderboardGroup, ? extends Iterable<Event>> leaderboardGroupsImportedWithEventIds,
            String remoteServerUrl, boolean override, boolean importWind, boolean importDeviceConfigurations,
            boolean importTrackedRacesAndStartTracking) {
        this(Util.map(leaderboardGroupsImportedWithEventIds.entrySet(),
                e->new LeaderboardGroupWithEventIdsImpl(e.getKey(), Util.map(e.getValue(), event->event.getId()))),
             remoteServerUrl, override, importWind, importDeviceConfigurations, importTrackedRacesAndStartTracking);
    }
    
    public MasterDataImportResultImpl(Iterable<LeaderboardGroupWithEventIds> leaderboardGroupsImported,
            String remoteServerUrl, boolean override, boolean importWind, boolean importDeviceConfigurations,
            boolean importTrackedRacesAndStartTracking) {
        this.leaderboardGroupsImported = leaderboardGroupsImported;
        this.remoteServerUrl = remoteServerUrl;
        this.override = override;
        this.importWind = importWind;
        this.importDeviceConfigurations = importDeviceConfigurations;
        this.importTrackedRacesAndStartTracking = importTrackedRacesAndStartTracking;
    }

    @Override
    public Iterable<LeaderboardGroupWithEventIds> getLeaderboardGroupsImported() {
        return leaderboardGroupsImported;
    }

    @Override
    public String getRemoteServerUrl() {
        return remoteServerUrl;
    }

    @Override
    public boolean isOverride() {
        return override;
    }

    @Override
    public boolean isImportWind() {
        return importWind;
    }

    @Override
    public boolean isImportDeviceConfigurations() {
        return importDeviceConfigurations;
    }

    @Override
    public boolean isImportTrackedRacesAndStartTracking() {
        return importTrackedRacesAndStartTracking;
    }

}
