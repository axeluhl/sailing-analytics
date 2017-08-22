package com.sap.sailing.gwt.home.mobile.partials.anniversary;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class AnniversaryPanel extends Widget {

    private static UpcomingAnniversaryUiBinder uiBinder = GWT.create(UpcomingAnniversaryUiBinder.class);

    interface UpcomingAnniversaryUiBinder extends UiBinder<Element, AnniversaryPanel> {
    }

    @UiField
    DivElement teaserUi, descriptionUi;

    public AnniversaryPanel() {
        setElement(uiBinder.createAndBindUi(this));
        int countdown = 499, anniversary = 10000;

        teaserUi.setInnerText(StringMessages.INSTANCE.anniversaryMajorCountdownTeaser(countdown, anniversary));
        descriptionUi.setInnerText(StringMessages.INSTANCE.anniversaryMajorCountdownDescription(anniversary));
    }

}
