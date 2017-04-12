package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes;

import com.google.web.bindery.event.shared.EventBus;

public interface AutoPlayNodeController<NODE extends AutoPlayNode> {
    void start(NODE node, EventBus eventBus);
    void doSuspend();
    void doContinue();
    void stop();

    Class<NODE> getNodeRef();
}