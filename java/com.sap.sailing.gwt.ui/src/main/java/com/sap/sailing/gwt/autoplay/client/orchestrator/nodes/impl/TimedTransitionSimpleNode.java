package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;

public abstract class TimedTransitionSimpleNode
        extends AutoPlayNodeBase {

    private Place placeToGo;


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
        if (placeToGo != null) {
            getBus().fireEvent(new PlaceChangeEvent(placeToGo));
        }
    }


}