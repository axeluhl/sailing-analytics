package com.sap.sailing.gwt.autoplay.client.place.sixtyinch;

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
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.autoplay.client.events.MiniLeaderboardUpdatedEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.SlideConfig;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.SlideEventTriggeredTransitionConfig;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.SlideTimedTransitionConfig;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.Slide7Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;

public class SixtyInchOrchestrator {

    private AutoPlayClientFactorySixtyInch cf;

    private List<AutoPlayDataLoader<AutoPlayClientFactorySixtyInch>> loaders = new ArrayList<>();

    private SlideConfig currentSlideConfigurationRoot;
    private SlideConfig currentSlideConfiguration;

    public SixtyInchOrchestrator(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;
        loaders.add(new EventDTODataLoader());
        loaders.add(new MiniLeaderboardLoader());
        loaders.add(new RaceTimeInfoProviderLoader());
        
        SlideConfig slideInit = new SlideEventTriggeredTransitionConfig(this, new SlideInitPlace(),
                new Type<?>[] { EventChanged.TYPE, MiniLeaderboardUpdatedEvent.TYPE });
        // SlideConfig slide0 = new SlideTimedTransitionConfig(this, new Slide0Place(), 10000);
        SlideConfig slide1 = new SlideTimedTransitionConfig(this, new Slide1Place(), 10000);
        // SlideConfig slide2 = new SlideTimedTransitionConfig(this, new Slide2Place(), 10000);
        // SlideConfig slide3 = new SlideTimedTransitionConfig(this, new Slide3Place(), 10000);
        // SlideConfig slide4 = new SlideTimedTransitionConfig(this, new Slide4Place(), 10000);
        // SlideConfig slide5 = new SlideTimedTransitionConfig(this, new Slide5Place(), 10000);
        // SlideConfig slide6 = new SlideTimedTransitionConfig(this, new Slide6Place(), 10000);
        SlideConfig slide7 = new SlideTimedTransitionConfig(this, new Slide7Place(), 15000);
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
        currentSlideConfigurationRoot.start();
    }

    private void process(GwtEvent<?> event) {
        if (currentSlideConfiguration != null) {
            currentSlideConfiguration.process(event);
        }
    }

    public void doStart(SlideConfig slideConfig) {
        currentSlideConfiguration = slideConfig;
        GWT.log("transition to: " + slideConfig.getPlaceToGo().getClass().getName());
        cf.getPlaceController().goTo(slideConfig.getPlaceToGo());
    }
}
