package com.sap.sailing.gwt.home.shared.partials.regattacompetition;

import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.home.communication.event.RaceCompetitionFormatSeriesDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;


public interface RegattaCompetitionView {
    
    void clearContent();
    RegattaCompetitionSeriesView addSeriesView(RaceCompetitionFormatSeriesDTO series);

    public interface RegattaCompetitionSeriesView {
        RegattaCompetitionFleetView addFleetView(RaceCompetitionFormatFleetDTO fleet);
        void doFilter(boolean filter);
    }
    
    public interface RegattaCompetitionFleetView {
        RegattaCompetitionRaceView addRaceView(SimpleRaceMetadataDTO race, RegattaCompetitionPresenter presenter);
        void setNumberOfFleetsInSeries(int fleetCount);
        void doFilter(boolean filter);
    }

    public interface RegattaCompetitionRaceView {
        void doFilter(boolean filter);
    }
    
}
