package com.sap.sailing.gwt.home.mobile.places.event.latestnews;

import java.util.List;

import com.sap.sailing.gwt.home.communication.event.news.GetEventOverviewNewsAction;
import com.sap.sailing.gwt.home.communication.event.news.NewsEntryDTO;
import com.sap.sailing.gwt.home.mobile.partials.updatesBox.UpdatesBox;
import com.sap.sailing.gwt.home.mobile.places.event.AbstractEventView;

public class LatestNewsViewImpl extends AbstractEventView<LatestNewsView.Presenter> implements LatestNewsView {

    private final UpdatesBox updatesBox;

    public LatestNewsViewImpl(LatestNewsView.Presenter presenter) {
        super(presenter, false, true);
        setViewContent(updatesBox = new UpdatesBox(presenter, refreshManager));
        updatesBox.setDontHide(true);
        updatesBox.setDontDrillDown(true);
        refreshManager.add(updatesBox, new GetEventOverviewNewsAction(getEventId()));
    }

    @Override
    public void showNews(List<NewsEntryDTO> news) {
        updatesBox.setData(news);
    }
}
