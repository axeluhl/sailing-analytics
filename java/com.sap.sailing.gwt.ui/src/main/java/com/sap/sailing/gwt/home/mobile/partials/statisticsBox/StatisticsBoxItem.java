package com.sap.sailing.gwt.home.mobile.partials.statisticsBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

class StatisticsBoxItem extends Widget {

    private static StatisticsBoxItemUiBinder uiBinder = GWT.create(StatisticsBoxItemUiBinder.class);

    interface StatisticsBoxItemUiBinder extends UiBinder<Element, StatisticsBoxItem> {
    }
    
    @UiField ImageElement iconUi;
    @UiField SpanElement nameUi;
    @UiField SpanElement valueUi;

    public StatisticsBoxItem(String iconUrl, String name, Object payload) {
        setElement(uiBinder.createAndBindUi(this));
        iconUi.setSrc(iconUrl);
        nameUi.setInnerText(name);
        final String text = payload == null ? "n/a" : String.valueOf(payload);
        valueUi.setInnerText(text);
        valueUi.setTitle(text);
    }

}
