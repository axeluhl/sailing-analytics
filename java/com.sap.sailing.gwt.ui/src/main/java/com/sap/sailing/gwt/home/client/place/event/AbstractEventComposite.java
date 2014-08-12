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
    private final EventPageNavigator pageNavigator;

    public AbstractEventComposite(EventDTO event, EventPageNavigator pageNavigator) {
        this.event = event;
        this.pageNavigator = pageNavigator;
    }

    public EventDTO getEvent() {
        return event;
    }

    public EventPageNavigator getPageNavigator() {
        return pageNavigator;
    }
}
