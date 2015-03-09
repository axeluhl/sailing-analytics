package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.sap.sailing.gwt.home.client.app.PlaceNavigation;
import com.sap.sailing.gwt.home.client.place.event2.EventView;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaRacesPlace;
import com.sap.sse.gwt.client.player.Timer;

public interface EventRegattaView extends EventView<AbstractEventRegattaPlace, EventRegattaView.Presenter> {

    public interface Presenter extends EventView.Presenter {

        PlaceNavigation<RegattaRacesPlace> getCurrentRegattaOverviewNavigation();
        
        Timer getAutoRefreshTimer();
    }
}
