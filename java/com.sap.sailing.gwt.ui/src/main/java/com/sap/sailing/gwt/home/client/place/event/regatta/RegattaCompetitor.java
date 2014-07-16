package com.sap.sailing.gwt.home.client.place.event.regatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;

public class RegattaCompetitor extends Composite {
    private static RegattaCompetitorUiBinder uiBinder = GWT.create(RegattaCompetitorUiBinder.class);

    interface RegattaCompetitorUiBinder extends UiBinder<Widget, RegattaCompetitor> {
    }

    @UiField SpanElement competitorRank;
    @UiField SpanElement competitorNationality;
    @UiField SpanElement competitorName;
    @UiField SpanElement competitorTotalPoints;
    
    public RegattaCompetitor(Integer rank, CompetitorDTO competitor) {
        
        RegattaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        competitorRank.setInnerText(String.valueOf(rank));
    }
}
