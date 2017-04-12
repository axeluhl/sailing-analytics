package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes;

import com.google.web.bindery.event.shared.EventBus;

public interface AutoPlayNodeController {
    void start(EventBus eventBus);
    void doSuspend();
    void doContinue();
    void stop();

}