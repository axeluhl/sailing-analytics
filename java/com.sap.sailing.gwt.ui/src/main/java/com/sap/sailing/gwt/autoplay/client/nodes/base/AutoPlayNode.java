package com.sap.sailing.gwt.autoplay.client.nodes.base;

import com.google.web.bindery.event.shared.EventBus;

public interface AutoPlayNode {
    void start(EventBus eventBus);
    void stop();

}