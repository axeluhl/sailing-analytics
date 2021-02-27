package com.sap.sailing.gwt.managementconsole.places.event.overview;

import java.util.List;

import com.google.gwt.user.client.ui.RequiresResize;
import com.sap.sailing.gwt.managementconsole.mvp.View;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public interface EventOverviewView extends View<EventOverviewView.Presenter>, RequiresResize {

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter {
        void reloadEventList();

        void navigateToEvent(final EventDTO event);
        
        void navigateToCreateEvent();
    }

    void renderEvents(List<EventDTO> events);

}
