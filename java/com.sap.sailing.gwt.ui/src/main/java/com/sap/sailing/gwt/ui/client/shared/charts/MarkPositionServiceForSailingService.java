package com.sap.sailing.gwt.ui.client.shared.charts;

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
    public MarkTracksDTO getMarkTracks(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canRemoveMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark,
            GPSFixDTO fix) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO fix) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO newFix) {
        // TODO Auto-generated method stub

    }

    @Override
    public void editMarkFix(LeaderboardNameRaceColumnNameAndFleetName raceIdentifier, MarkDTO mark, GPSFixDTO oldFix,
            Position newPosition) {
        // TODO Auto-generated method stub

    }

}
