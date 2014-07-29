package com.sap.sailing.gwt.home.client.place.event.regatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;

public class RegattaPhase extends UIObject {
    private static RegattaPhaseUiBinder uiBinder = GWT.create(RegattaPhaseUiBinder.class);

    interface RegattaPhaseUiBinder extends UiBinder<DivElement, RegattaPhase> {
    }

    @UiField SpanElement phaseName;
    @UiField DivElement phaseRacesPanel;
    
    public RegattaPhase(RaceGroupSeriesDTO series) {
        RegattaResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));

        if(LeaderboardNameConstants.DEFAULT_SERIES_NAME.equals(series.getName())) {
            phaseName.setInnerText("Races");
        } else {
            phaseName.setInnerText(series.getName());
        }
        
        series.getRaceColumns();
        for(RaceColumnDTO raceColumn: series.getRaceColumns()) {
            RegattaPhaseRace race = new RegattaPhaseRace(raceColumn);
            phaseRacesPanel.appendChild(race.getElement());
        }
    }
    
    public RegattaPhase() {
        RegattaResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
        
        phaseName.setInnerText("No series defined yet.");
    }
}
