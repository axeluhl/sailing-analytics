package com.sap.sailing.gwt.home.desktop.partials.standings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.MiniLeaderboardItemDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.SimpleCompetitorDTO;

public class StandingsListCompetitor extends UIObject {
    interface MyUiBinder extends UiBinder<Element, StandingsListCompetitor> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    
    @UiField SpanElement rankUi;
    @UiField SpanElement teamIdUi;
    @UiField SpanElement teamNameUi;
    @UiField ImageElement flagUi;
    @UiField DivElement pointsUi;

    public StandingsListCompetitor(MiniLeaderboardItemDTO item, boolean showRaceCounts) {
        setElement(uiBinder.createAndBindUi(this));
        SimpleCompetitorDTO competitor = item.getCompetitor();
        
        rankUi.setInnerText(Integer.toString(item.getRank()));
        teamIdUi.setInnerText(competitor.getSailID());
        teamNameUi.setInnerText(competitor.getName());
        pointsUi.setInnerText(StringMessages.INSTANCE.pointsValue(item.getPoints()));
        SafeUri imageUri = FlagImageResolver.getFlagImageUri(competitor.getFlagImageURL(), competitor.getTwoLetterIsoCountryCode());
        flagUi.setSrc(imageUri.asString());
    }

}
