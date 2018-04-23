package com.sap.sailing.gwt.home.mobile.partials.minileaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.home.communication.event.minileaderboard.MiniLeaderboardItemDTO;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class MinileaderboardBoxItem extends Widget {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Element, MinileaderboardBoxItem> {
    }
    
    @UiField Element competitorNameUi;
    @UiField Element competitorRankUi;
    @UiField Element competitorPointsUi;
    @UiField Element competitorRacesUi;
    @UiField Element competitorCountryNameUi;
    @UiField ImageElement competitorFlagUi;
    
    public MinileaderboardBoxItem(MiniLeaderboardItemDTO entry, boolean showRaceCount, FlagImageResolver flagImageResolver) {
        setElement(uiBinder.createAndBindUi(this));
        competitorNameUi.setInnerText(entry.getCompetitor().getName());
        competitorCountryNameUi.setInnerText(String.valueOf(entry.getCompetitor().getShortInfo()));
        competitorRankUi.setInnerText(String.valueOf(entry.getRank()) + ".");
        competitorPointsUi.setInnerText(StringMessages.INSTANCE.pointsValue(entry.getNetPoints()));
        if (showRaceCount) {
            competitorRacesUi.setInnerText("(" + entry.getRaceCount() + ")");
        }
        
        SimpleCompetitorDTO competitor = entry.getCompetitor();
        String flagImageURL = competitor.getFlagImageURL();
        String twoLetterIsoCountryCode = competitor.getTwoLetterIsoCountryCode();
        SafeUri imageUri = flagImageResolver.getFlagImageUri(flagImageURL, twoLetterIsoCountryCode);
        competitorFlagUi.setSrc(imageUri.asString());
    }
}
