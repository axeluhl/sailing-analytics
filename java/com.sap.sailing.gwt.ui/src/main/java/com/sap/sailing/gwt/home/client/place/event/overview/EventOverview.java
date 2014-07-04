package com.sap.sailing.gwt.home.client.place.event.overview;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventOverview extends Composite {
    private static EventOverviewUiBinder uiBinder = GWT.create(EventOverviewUiBinder.class);

    interface EventOverviewUiBinder extends UiBinder<Widget, EventOverview> {
    }

    @SuppressWarnings("unused")
    private final EventDTO event;

    public EventOverview(EventDTO event) {
        this.event = event;
        
        EventOverviewResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        updateUI();
    }
    
    private void updateUI() {
    }
}
