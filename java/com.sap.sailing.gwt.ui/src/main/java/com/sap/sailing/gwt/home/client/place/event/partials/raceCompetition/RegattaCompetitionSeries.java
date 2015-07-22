package com.sap.sailing.gwt.home.client.place.event.partials.raceCompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaView.Presenter;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatSeriesDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO.RaceTrackingState;

public class RegattaCompetitionSeries extends Composite {

    private static RegattaCompetitionSeriesUiBinder uiBinder = GWT.create(RegattaCompetitionSeriesUiBinder.class);

    interface RegattaCompetitionSeriesUiBinder extends UiBinder<HTMLPanel, RegattaCompetitionSeries> {
    }
    
    @UiField DivElement seriesNameUi;
    @UiField DivElement competitorCountUi;
    @UiField DivElement raceCountUi;
    private final HTMLPanel containerUi;

    public RegattaCompetitionSeries(Presenter presenter, RaceCompetitionFormatSeriesDTO series) {
        initWidget(containerUi = uiBinder.createAndBindUi(this));
        this.seriesNameUi.setInnerText(series.getSeriesName());
        this.competitorCountUi.setInnerText(series.getCompetitorCount() + " Comp. TODO");
        this.raceCountUi.setInnerText(series.getRaceCount() + " Races TODO");
        for (RaceCompetitionFormatFleetDTO fleet : series.getFleets()) {
            addFleet(presenter, fleet);
        }
    }
    
    public void addFleet(Presenter presenter, RaceCompetitionFormatFleetDTO fleet) {
        RegattaCompetitionFleet competitionFleet = new RegattaCompetitionFleet(fleet);
        for (SimpleRaceMetadataDTO race : fleet.getRaces()) {
            boolean tracked = race.getTrackingState() == RaceTrackingState.TRACKED_VALID_DATA;
            String raceViewerUrl = tracked ? presenter.getRaceViewerURL(race.getLeaderboardName(), race.getRegattaAndRaceIdentifier()) : null; 
            competitionFleet.addRace(race, raceViewerUrl);
        }
        containerUi.add(competitionFleet);
    }

}
