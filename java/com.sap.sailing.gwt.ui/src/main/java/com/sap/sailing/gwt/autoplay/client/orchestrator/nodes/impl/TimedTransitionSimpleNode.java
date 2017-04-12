package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.user.client.Timer;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;

public abstract class TimedTransitionSimpleNode<NODE extends AutoPlayNode>
        extends AutoPlaySingleNextSlideNodeBase<NODE> {

    private final int displayDurationInMs;
    private final Timer transitionTimer;
    private Place placeToGo;
    private String nodeName;
    private boolean isRunning = true;

    public TimedTransitionSimpleNode(String nodeName, Place placeToGo, int displayDurationInMs) {
        this.nodeName = nodeName;
        this.placeToGo = placeToGo;
        this.displayDurationInMs = displayDurationInMs;
        this.transitionTimer = new Timer() {
            @Override
            public void run() {
                GWT.log("Timed transition triggered by " + nodeName + " state: " + isRunning);
                if (isRunning) {
                    fireTransition();
                }
            }
        };
    }

    protected TimedTransitionSimpleNode(String nodeName, int displayDurationInMs) {
        this.nodeName = nodeName;
        this.displayDurationInMs = displayDurationInMs;
        this.transitionTimer = new Timer() {
            @Override
            public void run() {
                GWT.log("Timed transition triggered by " + nodeName + " state: " + isRunning);
                if (isRunning) {
                    fireTransition();
                }
            }
        };
    }

    public void onStart() {
        firePlaceChangeAndStartTimer();
    }

    public void setPlaceToGo(Place placeToGo) {
        this.placeToGo = placeToGo;
    }

    protected void firePlaceChangeAndStartTimer() {
        isRunning = true;
        if (placeToGo != null) {
            getBus().fireEvent(new PlaceChangeEvent(placeToGo));
        }
        if (!transitionTimer.isRunning())
            transitionTimer.schedule(this.displayDurationInMs);
    }

    @Override
    public void doSuspend() {
        GWT.log("Suspended node: " + nodeName);
        transitionTimer.cancel();
        isRunning = false;
    }

    @Override
    public void doContinue() {
        GWT.log("Continuing node: " + nodeName);
        transitionTimer.schedule(this.displayDurationInMs);
        isRunning = true;
    }

    @Override
    public void stop() {
        GWT.log("Stopping node: " + nodeName);
        transitionTimer.cancel();
        isRunning = false;
    }

    @Override
    public String toString() {
        return nodeName;
    }
}