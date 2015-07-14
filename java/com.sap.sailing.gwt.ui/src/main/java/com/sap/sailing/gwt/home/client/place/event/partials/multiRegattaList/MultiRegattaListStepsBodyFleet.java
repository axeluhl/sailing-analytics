package com.sap.sailing.gwt.home.client.place.event.partials.multiRegattaList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;

public class MultiRegattaListStepsBodyFleet extends UIObject {

    private static MultiRegattaListStepsBodyFleetUiBinder uiBinder = GWT.create(MultiRegattaListStepsBodyFleetUiBinder.class);

    interface MultiRegattaListStepsBodyFleetUiBinder extends UiBinder<Element, MultiRegattaListStepsBodyFleet> {
    }

    @UiField DivElement fleetProgressBarUi;
    @UiField DivElement fleetProgressBarLiveUi;

    public MultiRegattaListStepsBodyFleet(double finishedWidth, double liveWidth, double height, String color) {
        setElement(uiBinder.createAndBindUi(this));
        getElement().getStyle().setHeight(height, Unit.PCT);
        fleetProgressBarUi.getStyle().setWidth(finishedWidth, Unit.PCT);
        fleetProgressBarUi.getStyle().setBackgroundColor(color);
        fleetProgressBarLiveUi.getStyle().setWidth(liveWidth, Unit.PCT);
        fleetProgressBarLiveUi.getStyle().setBackgroundColor("#ff4040");
    }
    
}
