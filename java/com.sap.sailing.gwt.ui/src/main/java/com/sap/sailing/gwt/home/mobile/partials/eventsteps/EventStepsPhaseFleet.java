package com.sap.sailing.gwt.home.mobile.partials.eventsteps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;

public class EventStepsPhaseFleet extends UIObject {

    private static EventStepsPhaseFleetUiBinder uiBinder = GWT.create(EventStepsPhaseFleetUiBinder.class);

    interface EventStepsPhaseFleetUiBinder extends UiBinder<Element, EventStepsPhaseFleet> {
    }

    @UiField DivElement progressLiveUi;
    @UiField DivElement progressUi;

    public EventStepsPhaseFleet(double finishedWidth, double liveWidth, double height, String color) {
        setElement(uiBinder.createAndBindUi(this));
        getElement().getStyle().setHeight(height, Unit.PCT);
        progressUi.getStyle().setWidth(finishedWidth, Unit.PCT);
        progressUi.getStyle().setBackgroundColor(color);
        progressLiveUi.getStyle().setWidth(liveWidth, Unit.PCT);
        progressLiveUi.getStyle().setBackgroundColor("#ff4040");
    }

}
