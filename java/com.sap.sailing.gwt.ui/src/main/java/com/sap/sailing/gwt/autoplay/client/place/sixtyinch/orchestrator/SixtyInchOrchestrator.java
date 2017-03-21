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
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.TimedTransitionSimpleNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.Slide1Node;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.nodes.StartupNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.Slide7Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;

public class SixtyInchOrchestrator implements Orchestrator {

    private AutoPlayClientFactorySixtyInch cf;

    private AutoPlayNodeReference currentNodeRef;
    private AutoPlayNode rootNodeForStartup;

    public SixtyInchOrchestrator(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;
        
        StartupNode slideInit = new StartupNode(cf);
        // SlideConfig slide0 = new SlideTimedTransitionConfig(this, new Slide0Place(), 10000);
        Slide1Node node1 = new Slide1Node(cf);
        // SlideConfig slide2 = new SlideTimedTransitionConfig(this, new Slide2Place(), 10000);
        // SlideConfig slide3 = new SlideTimedTransitionConfig(this, new Slide3Place(), 10000);
        // SlideConfig slide4 = new SlideTimedTransitionConfig(this, new Slide4Place(), 10000);
        // SlideConfig slide5 = new SlideTimedTransitionConfig(this, new Slide5Place(), 10000);
        // SlideConfig slide6 = new SlideTimedTransitionConfig(this, new Slide6Place(), 10000);
        TimedTransitionSimpleNode node7 = new TimedTransitionSimpleNode("node7", new Slide7Place(), 15000);
        // SlideConfig slide8 = new SlideTimedTransitionConfig(this, new Slide8Place(), 10000);
        // SlideConfig slide9 = new SlideTimedTransitionConfig(this, new Slide9Place(), 10000);
        // slideInit.setNextSlide(slide0);
        // slide0.setNextSlide(slide1);
        // slide1.setNextSlide(slide2);
        // slide2.setNextSlide(slide3);
        // slide3.setNextSlide(slide4);
        // slide4.setNextSlide(slide5);
        // slide5.setNextSlide(slide6);
        // slide6.setNextSlide(slide7);
        // slide7.setNextSlide(slide8);
        // slide8.setNextSlide(slide9);
        // slide9.setNextSlide(slide0);

        slideInit.setNextNode(node1);
        node1.setNextNode(node7);
        node7.setNextNode(node1);

        rootNodeForStartup = slideInit;
    }


    /* (non-Javadoc)
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
        transitionToNode(null, rootNodeForStartup);
    }

    private void processFailure(FailureEvent event) {
        GWT.log("Captured failure event: " + event);
        if (currentNodeRef != null) {
            currentNodeRef.stop();
        }
        cf.getPlaceController().goTo(new SlideInitPlace(event, currentNodeRef.node));
    }


    /* (non-Javadoc)
     * @see com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.Orchestrator#didMoveToSlide(com.sap.sailing.gwt.autoplay.client.orchestrator.AutoPlayNode)
     */
    @Override
    public void transitionToNode(AutoPlayNode source, AutoPlayNode nextNode) {
        if (source == null) {
            GWT.log("transition started by: unknown");
        } else {
            GWT.log("transition started by: " + source);
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
