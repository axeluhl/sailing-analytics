package com.sap.sailing.gwt.home.client.place.event2;

import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventPlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.shared.eventview.HasRegattaMetadata;
import com.sap.sse.gwt.client.player.Timer;

public interface EventView<PLACE extends AbstractEventPlace, PRES extends EventView.Presenter> extends IsWidget {

    public interface Presenter extends EventPlaceNavigator {
        EventContext getCtx();

        void handleTabPlaceSelection(TabView<?, ? extends Presenter> selectedActivity);

        SafeUri getUrl(Place place);
        void navigateTo(Place place);
        boolean needsSelectionInHeader();
        void forPlaceSelection(PlaceCallback callback);
        Timer getTimerForClientServerOffset();
        HomePlacesNavigator getHomePlaceNavigator();
        SailingServiceAsync getSailingService();
        
        AbstractEventRegattaPlace getPlaceForRegatta(String regattaId);
        AbstractEventRegattaPlace getPlaceForRegattaRaces(String regattaId);

        boolean showRegattaMetadata();
        HasRegattaMetadata getRegattaMetadata();
    }
    
    public interface PlaceCallback {
        void forPlace(AbstractEventPlace place, String title, boolean active);
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
