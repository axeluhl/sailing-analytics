package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.home.client.place.event.EventPageNavigator;
import com.sap.sailing.gwt.home.client.place.event.regattaschedule.EventRegattaScheduleResources.LocalCss;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaScheduleRace extends Composite {
    private static EventRegattaScheduleSeriesUiBinder uiBinder = GWT.create(EventRegattaScheduleSeriesUiBinder.class);

    interface EventRegattaScheduleSeriesUiBinder extends UiBinder<Widget, EventRegattaScheduleRace> {
    }

    private final FleetDTO fleet;
    private final RaceColumnDTO raceColumn;
    private final StrippedLeaderboardDTO leaderboard;
    
    @UiField Anchor raceBox;
    @UiField SpanElement raceName;
    @UiField SpanElement raceDetails;
    
    private final DateTimeFormat raceTimeFormat = DateTimeFormat.getFormat("EEE, h:mm a");
    private final EventPageNavigator pageNavigator;
    
    public EventRegattaScheduleRace(StrippedLeaderboardDTO leaderboard, FleetDTO fleet, RaceColumnDTO raceColumn, Timer timerForClientServerOffset, EventPageNavigator pageNavigator) {
        this.leaderboard = leaderboard;
        this.fleet = fleet;
        this.raceColumn = raceColumn;
        this.pageNavigator = pageNavigator;
        
        LocalCss css = EventRegattaScheduleResources.INSTANCE.css();
        css.ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        raceName.setInnerText(raceColumn.getName());

        RaceDTO race = raceColumn.getRace(fleet);
        if(race != null) {
            boolean live = raceColumn.isLive(fleet, timerForClientServerOffset.getLiveTimePointInMillis());
            
            // tracked race
            raceBox.getElement().addClassName(css.eventregattaschedule_series_fleet_racetracked());
            if(race.trackedRace != null) {
                if(race.trackedRace.startOfTracking != null) {
                    raceDetails.setInnerText(raceTimeFormat.format(race.trackedRace.startOfTracking));
                } else {
                    raceDetails.setInnerText("tracked");
                }
                if(live) {
                    raceBox.getElement().setAttribute("data-live", "data-live");
                }
            }
        } else {
            raceBox.getElement().addClassName(css.eventregattaschedule_series_fleet_raceuntracked());
            raceDetails.setInnerText("untracked");
        }
    }

    @UiHandler("raceBox")
    public void goToRaceboard(ClickEvent e) {
        RaceDTO race = raceColumn.getRace(fleet);
        if(race != null && race.trackedRace != null) {
            pageNavigator.openRaceViewer(leaderboard, race);
        }
    }
}
