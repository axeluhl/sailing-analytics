package com.sap.sailing.gwt.home.client.place.event.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.AbstractEventComposite;
import com.sap.sailing.gwt.home.client.place.event.EventPlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventMedia extends AbstractEventComposite {
    private static EventMediaUiBinder uiBinder = GWT.create(EventMediaUiBinder.class);

    interface EventMediaUiBinder extends UiBinder<Widget, EventMedia> {
    }

    public EventMedia(EventDTO event, EventPlaceNavigator placeNavigator) {
        super(event, placeNavigator);
        
        EventMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
    
}
