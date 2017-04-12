package com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNodeController;

public class AutoPlayLoopNode implements AutoPlayNodeController {
    private EventBus bus;

    private List<AutoPlayNodeController> nodes = new ArrayList<>();
    private AutoPlayNodeController loopEndDestination;

    public AutoPlayLoopNode(AutoPlayNodeController... nodes) {
        this.nodes.addAll(Arrays.asList(nodes));
    }

    @Override
    public final void start(EventBus bus) {
        this.bus = bus;

        // do loop
    }

    protected EventBus getBus() {
        return bus;
    }

    @Override
    public void doSuspend() {
        // TODO Auto-generated method stub
    }

    @Override
    public void doContinue() {
        // TODO Auto-generated method stub
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }

    public void setLoopEndDestination(AutoPlayNodeController loopEndDestination) {
        this.loopEndDestination = loopEndDestination;
    }
}