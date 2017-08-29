package com.sap.sailing.gwt.home.desktop.partials.anniversary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

class AnniversaryItem extends Widget {

    private static UpcomingAnniversaryUiBinder uiBinder = GWT.create(UpcomingAnniversaryUiBinder.class);

    interface UpcomingAnniversaryUiBinder extends UiBinder<Element, AnniversaryItem> {
    }

    @UiField
    DivElement iconUi, teaserUi, descriptionUi;

    AnniversaryItem(String iconUrl, String teaser, String description) {
        setElement(uiBinder.createAndBindUi(this));
        this.iconUi.getStyle().setBackgroundImage("url('" + iconUrl + "')");
        this.teaserUi.setInnerText(teaser);
        this.descriptionUi.setInnerHTML(description);
    }

}
