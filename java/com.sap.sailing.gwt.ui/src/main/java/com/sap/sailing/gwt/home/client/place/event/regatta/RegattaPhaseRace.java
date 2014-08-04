package com.sap.sailing.gwt.home.client.place.event.regatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;

public class RegattaPhaseRace extends UIObject {
    private static RegattaPhaseRaceUiBinder uiBinder = GWT.create(RegattaPhaseRaceUiBinder.class);

    interface RegattaPhaseRaceUiBinder extends UiBinder<DivElement, RegattaPhaseRace> {
    }

    @UiField DivElement raceStatus;
    
    public RegattaPhaseRace(RaceColumnDTO raceColumn) {
        RegattaResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        
        raceStatus.setAttribute("data-status", "finished");
    }
}
