package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;

public class EventRegattaScheduleRace extends Composite {
    private static EventRegattaScheduleSeriesUiBinder uiBinder = GWT.create(EventRegattaScheduleSeriesUiBinder.class);

    interface EventRegattaScheduleSeriesUiBinder extends UiBinder<Widget, EventRegattaScheduleRace> {
    }

    @SuppressWarnings("unused")
    private final FleetDTO fleet;
    @SuppressWarnings("unused")
    private final RaceColumnDTO raceColumn;
    
    @UiField SpanElement raceName;
    @UiField SpanElement raceDetails;
    
    public EventRegattaScheduleRace(FleetDTO fleet, RaceColumnDTO raceColumn) {
        this.fleet = fleet;
        this.raceColumn = raceColumn;
        
        EventRegattaScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        raceName.setInnerText(raceColumn.getName());

        RaceDTO race = raceColumn.getRace(fleet);
        if(race != null) {
            raceDetails.setInnerText(race.isTracked ? "tracked" : "untracked");
        } else {
            raceDetails.setInnerText("no race");
        }
    }
    
}
