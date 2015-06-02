package com.sap.sailing.gwt.home.client.place.event.partials.regattaraces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.domain.common.dto.FleetDTO;

public class EventRegattaRacesFleet extends UIObject {
    private static EventRegattaRacesFleetUiBinder uiBinder = GWT.create(EventRegattaRacesFleetUiBinder.class);

    interface EventRegattaRacesFleetUiBinder extends UiBinder<DivElement, EventRegattaRacesFleet> {
    }

    @UiField SpanElement fleetName;
    @UiField DivElement fleetCountAndColor;

    public EventRegattaRacesFleet(FleetDTO fleet, int maxNumberOfCompetitorsInFleetRace) {
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        
        fleetName.setInnerText(fleet.getName());
        if(fleet.getColor() != null) {
            fleetCountAndColor.getStyle().setBackgroundColor(fleet.getColor().getAsHtml());
        }
        if(maxNumberOfCompetitorsInFleetRace > 0) {
            fleetCountAndColor.setInnerText(String.valueOf(maxNumberOfCompetitorsInFleetRace));
        }
    }
}
