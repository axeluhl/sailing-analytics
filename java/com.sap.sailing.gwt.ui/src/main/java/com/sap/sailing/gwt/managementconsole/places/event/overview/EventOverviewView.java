package com.sap.sailing.gwt.managementconsole.places.event.overview;

import java.util.List;

import com.google.gwt.user.client.ui.RequiresResize;
import com.sap.sailing.gwt.managementconsole.mvp.View;
import com.sap.sailing.gwt.managementconsole.partials.contextmenu.HasContextMenuView;
import com.sap.sailing.gwt.ui.shared.EventDTO;


public interface EventOverviewView
        extends View<EventOverviewView.Presenter>, HasContextMenuView<EventDTO>, RequiresResize {

    void renderEvents(List<EventDTO> events);

    interface Presenter
            extends com.sap.sailing.gwt.managementconsole.mvp.Presenter, HasContextMenuView.Presenter<EventDTO> {

        void reloadEventList();
        
        void navigateToCreateEvent();

        void navigateToEvent(EventDTO event);

        void advancedSettings(EventDTO event);

        void deleteEvent(EventDTO event);
    }

}