package com.sap.sailing.gwt.home.client.place.event2;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public interface EventView extends IsWidget {

    public interface Presenter {
        EventContext getCtx();
        void handleTabPlaceSelection(TabActivity<?, EventContext> selectedActivity);        
        void navigateTo(Place place);
    }

    /**
     * This is the presenter the view can talk to.
     * 
     * @param currentPresenter
     */
    void registerPresenter(Presenter currentPresenter);

    /**
     * Tell the view to process tabbar place navigation
     * 
     * @param place
     */
    void navigateTabsTo(AbstractEventPlace place);
    
}
