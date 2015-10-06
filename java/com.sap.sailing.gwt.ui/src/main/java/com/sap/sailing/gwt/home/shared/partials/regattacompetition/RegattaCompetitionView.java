package com.sap.sailing.gwt.home.shared.partials.regattacompetition;

import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatSeriesDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;

public interface RegattaCompetitionView {
    
    void clearContent();
    RegattaCompetitionSeriesView addSeriesView(RaceCompetitionFormatSeriesDTO series);

    public interface RegattaCompetitionSeriesView {
        RegattaCompetitionFleetView addFleetView(RaceCompetitionFormatFleetDTO fleet);
        void doFilter(boolean filter);
    }
    
    public interface RegattaCompetitionFleetView {
        RegattaCompetitionRaceView addRaceView(SimpleRaceMetadataDTO race, String raceViewerUrl);
        void setNumberOfFleetsInSeries(int fleetCount);
        void doFilter(boolean filter);
    }

    public interface RegattaCompetitionRaceView {
        void doFilter(boolean filter);
    }
    
}
