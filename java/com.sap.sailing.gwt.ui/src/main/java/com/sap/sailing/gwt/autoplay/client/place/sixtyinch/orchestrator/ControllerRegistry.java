package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNodeController;

public class ControllerRegistry {

    private Map<Class<AutoPlayNode>, AutoPlayNodeController> controllerRegistry = new HashMap<Class<AutoPlayNode>, AutoPlayNodeController>();

    public <N extends AutoPlayNode> void addToRegisty(AutoPlayNodeController<N> controller) {
        controllerRegistry.put((Class<AutoPlayNode>) controller.getNodeRef(), controller);
    }

    public <N extends AutoPlayNode> AutoPlayNodeController<N> getController(N node) {
        return controllerRegistry.get(node);
    }
}