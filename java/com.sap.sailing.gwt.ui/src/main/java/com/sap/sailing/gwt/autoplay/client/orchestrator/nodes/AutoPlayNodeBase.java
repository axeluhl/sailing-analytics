package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;

public abstract class AutoPlayNodeBase implements AutoPlayNode {

    private Place placeToGo;
    private AutoPlayNode nextSlide;
    private Orchestrator orchestrator;

    protected AutoPlayNodeBase(Orchestrator orchestrator, Place placeToGo) {
        this.orchestrator = orchestrator;
        this.placeToGo = placeToGo;
    }

    public void setNextSlide(AutoPlayNode nextSlide) {
        this.nextSlide = nextSlide;
    }

    @Override
    public final void start() {
        orchestrator.didMoveToSlide(this);
        onStart();
    }

    public abstract void onStart();

    protected void fireTransition() {
        GWT.log("fired transition in " + toString());
        if (nextSlide != null) {
            nextSlide.start();
        } else {
            orchestrator.didMoveToSlide(null);
        }
    }

    @Override
    public Place getPlaceToGo() {
        return placeToGo;
    }

}