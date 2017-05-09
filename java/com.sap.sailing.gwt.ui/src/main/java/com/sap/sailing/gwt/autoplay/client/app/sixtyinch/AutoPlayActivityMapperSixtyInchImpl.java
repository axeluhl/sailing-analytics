package com.sap.sailing.gwt.autoplay.client.app.sixtyinch;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats.RaceEndWithBoatsPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats.RaceEndWithBoatsViewImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats.RaceEndWithCompetitorsTop3Place;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats.RaceEndWithFlagesTop3Place;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext.IdleNextUpViewImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext.IdleUpNextPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext.IdleUpNextPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard.LiveRaceWithRacemapAndLeaderBoardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard.LiveRaceWithRacemapAndLeaderBoardPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard.LiveRaceWithRacemapAndLeaderBoardViewImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors.PreRaceCompetitorsFlagsPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors.PreRaceCompetitorsImagePlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors.PreRaceCompetitorsPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors.PreRaceCompetitorsViewImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreLeaderBoardWithImagePresenterImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreLeaderBoardWithImageViewImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreRaceLeaderBoardWithCompetitorPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreRaceLeaderBoardWithFlagPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.PreRaceRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.PreRaceRacemapPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.PreRaceRacemapViewImpl;
import com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.config.SixtyInchConfigPlace;
import com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.config.SixtyInchConfigPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.config.SixtyInchConfigViewImpl;
import com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.initial.SixtyInchInitialImpl;
import com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.initial.SixtyInchInitialPlace;
import com.sap.sailing.gwt.autoplay.client.places.startup.sixtyinch.initial.SixtyInchInitialPresenterImpl;
import com.sap.sailing.gwt.autoplay.client.utils.CompetitorImageProvider;
import com.sap.sailing.gwt.autoplay.client.utils.FlagImageProvider;

public class AutoPlayActivityMapperSixtyInchImpl implements ActivityMapper {
    private final AutoPlayClientFactory clientFactory;

    public AutoPlayActivityMapperSixtyInchImpl(AutoPlayClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof SixtyInchConfigPlace) {
            return new SixtyInchConfigPresenterImpl((SixtyInchConfigPlace) place, clientFactory,
                    new SixtyInchConfigViewImpl(clientFactory));
        }
        if (place instanceof SixtyInchInitialPlace) {
            return new SixtyInchInitialPresenterImpl((SixtyInchInitialPlace) place, clientFactory, new SixtyInchInitialImpl());
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
        if (place instanceof LiveRaceWithRacemapAndLeaderBoardPlace) {
            return new LiveRaceWithRacemapAndLeaderBoardPresenterImpl((LiveRaceWithRacemapAndLeaderBoardPlace) place, clientFactory, new LiveRaceWithRacemapAndLeaderBoardViewImpl());
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
        return new SixtyInchInitialPresenterImpl(new SixtyInchInitialPlace(), clientFactory, new SixtyInchInitialImpl());
    }
}
