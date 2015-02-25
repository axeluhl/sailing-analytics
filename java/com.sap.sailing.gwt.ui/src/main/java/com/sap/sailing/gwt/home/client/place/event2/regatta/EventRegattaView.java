package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.sap.sailing.gwt.home.client.place.event2.EventView;

public interface EventRegattaView extends EventView<AbstractEventRegattaPlace, EventRegattaView.Presenter> {

    public interface Presenter extends EventView.Presenter {

        void gotoOverview();


    }
}
