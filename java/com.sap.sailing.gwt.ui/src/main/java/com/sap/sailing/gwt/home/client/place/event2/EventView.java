package com.sap.sailing.gwt.home.client.place.event2;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventPlaceNavigator;
import com.sap.sse.gwt.client.player.Timer;

public interface EventView<PLACE extends AbstractEventPlace, PRES extends EventView.Presenter> extends IsWidget {

    public interface Presenter extends EventPlaceNavigator {
        EventContext getCtx();

        void handleTabPlaceSelection(TabView<?, ? extends Presenter> selectedActivity);

        String getUrl(Place place);
        void navigateTo(Place place);
        boolean needsSelectionInHeader();
        void forPlaceSelection(PlaceCallback callback);
        String getEventName();
        Timer getTimerForClientServerOffset();
        HomePlacesNavigator getHomePlaceNavigator();
    }
    
    public interface PlaceCallback {
        void forPlace(AbstractEventPlace place, String title);
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
