package com.sap.sailing.gwt.ui.client.shared.charts;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.shared.charts.RaceIdentifierToLeaderboardRaceColumnAndFleetMapper.LeaderboardNameRaceColumnNameAndFleetName;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public class MarkPositionServiceForSailingService implements MarkPositionService {
    private final SailingServiceAsync sailingService;
    
    public MarkPositionServiceForSailingService(SailingServiceAsync sailingService) {
        super();
        this.sailingService = sailingService;
    }
    
    @Override
    public void getMarksInTrackedRace(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, AsyncCallback<Iterable<MarkDTO>> callback) {
        sailingService.getMarksInTrackedRace(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), callback);
    }

    @Override
    public void getMarkTrack(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, String markIdAsString,
            AsyncCallback<MarkTrackDTO> callback) {
        sailingService.getMarkTrack(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), markIdAsString, callback);
    }

    @Override
    public void canRemoveMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark,
            GPSFixDTO fix, AsyncCallback<Boolean> callback) {
        sailingService.canRemoveMarkFix(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), mark.getIdAsString(), fix, callback);
    }

    @Override
    public void removeMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO fix, AsyncCallback<Void> callback) {
        sailingService.removeMarkFix(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), mark.getIdAsString(), fix, callback);
    }

    @Override
    public void addMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO newFix, AsyncCallback<Void> callback) {
        sailingService.addMarkFix(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), mark.getIdAsString(), newFix, callback);
    }

    @Override
    public void editMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO oldFix,
            Position newPosition, AsyncCallback<Void> callback) {
        sailingService.editMarkFix(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), mark.getIdAsString(), oldFix, newPosition, callback);
    }
}
