package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;

public class TriggerUponEventsSimpleNode extends AutoPlayNodeBase {
    private ArrayList<Type<?>> remainingEventsBeforeTrigger = new ArrayList<>();
    private boolean isSuspended;
    private boolean isDone = false;

    public TriggerUponEventsSimpleNode(Orchestrator orchestrator, Place thisSlidePlace,
            Type<?>[] transitionEvents) {
        super(orchestrator, thisSlidePlace);
        remainingEventsBeforeTrigger.addAll(Arrays.asList(transitionEvents));
    }

    public void onStart() {
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
    @Override
    public void process(GwtEvent<?> event) {
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