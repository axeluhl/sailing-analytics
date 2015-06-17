package com.sap.sailing.gwt.home.mobile.partials.minileaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMobileLeaderbordAction.SimplifiedLeaderboardItemDTO;

public class MinileaderboardBoxItem extends Widget {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Element, MinileaderboardBoxItem> {
    }
    
    @UiField
    Element competitorNameUi;
    
    public MinileaderboardBoxItem(SimplifiedLeaderboardItemDTO entry) {
        setElement(uiBinder.createAndBindUi(this));
        competitorNameUi.setInnerText(entry.getCompetitor().getName());
        
    }


}
