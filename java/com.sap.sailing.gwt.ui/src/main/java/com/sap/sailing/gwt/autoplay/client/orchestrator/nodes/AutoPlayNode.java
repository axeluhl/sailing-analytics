package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes;

import com.google.web.bindery.event.shared.EventBus;

public interface AutoPlayNode {
    void start(EventBus eventBus);
    void stop();

}