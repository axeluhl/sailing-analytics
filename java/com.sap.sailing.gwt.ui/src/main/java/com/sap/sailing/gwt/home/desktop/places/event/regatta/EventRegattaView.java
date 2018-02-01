package com.sap.sailing.gwt.home.desktop.places.event.regatta;

import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.home.desktop.places.event.regatta.overviewtab.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.security.ui.client.UserService;

public interface EventRegattaView extends EventView<AbstractEventRegattaPlace, EventRegattaView.Presenter> {

    public interface Presenter extends EventView.Presenter {

        PlaceNavigation<RegattaOverviewPlace> getCurrentRegattaOverviewNavigation();
        
        Timer getAutoRefreshTimer();
        
        UserService getUserService();
    }
}
