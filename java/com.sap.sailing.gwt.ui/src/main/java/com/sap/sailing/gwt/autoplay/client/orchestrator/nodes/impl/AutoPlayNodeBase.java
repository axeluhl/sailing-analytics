package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.ResettableEventBus;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNodeController;

public abstract class AutoPlayNodeBase implements AutoPlayNodeController {
    private ResettableEventBus bus;


    @Override
    public final void start(EventBus bus) {
        this.bus = new ResettableEventBus(bus);
        onStart();
    }

    @Override
    public final void stop() {
        onStop();
        this.bus.removeHandlers();
    }

    public abstract void onStart();

    public void onStop() {
    };

    protected EventBus getBus() {
        return bus;
    }

}