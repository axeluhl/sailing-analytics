package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.sap.sailing.gwt.home.client.place.event2.EventView;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;

public interface EventRegattaView extends EventView<AbstractEventRegattaPlace, EventRegattaView.Presenter> {

    public interface Presenter extends EventView.Presenter {

        SailingServiceAsync getSailingService();

    }
}
