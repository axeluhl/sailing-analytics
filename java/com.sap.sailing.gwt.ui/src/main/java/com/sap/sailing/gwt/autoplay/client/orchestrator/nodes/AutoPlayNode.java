package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.place.shared.Place;

public interface AutoPlayNode {
    void start();

    void stop();

    void doSuspend();

    void doContinue();

    void process(GwtEvent<?> event);

    Place getPlaceToGo();

    void setNextSlide(AutoPlayNode nextSlide);
}