package com.sap.sailing.gwt.home.mobile.partials.anniversary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.sap.sailing.gwt.home.shared.partials.anniversary.AbstractAnniversaryItem;

class AnniversaryItem extends AbstractAnniversaryItem {

    interface AnniversaryItemUiBinder extends UiBinder<Element, AnniversaryItem> {
    }

    private static AnniversaryItemUiBinder uiBinder = GWT.create(AnniversaryItemUiBinder.class);

    AnniversaryItem(boolean isAnnouncement) {
        setElement(uiBinder.createAndBindUi(this));
        super.initLayout(isAnnouncement);
    }

}
