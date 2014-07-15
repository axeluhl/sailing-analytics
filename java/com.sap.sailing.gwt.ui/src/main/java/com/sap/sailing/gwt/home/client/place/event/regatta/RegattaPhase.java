package com.sap.sailing.gwt.home.client.place.event.regatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;

public class RegattaPhase {
    private static RegattaPhaseUiBinder uiBinder = GWT.create(RegattaPhaseUiBinder.class);

    interface RegattaPhaseUiBinder extends UiBinder<DivElement, RegattaPhase> {
    }

    @UiField SpanElement phaseName;
    @UiField DivElement phaseRacesPanel;
    
    private DivElement root;

    public RegattaPhase(RaceGroupSeriesDTO series) {
        RegattaResources.INSTANCE.css().ensureInjected();
        root = uiBinder.createAndBindUi(this);
        
        phaseName.setInnerText(series.getName());
        
        for(int i = 0; i < 5; i++) {
            RegattaPhaseRace race = new RegattaPhaseRace();
            phaseRacesPanel.appendChild(race.getElement());
        }
    }

    public Element getElement() {
        return root;
      }
}
