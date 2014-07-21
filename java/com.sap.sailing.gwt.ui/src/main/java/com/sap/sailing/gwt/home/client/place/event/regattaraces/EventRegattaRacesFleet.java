package com.sap.sailing.gwt.home.client.place.event.regattaraces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sap.sailing.domain.common.dto.FleetDTO;

public class EventRegattaRacesFleet {
    private static EventRegattaRacesFleetUiBinder uiBinder = GWT.create(EventRegattaRacesFleetUiBinder.class);

    interface EventRegattaRacesFleetUiBinder extends UiBinder<DivElement, EventRegattaRacesFleet> {
    }

    @UiField SpanElement fleetName;
    @UiField DivElement fleetCountAndColor;
    
    private DivElement root;

    public EventRegattaRacesFleet(FleetDTO fleet) {
        EventRegattaRacesResources.INSTANCE.css().ensureInjected();
        root = uiBinder.createAndBindUi(this);
        
        fleetName.setInnerText(fleet.getName());
        if(fleet.getColor() != null) {
            fleetCountAndColor.getStyle().setBackgroundColor(fleet.getColor().getAsHtml());
        }
        fleetCountAndColor.setInnerText("tbd.");
    }
    
    public Element getElement() {
        return root;
    }
}
