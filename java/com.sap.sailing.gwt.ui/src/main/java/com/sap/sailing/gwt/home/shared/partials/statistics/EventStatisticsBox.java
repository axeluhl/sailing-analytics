package com.sap.sailing.gwt.home.shared.partials.statistics;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.communication.event.statistics.EventStatisticsDTO;
import com.sap.sailing.gwt.home.shared.refresh.RefreshableWidget;

public class EventStatisticsBox extends Composite implements RefreshableWidget<EventStatisticsDTO> {
    
    private final EventStatisticsBoxPresenter statisticsBoxPresenter;

    public EventStatisticsBox(boolean showRegattaInformation, StatisticsBoxView view) {
        initWidget(view.asWidget());
        statisticsBoxPresenter = new EventStatisticsBoxPresenter(showRegattaInformation, view);
    }
    
    @Override
    public void setData(EventStatisticsDTO statistics) {
        statisticsBoxPresenter.setData(statistics);
    }
}
