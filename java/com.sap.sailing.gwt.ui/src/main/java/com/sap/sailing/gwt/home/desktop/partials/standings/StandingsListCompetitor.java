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
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.MiniLeaderboardItemDTO;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class StandingsListCompetitor extends UIObject {
    private static final StringMessages i18n = StringMessages.INSTANCE;

    interface MyUiBinder extends UiBinder<Element, StandingsListCompetitor> {
    }
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    
    @UiField SpanElement rankUi;
    @UiField SpanElement teamIdUi;
    @UiField SpanElement teamNameUi;
    @UiField ImageElement flagUi;
    @UiField DivElement pointsUi;

    public StandingsListCompetitor(MiniLeaderboardItemDTO item, boolean showRaceCounts, FlagImageResolver flagImageResolver) {
        setElement(uiBinder.createAndBindUi(this));
        SimpleCompetitorDTO competitor = item.getCompetitor();
        
        rankUi.setInnerText(Integer.toString(item.getRank()));
        teamIdUi.setInnerText(competitor.getShortInfo());
        teamNameUi.setInnerText(competitor.getName());
        String pointsString = i18n.pointsValue(item.getNetPoints());
        if (showRaceCounts) {
            pointsString += " (" + i18n.racesCount(item.getRaceCount()) + ")";
        }
        pointsUi.setInnerText(pointsString);
        SafeUri imageUri = flagImageResolver.getFlagImageUri(competitor.getFlagImageURL(), competitor.getTwoLetterIsoCountryCode());
        flagUi.setSrc(imageUri.asString());
    }

}
