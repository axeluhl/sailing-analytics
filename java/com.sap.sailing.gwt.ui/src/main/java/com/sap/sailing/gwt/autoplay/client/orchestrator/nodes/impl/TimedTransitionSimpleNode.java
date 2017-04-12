package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;

public abstract class TimedTransitionSimpleNode
        extends AutoPlayNodeBase {

    private Place placeToGo;
    private boolean isRunning = true;



    public void onStart() {
        firePlaceChangeAndStartTimer();
    }

    public void setPlaceToGo(Place placeToGo) {
        this.placeToGo = placeToGo;
    }

    protected Place getPlaceToGo() {
        return placeToGo;
    }

    protected void firePlaceChangeAndStartTimer() {
        isRunning = true;
        if (placeToGo != null) {
            getBus().fireEvent(new PlaceChangeEvent(placeToGo));
        }
    }

    @Override
    public void doSuspend() {
        isRunning = false;
    }

    @Override
    public void doContinue() {
        isRunning = true;
    }

    @Override
    public void stop() {
        isRunning = false;
    }

}