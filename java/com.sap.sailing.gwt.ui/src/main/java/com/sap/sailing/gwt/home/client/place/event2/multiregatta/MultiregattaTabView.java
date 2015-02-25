package com.sap.sailing.gwt.home.client.place.event2.multiregatta;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;

public interface MultiregattaTabView<PLACE extends Place> extends
        TabView<PLACE, EventContext, EventMultiregattaView.Presenter> {

}
