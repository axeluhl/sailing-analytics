package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNodeController;

public abstract class AutoPlayNodeBase implements AutoPlayNodeController {
    private EventBus bus;

    @Override
    public final void start(EventBus bus) {
        this.bus = bus;
        onStart();
    }

    public abstract void onStart();

    protected EventBus getBus() {
        return bus;
    }

}