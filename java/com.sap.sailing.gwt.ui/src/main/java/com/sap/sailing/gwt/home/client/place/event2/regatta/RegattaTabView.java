package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;

public interface RegattaTabView<PLACE extends Place> extends
        TabView<PLACE, EventContext, EventRegattaView.Presenter> {

}
