package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;

public interface SeriesTabView<PLACE extends Place> extends TabView<PLACE, SeriesTabsView.Presenter> {
    public interface Presenter extends SeriesView.Presenter {
    }

}
