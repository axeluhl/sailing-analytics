package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;

public interface SeriesView<PLACE extends AbstractSeriesPlace, PRES extends SeriesView.Presenter> extends IsWidget {

    public interface Presenter {
        SeriesContext getCtx();

        void handleTabPlaceSelection(TabView<?, ? extends Presenter> selectedActivity);

        SafeUri getUrl(Place place);
        void navigateTo(Place place);
        boolean needsSelectionInHeader();
        void forPlaceSelection(PlaceCallback callback);
        String getEventName();
    }
    
    public interface PlaceCallback {
        void forPlace(AbstractSeriesPlace place, String title);
    }

    /**
     * This is the presenter the view can talk to.
     * 
     * @param currentPresenter
     */
    void registerPresenter(PRES currentPresenter);

    /**
     * Tell the view to process tabbar place navigation
     * 
     * @param place
     */
    void navigateTabsTo(PLACE place);
    
}
