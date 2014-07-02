package com.sap.sailing.gwt.home.client.place.event.regattaschedule;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class EventRegattaScheduleFleet extends Composite {
    private static EventRegattaScheduleSeriesUiBinder uiBinder = GWT.create(EventRegattaScheduleSeriesUiBinder.class);

    interface EventRegattaScheduleSeriesUiBinder extends UiBinder<Widget, EventRegattaScheduleFleet> {
    }

    @SuppressWarnings("unused")
    private final RegattaDTO regatta;
    @SuppressWarnings("unused")
    private final SeriesDTO series;

    @SuppressWarnings("unused")
    private final FleetDTO fleet;
    
    @UiField SpanElement fleetName;
    @UiField HTMLPanel racesListPanel;
    
    public EventRegattaScheduleFleet(RegattaDTO regatta, SeriesDTO series, FleetDTO fleet) {
        this.regatta = regatta;
        this.series = series;
        this.fleet = fleet;
        
        EventRegattaScheduleResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        fleetName.setInnerText(fleet.getName());
        
        for(RaceColumnDTO raceColumn: series.getRaceColumns()) {
            EventRegattaScheduleRace eventRegattaScheduleRace = new EventRegattaScheduleRace(fleet, raceColumn);
            racesListPanel.add(eventRegattaScheduleRace);
        }
    }
    
}
