package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.autoplay.client.places.config.ConfigPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats.RaceEndWithCompetitorsTop3Place;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats.RaceEndWithFlagesTop3Place;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext.IdleUpNextPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard.LeaderboardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.raceboard.LiveRaceWithRaceboardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard.LiveRaceWithRacemapAndLeaderBoardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors.PreRaceCompetitorsFlagsPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors.PreRaceCompetitorsImagePlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreRaceLeaderBoardWithCompetitorPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreRaceLeaderBoardWithFlagPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.PreRaceRacemapPlace;

@WithTokenizers({ //
        ConfigPlace.Tokenizer.class, //
        LiveRaceWithRaceboardPlace.Tokenizer.class, //
        LeaderboardPlace.Tokenizer.class, //
        PreRaceCompetitorsFlagsPlace.Tokenizer.class, //
        PreRaceCompetitorsImagePlace.Tokenizer.class, //
        IdleUpNextPlace.Tokenizer.class, //
        PreRaceLeaderBoardWithCompetitorPlace.Tokenizer.class, //
        PreRaceLeaderBoardWithFlagPlace.Tokenizer.class, //
        PreRaceRacemapPlace.Tokenizer.class, //
        LiveRaceWithRacemapAndLeaderBoardPlace.Tokenizer.class, //
        RaceEndWithCompetitorsTop3Place.Tokenizer.class, //
        RaceEndWithFlagesTop3Place.Tokenizer.class //
})
public interface AutoPlayHistoryMapper extends PlaceHistoryMapper {
}
