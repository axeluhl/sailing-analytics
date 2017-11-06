package com.sap.sailing.gwt.home.desktop.partials.eventdescription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class EventDescription extends Composite {

    private static StatisticsBoxUiBinder uiBinder = GWT.create(StatisticsBoxUiBinder.class);

    interface StatisticsBoxUiBinder extends UiBinder<Widget, EventDescription> {
    }
    
    @UiField DivElement descriptionUi;
    
    public EventDescription(String description) {
        EventDescriptionResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        descriptionUi.setInnerText(description);
    }
}
