package com.sap.sailing.gwt.autoplay.client.nodes.base;

import com.google.web.bindery.event.shared.EventBus;

public interface AutoPlayNode {
    String getName();
    void start(EventBus eventBus);
    void stop();

    void log(String logMessage);
}