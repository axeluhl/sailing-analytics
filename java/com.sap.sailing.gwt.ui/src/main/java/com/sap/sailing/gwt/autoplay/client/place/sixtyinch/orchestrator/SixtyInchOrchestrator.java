package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.dataloader.AutoPlayDataLoader;
import com.sap.sailing.gwt.autoplay.client.dataloader.EventDTODataLoader;
import com.sap.sailing.gwt.autoplay.client.dataloader.MiniLeaderboardLoader;
import com.sap.sailing.gwt.autoplay.client.dataloader.RaceTimeInfoProviderLoader;
import com.sap.sailing.gwt.autoplay.client.events.AutoplayFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.DataLoadFailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.autoplay.client.events.FailureEvent;
import com.sap.sailing.gwt.autoplay.client.events.MiniLeaderboardUpdatedEvent;
import com.sap.sailing.gwt.autoplay.client.orchestrator.Orchestrator;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.AutoPlayNode;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.TimedTransitionSimpleNode;
import com.sap.sailing.gwt.autoplay.client.orchestrator.nodes.TriggerUponEventsSimpleNode;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.Slide7Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;

public class SixtyInchOrchestrator implements Orchestrator {

    private AutoPlayClientFactorySixtyInch cf;

    private List<AutoPlayDataLoader<AutoPlayClientFactorySixtyInch>> loaders = new ArrayList<>();

    private AutoPlayNode currentSlideConfigurationRoot;
    private AutoPlayNode currentSlideConfiguration;

    public SixtyInchOrchestrator(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;
        loaders.add(new EventDTODataLoader());
        loaders.add(new MiniLeaderboardLoader());
        loaders.add(new RaceTimeInfoProviderLoader());
        
        AutoPlayNode slideInit = new TriggerUponEventsSimpleNode(this, new SlideInitPlace(),
                new Type<?>[] { EventChanged.TYPE, MiniLeaderboardUpdatedEvent.TYPE
                // ,RaceTimeInfoProviderUpdatedEvent.TYPE
                });
        // SlideConfig slide0 = new SlideTimedTransitionConfig(this, new Slide0Place(), 10000);
        AutoPlayNode slide1 = new TimedTransitionSimpleNode(this, new Slide1Place(), 10000);
        // SlideConfig slide2 = new SlideTimedTransitionConfig(this, new Slide2Place(), 10000);
        // SlideConfig slide3 = new SlideTimedTransitionConfig(this, new Slide3Place(), 10000);
        // SlideConfig slide4 = new SlideTimedTransitionConfig(this, new Slide4Place(), 10000);
        // SlideConfig slide5 = new SlideTimedTransitionConfig(this, new Slide5Place(), 10000);
        // SlideConfig slide6 = new SlideTimedTransitionConfig(this, new Slide6Place(), 10000);
        AutoPlayNode slide7 = new TimedTransitionSimpleNode(this, new Slide7Place(), 15000);
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

        slideInit.setNextSlide(slide1);
        slide1.setNextSlide(slide7);
        slide7.setNextSlide(slide1);

        currentSlideConfigurationRoot = slideInit;
    }


    /* (non-Javadoc)
     * @see com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.Orchestrator#start()
     */
    @Override
    public void start() {
        for (AutoPlayDataLoader<AutoPlayClientFactorySixtyInch> loader : loaders) {
            loader.startLoading(cf.getEventBus(), cf);
        }
        cf.getEventBus().addHandler(EventChanged.TYPE, new EventChanged.Handler() {
            @Override
            public void onEventChanged(EventChanged e) {
                process(e);
            }
        });
        cf.getEventBus().addHandler(MiniLeaderboardUpdatedEvent.TYPE, new MiniLeaderboardUpdatedEvent.Handler() {
            @Override
            public void handleNoOpEvent(MiniLeaderboardUpdatedEvent e) {
                process(e);
            }
        });
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
        currentSlideConfigurationRoot.start();
    }

    private void processFailure(FailureEvent event) {
        GWT.log("Captured failure event: " + event);
        if (currentSlideConfiguration != null) {
            currentSlideConfiguration.stop();
        }

        cf.getPlaceController().goTo(new SlideInitPlace(event, currentSlideConfiguration));
    }

    private void process(GwtEvent<?> event) {
        if (currentSlideConfiguration != null) {
            currentSlideConfiguration.process(event);
        }
    }

    /* (non-Javadoc)
     * @see com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.Orchestrator#didMoveToSlide(com.sap.sailing.gwt.autoplay.client.orchestrator.AutoPlayNode)
     */
    @Override
    public void didMoveToSlide(AutoPlayNode slideConfig) {
        if (currentSlideConfiguration != null) {
            try {
                currentSlideConfiguration.stop();
            } catch (Exception e) {
                GWT.log("Failed to stop current slide", e);
            }
        }
        currentSlideConfiguration = slideConfig;
        GWT.log("transition to: " + slideConfig.getPlaceToGo().getClass().getName());
        cf.getPlaceController().goTo(slideConfig.getPlaceToGo());
    }

}
