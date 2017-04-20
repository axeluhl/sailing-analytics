package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.ResettableEventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNodeController;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl.AutoPlayLoopNode;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.impl.AutoPlaySequenceNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.IdleUpNextController;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.LifeRaceWithRacemapController;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.PreRaceWithRacemapController;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.RaceEndWithBoatsController;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.SixtyInchRootNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.StartupController;

public class SixtyInchOrchestrator implements Orchestrator {

    private AutoPlayClientFactorySixtyInch cf;

    private AutoPlayNodeReference currentNodeRef;
    private StartupController root;

    public SixtyInchOrchestrator(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;

        AutoPlayLoopNode idleLoop = new AutoPlayLoopNode(30, new IdleUpNextController(cf));
        AutoPlayLoopNode preLifeRaceLoop = new AutoPlayLoopNode(30, new PreRaceWithRacemapController(cf));
        AutoPlayLoopNode lifeRaceLoop = new AutoPlayLoopNode(30, new LifeRaceWithRacemapController(cf));
        AutoPlayNodeController afterLifeRaceLoop = new AutoPlaySequenceNode(30, new RaceEndWithBoatsController(cf),
                idleLoop);

        SixtyInchRootNode raceLoop = new SixtyInchRootNode(cf, idleLoop, lifeRaceLoop, preLifeRaceLoop,
                afterLifeRaceLoop);

        root = new StartupController(cf);
        root.setWhenReadyDestination(raceLoop);


        transitionToNode(root);

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
        transitionToNode(root);
    }

    @Override
    public void transitionToNode(AutoPlayNodeController autoPlayController) {
        if (autoPlayController == null) {
            GWT.log("Found no successor Node, staying at current node, validate Orchestrator");
            return;
        }
        if (currentNodeRef != null) {
            currentNodeRef.stop();
        }
        currentNodeRef = new AutoPlayNodeReference(this, cf.getEventBus(), autoPlayController);

        currentNodeRef.start();
    }

    private static class AutoPlayNodeReference {
        private ResettableEventBus bus;
        private AutoPlayNodeController controller;

        public AutoPlayNodeReference(Orchestrator orchestrator, EventBus bus, AutoPlayNodeController controller) {
            this.bus = new ResettableEventBus(bus);
            this.controller = controller;
        }

        public void start() {
            controller.start(this.bus);
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
