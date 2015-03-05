package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.series.SeriesClientFactory;

public class SeriesTabActivity extends AbstractSeriesActivity<AbstractSeriesTabPlace> implements SeriesTabsView.Presenter {

    private SeriesTabsView currentView = new TabletAndDesktopSeriesView();

    public SeriesTabActivity(AbstractSeriesTabPlace place, SeriesClientFactory clientFactory, HomePlacesNavigator homePlacesNavigator) {
        super(place, clientFactory, homePlacesNavigator);
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        currentView.registerPresenter(this);
        panel.setWidget(currentView);
        currentView.navigateTabsTo(currentPlace);
    }
}
