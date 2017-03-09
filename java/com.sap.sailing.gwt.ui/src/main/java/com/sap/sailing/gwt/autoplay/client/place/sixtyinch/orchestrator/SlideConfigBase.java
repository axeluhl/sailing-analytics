package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchOrchestrator;

public abstract class SlideConfigBase implements SlideConfig {

    private Place placeToGo;
    private SlideConfigBase nextSlide;
    private SixtyInchOrchestrator orchestrator;

    protected SlideConfigBase(SixtyInchOrchestrator orchestrator, Place placeToGo, SlideConfigBase nextSlide) {
        this.orchestrator = orchestrator;
        this.placeToGo = placeToGo;
        this.nextSlide = nextSlide;
    }

    @Override
    public final void start() {
        orchestrator.doStart(this);
        onStart();
    }

    public abstract void onStart();

    protected void fireTransition() {
        nextSlide.start();
    }

    @Override
    public Place getPlaceToGo() {
        return placeToGo;
    }

}