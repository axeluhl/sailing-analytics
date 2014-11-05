package com.sap.sailing.gwt.home.client.place.event;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.ui.shared.EventDTO;

/**
 * A base class for a content composite related to an sailing event  
 * @author Frank
 *
 */
public class AbstractEventComposite extends Composite {
    private final EventDTO event;
    private final EventPlaceNavigator pageNavigator;

    public AbstractEventComposite(EventDTO event, EventPlaceNavigator pageNavigator) {
        this.event = event;
        this.pageNavigator = pageNavigator;
    }

    public EventDTO getEvent() {
        return event;
    }

    public EventPlaceNavigator getPageNavigator() {
        return pageNavigator;
    }
}
