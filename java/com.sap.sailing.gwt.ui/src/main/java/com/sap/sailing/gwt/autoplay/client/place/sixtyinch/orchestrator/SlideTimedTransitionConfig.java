package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Timer;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchOrchestrator;

public class SlideTimedTransitionConfig extends SlideConfigBase {

    private final int displayDurationInMs;
    private final Timer loadTimer;

    public SlideTimedTransitionConfig(SixtyInchOrchestrator orchestrator, Place thisSlidePlace,
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
        loadTimer.schedule(this.displayDurationInMs);
    }

    @Override
    public void doSuspend() {
        loadTimer.cancel();
    }

    @Override
    public void doContinue() {
        loadTimer.schedule(this.displayDurationInMs);
    }

    @Override
    public void process(GwtEvent<?> event) {
        // maybe react to error events?
    }
}