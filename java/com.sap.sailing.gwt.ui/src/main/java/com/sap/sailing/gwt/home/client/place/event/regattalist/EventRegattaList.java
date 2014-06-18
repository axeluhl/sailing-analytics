package com.sap.sailing.gwt.home.client.place.event.regattalist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventRegattaList extends Composite {
    private static EventRegattaHeaderUiBinder uiBinder = GWT.create(EventRegattaHeaderUiBinder.class);

    interface EventRegattaHeaderUiBinder extends UiBinder<Widget, EventRegattaList> {
    }

    @SuppressWarnings("unused")
    private final EventDTO event;
    
    public EventRegattaList(EventDTO event) {
        this.event = event;
        
        EventRegattaListResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
}
