package com.sap.sailing.gwt.home.client.place.event.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventMedia extends Composite {
    private static EventMediaUiBinder uiBinder = GWT.create(EventMediaUiBinder.class);

    interface EventMediaUiBinder extends UiBinder<Widget, EventMedia> {
    }

    @SuppressWarnings("unused")
    private final EventDTO event;
    
    public EventMedia(EventDTO event) {
        this.event = event;
        
        EventMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
}
