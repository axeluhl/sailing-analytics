package com.sap.sailing.gwt.ui.client.shared.charts;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.gwt.ui.client.SailingWriteServiceAsync;
import com.sap.sailing.gwt.ui.client.shared.charts.RaceIdentifierToLeaderboardRaceColumnAndFleetMapper.LeaderboardNameRaceColumnNameAndFleetName;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;

public class MarkPositionServiceForSailingService implements MarkPositionService {
    private final SailingWriteServiceAsync sailingWriteService;
    
    public MarkPositionServiceForSailingService(SailingWriteServiceAsync sailingWriteService) {
        super();
        this.sailingWriteService = sailingWriteService;
    }
    
    @Override
    public void getMarksInTrackedRace(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, AsyncCallback<Iterable<MarkDTO>> callback) {
        sailingWriteService.getMarksInTrackedRace(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), callback);
    }

    @Override
    public void getMarkTrack(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, String markIdAsString,
            AsyncCallback<MarkTrackDTO> callback) {
        sailingWriteService.getMarkTrack(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), markIdAsString, callback);
    }

    @Override
    public void canRemoveMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark,
            GPSFixDTO fix, AsyncCallback<Boolean> callback) {
        sailingWriteService.canRemoveMarkFix(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), mark.getIdAsString(), fix, callback);
    }

    @Override
    public void removeMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO fix, AsyncCallback<Void> callback) {
        sailingWriteService.removeMarkFix(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), mark.getIdAsString(), fix, callback);
    }

    @Override
    public void addMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO newFix, AsyncCallback<Void> callback) {
        sailingWriteService.addMarkFix(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), mark.getIdAsString(), newFix, callback);
    }

    @Override
    public void editMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO oldFix,
            Position newPosition, AsyncCallback<Void> callback) {
        sailingWriteService.editMarkFix(raceIdentifier.getLeaderboardName(), raceIdentifier.getRaceColumnName(), raceIdentifier.getFleetName(), mark.getIdAsString(), oldFix, newPosition, callback);
    }
}
