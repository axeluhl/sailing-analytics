package com.sap.sailing.gwt.autoplay.client.nodes.base;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.ResettableEventBus;

public abstract class AutoPlayNodeBase implements AutoPlayNode {
    private ResettableEventBus bus;
    private boolean isStopped;

    @Override
    public final void start(EventBus bus) {
        isStopped = false;
        this.bus = new ResettableEventBus(bus);
        onStart();
    }

    @Override
    public final void stop() {
        isStopped = true;
        onStop();
        if (bus != null) {
            this.bus.removeHandlers();
        }
    }

    public abstract void onStart();

    public void onStop() {
    };

    public boolean isStopped() {
        return isStopped;
    }

    protected EventBus getBus() {
        return bus;
    }

}