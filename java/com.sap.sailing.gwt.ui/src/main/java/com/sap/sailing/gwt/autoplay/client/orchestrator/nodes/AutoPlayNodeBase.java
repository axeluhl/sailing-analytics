package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;

public abstract class AutoPlayNodeBase implements AutoPlayNode {
    private Orchestrator orchestrator;
    private EventBus bus;

    @Override
    public final void start(EventBus bus, Orchestrator orchestrator) {
        this.bus = bus;
        this.orchestrator = orchestrator;
        onStart();
    }

    public abstract void onStart();

    protected EventBus getBus() {
        return bus;
    }

    protected Orchestrator getOrchestrator() {
        return orchestrator;
    }

}