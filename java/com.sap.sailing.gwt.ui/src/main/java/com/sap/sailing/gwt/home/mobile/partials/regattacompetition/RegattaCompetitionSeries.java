package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import static com.sap.sailing.domain.common.LeaderboardNameConstants.DEFAULT_SERIES_NAME;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.events.CollapseAnimation;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.home.mobile.places.races.RacesView.Presenter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListFleetDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListSeriesDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.RaceTrackingState;

public class RegattaCompetitionSeries extends Composite {

    private static RegattaCompetitionSeriesUiBinder uiBinder = GWT.create(RegattaCompetitionSeriesUiBinder.class);

    interface RegattaCompetitionSeriesUiBinder extends UiBinder<Widget, RegattaCompetitionSeries> {
    }
    
    private static final String ACCORDION_COLLAPSED_STYLE = RegattaCompetitionResources.INSTANCE.css().accordioncollapsed();
    private static final StringMessages I18N = StringMessages.INSTANCE;
    
    
    @UiField SectionHeaderContent sectionHeaderUi;
    @UiField FlowPanel fleetContainerUi;

    public RegattaCompetitionSeries(RaceListSeriesDTO series) {
        initWidget(uiBinder.createAndBindUi(this));
        sectionHeaderUi.setSectionTitle(DEFAULT_SERIES_NAME.equals(series.getSeriesName()) ? I18N.races() : series.getSeriesName());
        initSubtitle(series.getCompetitorCount(), series.getRaceCount());
        initAnimation(sectionHeaderUi, fleetContainerUi, true);
    }
    
    private void initSubtitle(int competitorCount, int raceCount) {
        if (competitorCount > 0 || raceCount > 0) {
            String competitors = competitorCount > 0 ? I18N.competitorsCount(competitorCount) : "";
            String separator = competitorCount > 0 && raceCount > 0 ? " | " : "";
            String races = raceCount > 0 ? I18N.racesCount(raceCount) : "";
            sectionHeaderUi.setSubtitle(competitors + separator + races);
        }
    }        
    
    private void initAnimation(Widget trigger, final Widget content, boolean showInitial) {
        final CollapseAnimation animation = new CollapseAnimation(content.getElement(), showInitial);
        content.setStyleName(ACCORDION_COLLAPSED_STYLE, !showInitial);
        trigger.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boolean collapsed = content.getElement().hasClassName(ACCORDION_COLLAPSED_STYLE);
                content.setStyleName(ACCORDION_COLLAPSED_STYLE, !collapsed);
                animation.animate(collapsed);
            }
        }, ClickEvent.getType());
    }

    public void addFleet(Presenter presenter, RaceListFleetDTO fleet, int fleetCount) {
        RegattaCompetitionFleet competitionFleet = new RegattaCompetitionFleet(fleet, fleetCount);
        for (RaceListRaceDTO race : fleet.getRaces()) {
            boolean tracked = race.getTrackingState() == RaceTrackingState.TRACKED_VALID_DATA;
            String raceViewerUrl = tracked ? null : null; // TODO No mobile "RaceViewer implemented yet 
            competitionFleet.addRace(race, raceViewerUrl);
        }
        fleetContainerUi.add(competitionFleet);
    }

}
