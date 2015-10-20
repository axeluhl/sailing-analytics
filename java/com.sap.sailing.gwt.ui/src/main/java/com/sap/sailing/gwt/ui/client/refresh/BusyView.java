package com.sap.sailing.gwt.ui.client.refresh;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;

public class BusyView extends Composite {

    public BusyView() {
        BusyViewResources.INSTANCE.css().ensureInjected();
        initWidget(new SimpleBusyIndicator(true, 1.0f, "", BusyViewResources.INSTANCE.css().busy()));
    }

}
