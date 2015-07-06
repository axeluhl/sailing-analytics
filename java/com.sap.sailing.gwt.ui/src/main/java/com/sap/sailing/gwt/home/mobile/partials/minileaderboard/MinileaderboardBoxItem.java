package com.sap.sailing.gwt.home.mobile.partials.minileaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.MiniLeaderboardItemDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.SimpleCompetitorDTO;

public class MinileaderboardBoxItem extends Widget {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Element, MinileaderboardBoxItem> {
    }
    
    @UiField
    Element competitorNameUi;
    @UiField
    Element competitorRankUi;
    @UiField
    Element competitorPointsUi;
    @UiField
    Element competitorCountryNameUi;
    @UiField
    ImageElement competitorFlagUi;
    
    public MinileaderboardBoxItem(MiniLeaderboardItemDTO entry) {
        setElement(uiBinder.createAndBindUi(this));
        competitorNameUi.setInnerText(entry.getCompetitor().getName());
        competitorCountryNameUi.setInnerText(String.valueOf(entry.getCompetitor().getSailID()));
        competitorRankUi.setInnerText(String.valueOf(entry.getRank()) + ".");
        competitorPointsUi.setInnerText(StringMessages.INSTANCE.pointsValue(entry.getPoints()));
        
        SimpleCompetitorDTO competitor = entry.getCompetitor();
        String flagImageURL = competitor.getFlagImageURL();
        String twoLetterIsoCountryCode = competitor.getTwoLetterIsoCountryCode();
        SafeUri imageUri = FlagImageResolver.getFlagImageResource(flagImageURL, twoLetterIsoCountryCode);
        competitorFlagUi.setSrc(imageUri.asString());
    }
}
