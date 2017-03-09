package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchOrchestrator;

public abstract class SlideConfigBase implements SlideConfig {

    private Place placeToGo;
    private SlideConfig nextSlide;
    private SixtyInchOrchestrator orchestrator;

    protected SlideConfigBase(SixtyInchOrchestrator orchestrator, Place placeToGo) {
        this.orchestrator = orchestrator;
        this.placeToGo = placeToGo;
    }

    public void setNextSlide(SlideConfig nextSlide) {
        this.nextSlide = nextSlide;
    }

    @Override
    public final void start() {
        orchestrator.doStart(this);
        onStart();
    }

    public abstract void onStart();

    protected void fireTransition() {
        if (nextSlide != null) {
            nextSlide.start();
        } else {
            orchestrator.doStart(null);
        }
    }

    @Override
    public Place getPlaceToGo() {
        return placeToGo;
    }

}