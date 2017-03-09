package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.place.shared.Place;

public interface SlideConfig {
    void start();

    void doSuspend();

    void doContinue();

    void process(GwtEvent<?> event);

    Place getPlaceToGo();

    void setNextSlide(SlideConfig nextSlide);
}