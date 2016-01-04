package com.sap.sailing.gwt.home.desktop.places.event.regatta;

import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sse.gwt.client.player.Timer;

public interface EventRegattaView extends EventView<AbstractEventRegattaPlace, EventRegattaView.Presenter> {

    public interface Presenter extends EventView.Presenter {

        PlaceNavigation<RegattaOverviewPlace> getCurrentRegattaOverviewNavigation();
        
        Timer getAutoRefreshTimer();
    }
}
