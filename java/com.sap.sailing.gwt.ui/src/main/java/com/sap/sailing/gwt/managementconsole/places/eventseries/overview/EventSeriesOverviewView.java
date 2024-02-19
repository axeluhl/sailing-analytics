package com.sap.sailing.gwt.managementconsole.places.eventseries.overview;

import java.util.List;

import com.google.gwt.user.client.ui.RequiresResize;
import com.sap.sailing.gwt.common.communication.event.EventSeriesMetadataDTO;
import com.sap.sailing.gwt.managementconsole.mvp.View;
import com.sap.sailing.gwt.managementconsole.partials.contextmenu.HasContextMenuView;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface EventSeriesOverviewView
        extends View<EventSeriesOverviewView.Presenter>, HasContextMenuView<EventSeriesMetadataDTO>, RequiresResize {

    void renderEventSeries(List<EventSeriesMetadataDTO> eventSeries);

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter,
            HasContextMenuView.Presenter<EventSeriesMetadataDTO> {

        void reloadEventSeriesList();

        void navigateToCreateEventSeries();

        void navigateToEventSeries(EventSeriesMetadataDTO eventSeries);

        void advancedSettings(ManagementConsoleResources app_res, EventSeriesMetadataDTO eventSeries);

        void deleteEventSeries(EventSeriesMetadataDTO eventSeries);
    }

}
