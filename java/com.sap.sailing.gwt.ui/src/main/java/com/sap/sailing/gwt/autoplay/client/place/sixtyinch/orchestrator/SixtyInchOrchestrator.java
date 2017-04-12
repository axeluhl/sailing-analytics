package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.ResettableEventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.FallbackToIdleLoopEvent;
import com.sap.sailing.gwt.autoplay.client.events.LiferaceDetectedEvent;
import com.sap.sailing.gwt.autoplay.client.events.UpcomingLiferaceDetectedEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.IdleUpNextNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.LifeRaceWithRacemapNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.PreRaceWithRacemapNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.RaceEndWithBoatsNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.StartupNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;

public class SixtyInchOrchestrator implements Orchestrator {

    private AutoPlayClientFactorySixtyInch cf;

    private AutoPlayNodeReference currentNodeRef;

    private final AutoPlayNode preLifeRaceLoopStart;
    private final AutoPlayNode afterLifeRaceLoopStart;
    private final AutoPlayNode lifeRaceLoopStart;
    private final AutoPlayNode idleLoopStart;

    private final LifeRacesMonitor racesMonitor;

    public SixtyInchOrchestrator(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;

        StartupNode slideInit = new StartupNode(cf);

        IdleUpNextNode idleUpNext = new IdleUpNextNode(cf);

        PreRaceWithRacemapNode preRaceRacemapNode = new PreRaceWithRacemapNode(cf);
        LifeRaceWithRacemapNode lifeRaceWithRacemap = new LifeRaceWithRacemapNode(cf);
        RaceEndWithBoatsNode endRaceSlideWithBoats = new RaceEndWithBoatsNode(cf);
        endRaceSlideWithBoats.setNextNode(idleUpNext);

        slideInit.setNextNode(idleUpNext);

        idleLoopStart = slideInit;
        preLifeRaceLoopStart = preRaceRacemapNode;
        lifeRaceLoopStart = lifeRaceWithRacemap;
        afterLifeRaceLoopStart = endRaceSlideWithBoats;
        racesMonitor = new LifeRacesMonitor(cf);
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
        cf.getEventBus().addHandler(UpcomingLiferaceDetectedEvent.TYPE, new UpcomingLiferaceDetectedEvent.Handler() {
            @Override
            public void onLiferaceDetected(UpcomingLiferaceDetectedEvent e) {
                cf.getSlideCtx().setCurrenLifeRace(e.getLifeRace());
                transitionToNode(preLifeRaceLoopStart);
            }
        });
        cf.getEventBus().addHandler(LiferaceDetectedEvent.TYPE, new LiferaceDetectedEvent.Handler() {
            @Override
            public void onLiferaceDetected(LiferaceDetectedEvent e) {
                cf.getSlideCtx().setCurrenLifeRace(e.getLifeRace());
                transitionToNode(lifeRaceLoopStart);
            }
        });
        cf.getEventBus().addHandler(FallbackToIdleLoopEvent.TYPE, new FallbackToIdleLoopEvent.Handler() {
            @Override
            public void onFallbackToIdle(FallbackToIdleLoopEvent e) {
                if (e.isComingFromLifeRace()) {
                    transitionToNode(afterLifeRaceLoopStart);
                } else {
                    transitionToNode(idleLoopStart);
                }
            }
        });

        transitionToNode(idleLoopStart);
    }

    private void processFailure(FailureEvent event) {
        GWT.log("Captured failure event: " + event);
        if (event.getCaught() != null) {
            event.getCaught().printStackTrace();
        }
        if (currentNodeRef != null) {
            currentNodeRef.stop();
        }
        cf.getPlaceController().goTo(new SlideInitPlace(event, currentNodeRef.node));
    }


    @Override
    public void transitionToNode(AutoPlayNode nextNode) {
        if(nextNode == null){
            GWT.log("Found no successor Node, staying at current node, validate Orchestrator");
            return;
        }
        if (currentNodeRef != null) {
            currentNodeRef.stop();
        }
        currentNodeRef = new AutoPlayNodeReference(this, cf.getEventBus(), nextNode);
        GWT.log("start orchestrator transition to " + nextNode.toString());
        currentNodeRef.start();
    }

    private static class AutoPlayNodeReference {
        private ResettableEventBus bus;
        private AutoPlayNode node;
        private Orchestrator orchestrator;

        public AutoPlayNodeReference(Orchestrator orchestrator, EventBus bus, AutoPlayNode node) {
            this.orchestrator = orchestrator;
            this.bus = new ResettableEventBus(bus);
            this.node = node;
        }

        public void start() {
            node.start(this.bus, orchestrator);
        }

        public void stop() {
            GWT.log("stop node " + node.toString());
            try {
                node.stop();
            } catch (Exception e) {
                GWT.log("Failed to stop current node", e);
            }
            bus.removeHandlers();
        }
    }

}
