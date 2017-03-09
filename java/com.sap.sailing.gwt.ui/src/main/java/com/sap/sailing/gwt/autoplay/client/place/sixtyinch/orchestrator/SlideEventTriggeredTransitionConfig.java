package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.SixtyInchOrchestrator;

public class SlideEventTriggeredTransitionConfig extends SlideConfigBase {
    private ArrayList<Type<?>> remainingEventsBeforeTrigger = new ArrayList<>();
    private boolean isSuspended;

    public SlideEventTriggeredTransitionConfig(SixtyInchOrchestrator orchestrator, Place thisSlidePlace,
            SlideEventTriggeredTransitionConfig nextSlide, Type<?>[] transitionEvents) {
        super(orchestrator, thisSlidePlace, nextSlide);
        remainingEventsBeforeTrigger.addAll(Arrays.asList(transitionEvents));
    }

    public void onStart() {
    }

    @Override
    public void process(GwtEvent<?> event) {
        remainingEventsBeforeTrigger.remove(event.getAssociatedType());
        if (remainingEventsBeforeTrigger.isEmpty()) {
            if (!isSuspended) {
                fireTransition();
            }
        }
    }

    @Override
    public void doSuspend() {
        isSuspended = true;
    }

    @Override
    public void doContinue() {
        isSuspended = false;
        if (remainingEventsBeforeTrigger.isEmpty()) {
            fireTransition();
        }
    }
}