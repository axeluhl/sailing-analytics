package com.sap.sailing.gwt.home.client.place.event.regatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

public class RegattaPhaseRace {
    private static RegattaPhaseRaceUiBinder uiBinder = GWT.create(RegattaPhaseRaceUiBinder.class);

    interface RegattaPhaseRaceUiBinder extends UiBinder<DivElement, RegattaPhaseRace> {
    }

    @UiField DivElement raceStatus;
    
    private DivElement root;
    
    public RegattaPhaseRace() {
        RegattaResources.INSTANCE.css().ensureInjected();
        root = uiBinder.createAndBindUi(this);
        
        raceStatus.setAttribute("data-status", "raceStatus");
    }
    
    public Element getElement() {
        return root;
      }

}
