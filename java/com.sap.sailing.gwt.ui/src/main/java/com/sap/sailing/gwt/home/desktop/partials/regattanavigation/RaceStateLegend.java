package com.sap.sailing.gwt.home.desktop.partials.regattanavigation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class RaceStateLegend extends Widget {

    private static RaceStateLegendUiBinder uiBinder = GWT.create(RaceStateLegendUiBinder.class);

    interface RaceStateLegendUiBinder extends UiBinder<Element, RaceStateLegend> {
    }

    public RaceStateLegend() {
        setElement(uiBinder.createAndBindUi(this));
    }

}
