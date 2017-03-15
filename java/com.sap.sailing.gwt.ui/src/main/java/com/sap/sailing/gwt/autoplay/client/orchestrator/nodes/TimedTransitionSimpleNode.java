package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Timer;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.Slide7Place;

public class TimedTransitionSimpleNode extends AutoPlayNodeBase {
    private final int displayDurationInMs;
    private final Timer loadTimer;

    public TimedTransitionSimpleNode(Orchestrator orchestrator, Place thisSlidePlace,
            int displayDurationInMs) {
        super(orchestrator, thisSlidePlace);
        this.displayDurationInMs = displayDurationInMs;
        this.loadTimer = new Timer() {
            @Override
            public void run() {
                fireTransition();
            }
        };
    }

    public void onStart() {
        GWT.log("Starting slide: " + toString());
        if (getPlaceToGo() instanceof Slide7Place)
            GWT.debugger();
        loadTimer.schedule(this.displayDurationInMs);
    }

    @Override
    public void doSuspend() {
        GWT.log("Suspended slide: " + toString());
        loadTimer.cancel();
    }

    @Override
    public void doContinue() {
        GWT.log("Continuing slide: " + toString());
        loadTimer.schedule(this.displayDurationInMs);
    }

    @Override
    public void process(GwtEvent<?> event) {
        // maybe react to error events?
    }

    @Override
    public void stop() {
        GWT.log("Stoppin slide: " + toString());
        loadTimer.cancel();
    }

    @Override
    public String toString() {
        return "SlideTimedTransitionConfig for " + getPlaceToGo().getClass().getName();
    }
}