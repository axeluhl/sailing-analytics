package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import static com.sap.sailing.domain.common.LeaderboardNameConstants.DEFAULT_SERIES_NAME;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.mobile.places.event.races.RacesView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceCompetitionFormatSeriesDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO.RaceTrackingState;
import com.sap.sse.common.filter.Filter;

public class RegattaCompetitionSeries extends Composite {

    private static RegattaCompetitionSeriesUiBinder uiBinder = GWT.create(RegattaCompetitionSeriesUiBinder.class);

    interface RegattaCompetitionSeriesUiBinder extends UiBinder<MobileSection, RegattaCompetitionSeries> {
    }
    
    private static final StringMessages I18N = StringMessages.INSTANCE;
    
    @UiField SectionHeaderContent sectionHeaderUi;
    @UiField FlowPanel fleetContainerUi;
    private final MobileSection seriesUi;

    public RegattaCompetitionSeries(RaceCompetitionFormatSeriesDTO series) {
        initWidget(seriesUi = uiBinder.createAndBindUi(this));
        seriesUi.setEdgeToEdgeContent(true);
        sectionHeaderUi.setSectionTitle(DEFAULT_SERIES_NAME.equals(series.getSeriesName()) ? I18N.races() : series.getSeriesName());
        initSubtitle(series.getCompetitorCount(), series.getRaceCount());
        sectionHeaderUi.initCollapsibility(fleetContainerUi.getElement(), true);
    }
    
    private void initSubtitle(int competitorCount, int raceCount) {
        if (competitorCount > 0 || raceCount > 0) {
            String competitors = competitorCount > 0 ? I18N.competitorsCount(competitorCount) : "";
            String separator = competitorCount > 0 && raceCount > 0 ? " | " : "";
            String races = raceCount > 0 ? I18N.racesCount(raceCount) : "";
            sectionHeaderUi.setSubtitle(competitors + separator + races);
        }
    }        
    
    public void addFleet(Presenter presenter, RaceCompetitionFormatFleetDTO fleet, int fleetCount) {
        RegattaCompetitionFleet competitionFleet = new RegattaCompetitionFleet(fleet, fleetCount);
        for (SimpleRaceMetadataDTO race : fleet.getRaces()) {
            boolean tracked = race.getTrackingState() == RaceTrackingState.TRACKED_VALID_DATA;
            String raceViewerUrl = tracked ? null : null; // TODO No mobile "RaceViewer implemented yet 
            competitionFleet.addRace(race, raceViewerUrl);
        }
        fleetContainerUi.add(competitionFleet);
    }

    public void applyFilter(Filter<SimpleRaceMetadataDTO> racesFilter) {
        boolean seriesVisible = false;
        int visibleFleetCount = 0;
        for (int i = 0; i < fleetContainerUi.getWidgetCount(); i++) {
            RegattaCompetitionFleet fleet = (RegattaCompetitionFleet) fleetContainerUi.getWidget(i);
            visibleFleetCount = visibleFleetCount + (fleet.applyFilter(racesFilter) ? 1 : 0);
            seriesVisible |= fleet.applyFilter(racesFilter);
        }
        seriesVisible = visibleFleetCount > 0;
        for (int i = 0; i < fleetContainerUi.getWidgetCount(); i++) {
            RegattaCompetitionFleet fleet = (RegattaCompetitionFleet) fleetContainerUi.getWidget(i);
            fleet.updateFleetWidth(visibleFleetCount);
        }
        setVisible(seriesVisible);
    }

}
