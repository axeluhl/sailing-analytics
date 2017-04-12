package com.sap.sailing.gwt.autoplay.client.place.sixtyinch;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.CompetitorImageProvider;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.FlagImageProvider;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.PreLeaderBoardWithImagePresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.PreLeaderBoardWithImageViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.PreRaceLeaderBoardWithCompetitorPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.PreRaceLeaderBoardWithFlagPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.IdleNextUpViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.IdleUpNextPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.IdleUpNextPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4.PreRaceCompetitorsFlagsPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4.PreRaceCompetitorsImagePlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4.PreRaceCompetitorsPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4.PreRaceCompetitorsViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6.PreRaceRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6.PreRaceRacemapPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6.PreRaceRacemapViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.LifeRaceWithRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.LifeRaceWithRacemapPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.LifeRaceWithRacemapViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.RaceEndWithBoatsPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.RaceEndWithBoatsViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.RaceEndWithCompetitorsTop3Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.RaceEndWithFlagesTop3Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.StartActivitySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.StartPlaceSixtyInch;

public class AutoPlayActivityMapperSixtyInchImpl implements ActivityMapper {
    private final AutoPlayClientFactorySixtyInch clientFactory;

    public AutoPlayActivityMapperSixtyInchImpl(AutoPlayClientFactorySixtyInch clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof StartPlaceSixtyInch) {
            return new StartActivitySixtyInch((StartPlaceSixtyInch) place, clientFactory);
        }
        if (place instanceof SlideInitPlace) {
            return new SlideInitPresenterImpl((SlideInitPlace) place, clientFactory, new SlideInitViewImpl());
        }
        if (place instanceof PreRaceLeaderBoardWithCompetitorPlace) {
            return new PreLeaderBoardWithImagePresenterImpl((PreRaceLeaderBoardWithCompetitorPlace) place,
                    clientFactory, new PreLeaderBoardWithImageViewImpl(new CompetitorImageProvider()));
        }
        if (place instanceof PreRaceLeaderBoardWithFlagPlace) {
            return new PreLeaderBoardWithImagePresenterImpl((PreRaceLeaderBoardWithFlagPlace) place, clientFactory,
                    new PreLeaderBoardWithImageViewImpl(new FlagImageProvider()));
        }
        if (place instanceof IdleUpNextPlace) {
            return new IdleUpNextPresenterImpl((IdleUpNextPlace) place, clientFactory, new IdleNextUpViewImpl());
        }
        if (place instanceof PreRaceCompetitorsFlagsPlace) {
            return new PreRaceCompetitorsPresenterImpl((PreRaceCompetitorsFlagsPlace) place, clientFactory,
                    new PreRaceCompetitorsViewImpl(new FlagImageProvider()));
        }
        if (place instanceof PreRaceCompetitorsImagePlace) {
            return new PreRaceCompetitorsPresenterImpl((PreRaceCompetitorsImagePlace) place, clientFactory,
                    new PreRaceCompetitorsViewImpl(new CompetitorImageProvider()));
        }
        if (place instanceof PreRaceRacemapPlace) {
            return new PreRaceRacemapPresenterImpl((PreRaceRacemapPlace) place, clientFactory,
                    new PreRaceRacemapViewImpl());
        }
        if (place instanceof LifeRaceWithRacemapPlace) {
            return new LifeRaceWithRacemapPresenterImpl((LifeRaceWithRacemapPlace) place, clientFactory, new LifeRaceWithRacemapViewImpl());
        }
        if (place instanceof RaceEndWithCompetitorsTop3Place) {
            return new RaceEndWithBoatsPresenterImpl((RaceEndWithCompetitorsTop3Place) place, clientFactory,
                    new RaceEndWithBoatsViewImpl(new CompetitorImageProvider()));
        }
        if (place instanceof RaceEndWithFlagesTop3Place) {
            return new RaceEndWithBoatsPresenterImpl((RaceEndWithFlagesTop3Place) place, clientFactory,
                    new RaceEndWithBoatsViewImpl(new FlagImageProvider()));
        }

        GWT.log("unknown place! " + place);
        return new SlideInitPresenterImpl(new SlideInitPlace(), clientFactory, new SlideInitViewImpl());
    }
}
