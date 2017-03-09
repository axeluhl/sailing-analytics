package com.sap.sailing.gwt.autoplay.client.place.sixtyinch;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.dataloader.AutoPlayDataLoader;
import com.sap.sailing.gwt.autoplay.client.dataloader.EventDTODataLoader;
import com.sap.sailing.gwt.autoplay.client.events.EventChanged;
import com.sap.sailing.gwt.autoplay.client.events.MiniLeaderboardUpdatedEvent;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.SlideConfig;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.SlideEventTriggeredTransitionConfig;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.orchestrator.SlideTimedTransitionConfig;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;

public class SixtyInchOrchestrator {

    private AutoPlayClientFactorySixtyInch cf;

    private List<AutoPlayDataLoader<AutoPlayClientFactorySixtyInch>> loaders = new ArrayList<>();

    private SlideConfig currentSlideConfigurationRoot;
    private SlideConfig currentSlideConfiguration;

    public SixtyInchOrchestrator(AutoPlayClientFactorySixtyInch cf) {
        this.cf = cf;
        loaders.add(new EventDTODataLoader());
        
        SlideConfig slideInit = new SlideEventTriggeredTransitionConfig(this, new SlideInitPlace(),
                new Type<?>[] { EventChanged.TYPE });
        SlideConfig slide1 = new SlideTimedTransitionConfig(this, new Slide1Place(), 10000);
        SlideConfig slide2 = new SlideTimedTransitionConfig(this, new Slide2Place(), 10000);
        slideInit.setNextSlide(slide1);
        slide1.setNextSlide(slide2);
        slide2.setNextSlide(slide1);

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
