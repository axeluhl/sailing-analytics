package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;

public class TriggerUponEventsSimpleNode extends AutoPlaySingleNextSlideNodeBase {

    private ArrayList<Type<?>> remainingEventsBeforeTrigger = new ArrayList<>();
    private boolean isSuspended;
    private boolean isDone = false;
    private final Place placeToGo;


    public TriggerUponEventsSimpleNode(Place placeToGo,
            Type<?>[] transitionEvents) {
        this.placeToGo = placeToGo;
        remainingEventsBeforeTrigger.addAll(Arrays.asList(transitionEvents));
    }

    public void onStart() {
        GWT.log("Starting node: " + toString());
        getBus().fireEvent(new PlaceChangeEvent(placeToGo));
    }

    public <H extends EventHandler> void registerEventToWaitFor(GwtEvent.Type<H> type, H handler) {
        getBus().addHandler(type, handler);
    }

    @Override
    public void doContinue() {
        isSuspended = false;
        if (isDone) {
            return;
        }
        if (remainingEventsBeforeTrigger.isEmpty()) {
            isDone = true;
            fireTransition();
        }
    }

    public void consume(GwtEvent<?> event) {
        if (isDone) {
            return;
        }
        remainingEventsBeforeTrigger.remove(event.getAssociatedType());
        if (remainingEventsBeforeTrigger.isEmpty()) {
            if (!isSuspended) {
                isDone = true;
                fireTransition();
            }
        }
    }

    @Override
    public void doSuspend() {
        isSuspended = true;
    }

    @Override
    public void stop() {
        doSuspend();
    }

}