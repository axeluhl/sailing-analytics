package com.sap.sailing.gwt.home.shared.partials.busy;

import com.google.gwt.user.client.ui.Composite;
import com.sap.sailing.gwt.ui.client.refresh.BusyView;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;

public class BusyViewImpl extends Composite implements BusyView {

    public BusyViewImpl() {
        BusyViewResources.INSTANCE.css().ensureInjected();
        initWidget(new SimpleBusyIndicator(true, 1.0f, "", BusyViewResources.INSTANCE.css().busy()));
    }

}
