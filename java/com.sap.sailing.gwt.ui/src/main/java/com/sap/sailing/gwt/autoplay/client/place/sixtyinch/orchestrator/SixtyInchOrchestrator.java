package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.ResettableEventBus;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.FallbackToIdleNodePathEvent;
import com.sap.sailing.gwt.autoplay.client.events.UpcomingLiferaceDetectedEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.TimedTransitionSimpleNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.IdleUpNextNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.LifeRaceWithRacemapNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.PreRaceWithRacemapNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.RaceEndWithBoatsNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.StartupNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.IdleUpNextPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;

public class SixtyInchOrchestrator implements Orchestrator {

    private AutoPlayClientFactorySixtyInch cf;

    private AutoPlayNodeReference currentNodeRef;

    private AutoPlayNode liveRaceInitialNode;
    private AutoPlayNode idleEventInitialNode;

    private UpcomingRacesMonitor racesMonitor;

    public SixtyInchOrchestrator(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;

        racesMonitor = new UpcomingRacesMonitor(cf);
        StartupNode slideInit = new StartupNode(cf);
        // SlideConfig slide0 = new SlideTimedTransitionConfig(this, new Slide0Place(), 10000);
        TimedTransitionSimpleNode node2 = new TimedTransitionSimpleNode("node2", new IdleUpNextPlace(), 200000);

        // SlideConfig slide3 = new SlideTimedTransitionConfig(this, new Slide3Place(), 10000);
        // SlideConfig slide4 = new SlideTimedTransitionConfig(this, new Slide4Place(), 10000);
        // SlideConfig slide5 = new SlideTimedTransitionConfig(this, new Slide5Place(), 10000);

        IdleUpNextNode idleUpNext = new IdleUpNextNode(cf);

        PreRaceWithRacemapNode preRaceRacemapNode = new PreRaceWithRacemapNode(cf);
        LifeRaceWithRacemapNode lifeRaceWithRacemap = new LifeRaceWithRacemapNode(cf);
        RaceEndWithBoatsNode endRaceSlideWithBoats = new RaceEndWithBoatsNode(cf);

        // SlideConfig slide9 = new SlideTimedTransitionConfig(this, new Slide9Place(), 10000);
        // slideInit.setNextSlide(slide0);
        // slide0.setNextSlide(slide1);
        // slide1.setNextSlide(slide2);
        // slide2.setNextSlide(slide3);
        // slide3.setNextSlide(slide4);
        // slide4.setNextSlide(slide5);
        // slide5.setNextSlide(slide6);
        // slide7.setNextSlide(slide8);
        // slide8.setNextSlide(slide9);
        // slide9.setNextSlide(slide0);

        slideInit.setNextNode(idleUpNext);
        // endRaceSlideWithBoats.setNextNode(preRaceRacemapNode);
        // preRaceRacemapNode.setNextNode(lifeRaceWithRacemap);
        // lifeRaceWithRacemap.setNextNode(endRaceSlideWithBoats);


        // node2.setNextNode(node7);
        // node7.setNextNode(node2);

        idleEventInitialNode = slideInit;
        liveRaceInitialNode = preRaceRacemapNode;
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
                racesMonitor.startMonitoring();
                cf.getSlideCtx().setCurrenLifeRace(e.getLifeRace());
                transitionToNode(liveRaceInitialNode);
            }
        });
        cf.getEventBus().addHandler(FallbackToIdleNodePathEvent.TYPE, new FallbackToIdleNodePathEvent.Handler() {
            @Override
            public void onFallbackToIdle(FallbackToIdleNodePathEvent e) {
                racesMonitor.startMonitoring();
                transitionToNode(e.getSource(), idleEventInitialNode);
            }
        });
        racesMonitor.startMonitoring();
        transitionToNode(null, idleEventInitialNode);
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


    public void transitionToNode(AutoPlayNode nextNode) {
        transitionToNode(null, nextNode);
    }

    @Override
    public void transitionToNode(AutoPlayNode source, AutoPlayNode nextNode) {
        if (source == null) {
            GWT.log("transition started by: unknown");
        } else {
            GWT.log("transition started by: " + source);
        }
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
