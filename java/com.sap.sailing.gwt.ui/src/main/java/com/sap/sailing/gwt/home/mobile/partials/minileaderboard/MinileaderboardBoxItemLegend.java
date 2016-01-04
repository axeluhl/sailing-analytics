package com.sap.sailing.gwt.home.mobile.partials.minileaderboard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class MinileaderboardBoxItemLegend extends Widget {
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    interface MyUiBinder extends UiBinder<Element, MinileaderboardBoxItemLegend> {
    }
    
    public MinileaderboardBoxItemLegend() {
        setElement(uiBinder.createAndBindUi(this));
    }
}
