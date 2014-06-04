package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.EventDatesFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class Stage extends Composite {

    @SuppressWarnings("unused")
    private EventDTO featuredEvent;
    
    @UiField Label subtitle;
    @UiField Label title;
    @UiField Label message;
    @UiField SpanElement regattaState;
    @UiField SpanElement actionMessage;

    interface StageUiBinder extends UiBinder<Widget, Stage> {
    }
    
    private static StageUiBinder uiBinder = GWT.create(StageUiBinder.class);

    public Stage() {
        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setFeaturedEvent(EventDTO featuredEvent) {
        this.featuredEvent = featuredEvent;
        
        title.setText(featuredEvent.getName());
        subtitle.setText(featuredEvent.venue.getName());

        message.setText(EventDatesFormatterUtil.formatDateRangeWithYear(featuredEvent.startDate, featuredEvent.endDate));
        actionMessage.setInnerText("[Race 6]");
        regattaState.setInnerText("Next Race");
    }
    
}
