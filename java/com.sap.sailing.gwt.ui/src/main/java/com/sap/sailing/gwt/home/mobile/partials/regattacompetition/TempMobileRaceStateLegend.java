package com.sap.sailing.gwt.home.mobile.partials.regattacompetition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.regattanavigation.RegattaNavigationResources;

public class TempMobileRaceStateLegend extends Widget {

    private static RaceStateLegendUiBinder uiBinder = GWT.create(RaceStateLegendUiBinder.class);

    interface RaceStateLegendUiBinder extends UiBinder<Element, TempMobileRaceStateLegend> {
    }

    public TempMobileRaceStateLegend() {
        RegattaNavigationResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));
    }

}
