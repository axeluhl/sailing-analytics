package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.home.client.place.event.regattaschedule.EventRegattaScheduleResources.LocalCss;
import com.sap.sse.gwt.client.player.Timer;

public class EventRegattaScheduleRace extends Composite {
    private static EventRegattaScheduleSeriesUiBinder uiBinder = GWT.create(EventRegattaScheduleSeriesUiBinder.class);

    interface EventRegattaScheduleSeriesUiBinder extends UiBinder<Widget, EventRegattaScheduleRace> {
    }

    @SuppressWarnings("unused")
    private final FleetDTO fleet;
    @SuppressWarnings("unused")
    private final RaceColumnDTO raceColumn;
    
    @UiField Anchor raceBox;
    @UiField SpanElement raceName;
    @UiField SpanElement raceDetails;
    
    public EventRegattaScheduleRace(FleetDTO fleet, RaceColumnDTO raceColumn, Timer timerForClientServerOffset) {
        this.fleet = fleet;
        this.raceColumn = raceColumn;
        
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
                    raceDetails.setInnerText(race.trackedRace.startOfTracking.toString());
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
    
}
