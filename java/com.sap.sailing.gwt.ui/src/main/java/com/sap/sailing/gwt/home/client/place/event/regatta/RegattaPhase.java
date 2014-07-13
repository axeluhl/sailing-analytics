package com.sap.sailing.gwt.home.client.place.event.regatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.RaceGroupSeriesDTO;

public class RegattaPhase extends Composite {
    private static RegattaPhaseUiBinder uiBinder = GWT.create(RegattaPhaseUiBinder.class);

    interface RegattaPhaseUiBinder extends UiBinder<Widget, RegattaPhase> {
    }

    @UiField SpanElement phaseName;
    @UiField HTMLPanel phaseRacesPanel;
    
    public RegattaPhase(RaceGroupSeriesDTO series) {
        
        RegattaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        phaseName.setInnerText(series.getName());
        
        for(int i = 0; i < 5; i++) {
            RegattaPhaseRace race = new RegattaPhaseRace();
            phaseRacesPanel.add(race);
        }
    }
}
