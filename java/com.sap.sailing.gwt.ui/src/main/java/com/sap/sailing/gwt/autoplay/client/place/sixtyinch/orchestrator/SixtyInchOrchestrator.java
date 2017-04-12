package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.ResettableEventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNodeController;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.IdleUpNextController;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.IdleUpNextNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.LifeRaceWithRacemapController;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.LifeRaceWithRacemapNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.PreRaceWithRacemapController;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.RaceEndWithBoatsNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.RaceEndWithBoatsController;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.RaceEndWithBoatsNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.StartupController;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.StartupNode;

public class SixtyInchOrchestrator implements Orchestrator {

    private AutoPlayClientFactorySixtyInch cf;

    private AutoPlayNodeReference currentNodeRef;

    private final LifeRacesMonitor racesMonitor;

    private ControllerRegistry controllerRegistry = new ControllerRegistry();

    public SixtyInchOrchestrator(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;

        controllerRegistry.addToRegisty(new StartupController(cf));
        controllerRegistry.addToRegisty(new IdleUpNextController(cf));
        controllerRegistry.addToRegisty(new PreRaceWithRacemapController(cf));
        controllerRegistry.addToRegisty(new LifeRaceWithRacemapController(cf));
        controllerRegistry.addToRegisty(new RaceEndWithBoatsController(cf));

        racesMonitor = new LifeRacesMonitor(cf);
        racesMonitor.setIdleStartNodeRef(new IdleUpNextNode());
        racesMonitor.setPreLifeRaceNodeRef(new RaceEndWithBoatsNode());
        racesMonitor.setLifeRaceNodeRef(new LifeRaceWithRacemapNode());
        racesMonitor.setAfterRaceNodeRef(new RaceEndWithBoatsNode());
        racesMonitor.startMonitoring();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.Orchestrator#start()
     */
    @Override
    public void start() {
        GWT.log("Starting orchestrator");
        cf.getEventBus().addHandler(AutoplayFailureEvent.TYPE, new AutoplayFailureEvent.Handler() {
            @Override
            public void onFailure(AutoplayFailureEvent e) {
                processFailure(e);
            }
        });
        cf.getEventBus().addHandler(DataLoadFailureEvent.TYPE, new DataLoadFailureEvent.Handler() {
            @Override
            public void onLoadFailure(DataLoadFailureEvent e) {
                processFailure(e);
            }
        });

    }

    private void processFailure(FailureEvent event) {
        GWT.log("Captured failure event: " + event);
        if (event.getCaught() != null) {
            event.getCaught().printStackTrace();
        }
        if (currentNodeRef != null) {
            currentNodeRef.stop();
        }
        transitionToNode(new StartupNode());
    }

    private AutoPlayNodeController<?> getControllerForNode(AutoPlayNode node) {
        return controllerRegistry.getController(node);
    }

    @Override
    public void transitionToNode(AutoPlayNode requestedNode) {
        if (requestedNode == null) {
            GWT.log("Found no successor Node, staying at current node, validate Orchestrator");
            return;
        }
        if (currentNodeRef != null) {
            currentNodeRef.stop();
        }
        AutoPlayNodeController<?> autoPlayController = getControllerForNode(requestedNode);
        currentNodeRef = new AutoPlayNodeReference(this, cf.getEventBus(), autoPlayController);

        currentNodeRef.start(requestedNode);
    }

    private static class AutoPlayNodeReference {
        private ResettableEventBus bus;
        private AutoPlayNodeController controller;

        public AutoPlayNodeReference(Orchestrator orchestrator, EventBus bus, AutoPlayNodeController controller) {
            this.bus = new ResettableEventBus(bus);
            this.controller = controller;
        }

        public void start(AutoPlayNode node) {
            controller.start(node, this.bus);
        }

        public void stop() {

            try {
                controller.stop();
            } catch (Exception e) {
                GWT.log("Failed to stop current node", e);
            }
            bus.removeHandlers();
        }
    }

}
