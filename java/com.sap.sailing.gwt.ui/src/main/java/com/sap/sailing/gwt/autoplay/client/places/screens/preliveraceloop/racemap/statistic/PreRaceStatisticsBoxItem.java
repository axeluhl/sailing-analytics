package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.statistic;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;

class PreRaceStatisticsBoxItem extends UIObject {

    private static PreRaceStatisticsBoxItemUiBinder uiBinder = GWT.create(PreRaceStatisticsBoxItemUiBinder.class);

    interface PreRaceStatisticsBoxItemUiBinder extends UiBinder<Element, PreRaceStatisticsBoxItem> {
    }
    
    @UiField ImageElement iconUi;
    @UiField SpanElement nameUi;
    @UiField SpanElement valueUi;

    public PreRaceStatisticsBoxItem(String iconUrl, String name, Object payload) {
        setElement(uiBinder.createAndBindUi(this));
        iconUi.setSrc(iconUrl);
        nameUi.setInnerText(name);
        valueUi.setInnerText(payload == null ? "n/a" : String.valueOf(payload));
    }

}
