package com.sap.sailing.gwt.home.client.shared.stage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class Stage extends Composite {

    @SuppressWarnings("unused")
    private EventDTO featuredEvent;
    
    @UiField Label subtitle;
    @UiField Label title;
    @UiField Label message;
    @UiField Label message2;
    @UiField Label name;

    interface StageUiBinder extends UiBinder<Widget, Stage> {
    }
    
    private static StageUiBinder uiBinder = GWT.create(StageUiBinder.class);

    public Stage() {
        StageResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setFeaturedEvent(EventDTO featuredEvent) {
        this.featuredEvent = featuredEvent;
        
        name.setText(featuredEvent.getName());
        title.setText(featuredEvent.getName());
        subtitle.setText(featuredEvent.venue.getName());
        message.setText(featuredEvent.endDate.toString());
        message2.setText(featuredEvent.endDate.toString());
    }

}
