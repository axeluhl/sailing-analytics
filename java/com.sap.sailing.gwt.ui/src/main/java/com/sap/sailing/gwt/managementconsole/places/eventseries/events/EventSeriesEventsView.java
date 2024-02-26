package com.sap.sailing.gwt.managementconsole.places.eventseries.events;

import java.util.List;

import com.sap.sailing.gwt.common.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.common.communication.event.EventSeriesMetadataDTO;
import com.sap.sailing.gwt.managementconsole.mvp.View;
import com.sap.sailing.gwt.managementconsole.partials.contextmenu.HasContextMenuView;
import com.sap.sailing.gwt.managementconsole.places.event.EventPresenter;

public interface EventSeriesEventsView
        extends View<EventSeriesEventsView.Presenter>, HasContextMenuView<EventMetadataDTO> {

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter, EventPresenter {

        void requestContextMenu(final EventSeriesMetadataDTO eventSeries);

    }

    void renderEvents(List<EventMetadataDTO> events);

    void showContextMenu(EventSeriesMetadataDTO eventSeries);

}
