package com.sap.sailing.gwt.home.client.place.fakeseries;

public interface SeriesTabsView extends SeriesView<AbstractSeriesTabPlace, SeriesTabsView.Presenter> {

    public interface Presenter extends SeriesView.Presenter {
        void gotoOverview();
    }
}
