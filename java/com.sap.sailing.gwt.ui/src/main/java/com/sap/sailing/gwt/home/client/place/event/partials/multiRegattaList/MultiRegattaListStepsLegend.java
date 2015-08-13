package com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class MultiRegattaListStepsLegend extends Widget {

    private static MultiRegattaListStepsLegendUiBinder uiBinder = GWT.create(MultiRegattaListStepsLegendUiBinder.class);

    interface MultiRegattaListStepsLegendUiBinder extends UiBinder<Element, MultiRegattaListStepsLegend> {
    }

    public MultiRegattaListStepsLegend() {
        setElement(uiBinder.createAndBindUi(this));
    }

}
