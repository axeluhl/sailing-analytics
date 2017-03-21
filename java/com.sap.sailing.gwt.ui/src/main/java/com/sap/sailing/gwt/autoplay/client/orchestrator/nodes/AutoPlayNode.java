package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;

public interface AutoPlayNode {
    void start(EventBus eventBus, Orchestrator orchestrator);
    void doSuspend();
    void doContinue();
    void stop();
}