package com.sap.sailing.gwt.home.shared.partials.statistics;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.home.communication.eventlist.EventListYearDTO;

public class YearStatisticsBox extends Composite {

    public YearStatisticsBox(StatisticsBoxView view, EventListYearDTO statistics) {
        initWidget(view.asWidget());
        final YearStatisticsBoxPresenter statisticsBoxPresenter = new YearStatisticsBoxPresenter(view);
        statisticsBoxPresenter.setData(statistics);
    }
}
