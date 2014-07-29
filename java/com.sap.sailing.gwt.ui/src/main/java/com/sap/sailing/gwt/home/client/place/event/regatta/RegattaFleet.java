package com.sap.sailing.gwt.home.client.place.event.regatta;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class RegattaFleet extends Composite {
    private static RegattaFleetUiBinder uiBinder = GWT.create(RegattaFleetUiBinder.class);

    interface RegattaFleetUiBinder extends UiBinder<Widget, RegattaFleet> {
    }

    public RegattaFleet() {
        
        RegattaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
