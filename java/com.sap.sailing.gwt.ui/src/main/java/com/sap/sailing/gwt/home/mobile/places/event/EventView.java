package com.sap.sailing.gwt.home.mobile.places.event;

import java.util.List;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.place.event.EventContext;
import com.sap.sailing.gwt.home.mobile.partials.statisticsBox.StatisticsDTO;
import com.sap.sailing.gwt.home.shared.dispatch.DispatchSystem;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;

public interface EventView {

    Widget asWidget();
    
    HasSelectionHandlers<String> getQuickfinder();

    void setSailorInfos(String description, String buttonLabel, String url);
    
    void setQuickFinderValues(List<RegattaMetadataDTO> regattaMetadatas);

    public interface Presenter {
        EventContext getCtx();

        DispatchSystem getDispatch();
    }

    void setStatistics(StatisticsDTO statistics);
}

