package com.sap.sailing.gwt.managementconsole.places.eventseries.overview;

import java.util.List;

import com.sap.sailing.gwt.common.communication.event.EventSeriesMetadataDTO;
import com.sap.sailing.gwt.managementconsole.mvp.View;

public interface EventSeriesOverviewView extends View<EventSeriesOverviewView.Presenter> {

    void renderEventSeries(List<EventSeriesMetadataDTO> eventSeries);

    interface Presenter extends com.sap.sailing.gwt.managementconsole.mvp.Presenter {
    }

}
