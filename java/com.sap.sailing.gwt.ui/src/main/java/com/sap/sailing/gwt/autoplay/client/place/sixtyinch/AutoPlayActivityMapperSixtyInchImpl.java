package com.sap.sailing.gwt.autoplay.client.place.sixtyinch;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactorySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.CompetitorImageProvider;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base.FlagImageProvider;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.afterliveraceLoop.boats.RaceEndWithBoatsPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.afterliveraceLoop.boats.RaceEndWithBoatsViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.afterliveraceLoop.boats.RaceEndWithCompetitorsTop3Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.afterliveraceLoop.boats.RaceEndWithFlagesTop3Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.idleloop.idleupnext.IdleNextUpViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.idleloop.idleupnext.IdleUpNextPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.idleloop.idleupnext.IdleUpNextPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.liferaceloop.racemap.LifeRaceWithRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.liferaceloop.racemap.LifeRaceWithRacemapPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.liferaceloop.racemap.LifeRaceWithRacemapViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.competitors.PreRaceCompetitorsFlagsPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.competitors.PreRaceCompetitorsImagePlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.competitors.PreRaceCompetitorsPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.competitors.PreRaceCompetitorsViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.leaderboard.PreLeaderBoardWithImagePresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.leaderboard.PreLeaderBoardWithImageViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.leaderboard.PreRaceLeaderBoardWithCompetitorPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.leaderboard.PreRaceLeaderBoardWithFlagPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.racemap.PreRaceRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.racemap.PreRaceRacemapPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.racemap.PreRaceRacemapViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.start.SlideInitPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.start.SlideInitPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.start.SlideInitViewImpl;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.start.StartActivitySixtyInch;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.start.StartPlaceSixtyInch;

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
