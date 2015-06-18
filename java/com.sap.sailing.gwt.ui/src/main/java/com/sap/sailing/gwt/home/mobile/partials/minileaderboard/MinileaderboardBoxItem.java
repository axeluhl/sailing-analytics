package com.sap.sailing.gwt.home.mobile.partials.minileaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.MiniLeaderboardItemDTO;

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
        competitorCountryNameUi.setInnerText(String.valueOf(entry.getCompetitor().getCountryName()));
        competitorRankUi.setInnerText(String.valueOf(entry.getRank()) + ".");
        competitorPointsUi.setInnerText(StringMessages.INSTANCE.pointsValue(entry.getPoints()));
        
        CompetitorDTO competitor = entry.getCompetitor();
        String flagImageURL = competitor.getFlagImageURL();

        if (flagImageURL == null || flagImageURL.isEmpty()) {
            String twoLetterIsoCountryCode = competitor.getTwoLetterIsoCountryCode();
            final ImageResource flagImageResource;
            if (twoLetterIsoCountryCode==null || twoLetterIsoCountryCode.isEmpty()) {
                flagImageResource = FlagImageResolver.getEmptyFlagImageResource();
            } else {
                flagImageResource = FlagImageResolver.getFlagImageResource(twoLetterIsoCountryCode);
            }
            flagImageURL = flagImageResource.getSafeUri().asString();
        }
        competitorFlagUi.setSrc(flagImageURL);
        
    }


}
