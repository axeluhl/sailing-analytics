package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.autoplay.client.places.autoplaystart.AutoPlayStartPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.boats.RaceEndWithCompetitorsBoatsPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceloop.flags.RaceEndWithCompetitorFlagsPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext.IdleUpNextPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboard.LeaderboardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.leaderboardsixty.IdleSixtyInchLeaderboardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.video.VideoPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.raceboard.LiveRaceWithRaceboardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.liveraceloop.racemapwithleaderboard.LiveRaceWithRacemapAndLeaderBoardPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preevent.IdlePreEventPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors.PreRaceCompetitorsFlagsPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors.PreRaceCompetitorsImagePlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreRaceLeaderboardWithCompetitorPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreRaceLeaderboardWithFlagPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.PreRaceRacemapPlace;

@WithTokenizers({ //
        AutoPlayStartPlace.Tokenizer.class, 
        IdleSixtyInchLeaderboardPlace.Tokenizer.class, 
        VideoPlace.Tokenizer.class, 
        IdleUpNextPlace.Tokenizer.class, 
        RaceEndWithCompetitorsBoatsPlace.Tokenizer.class,
        RaceEndWithCompetitorFlagsPlace.Tokenizer.class,
        LeaderboardPlace.Tokenizer.class,
        LiveRaceWithRaceboardPlace.Tokenizer.class,
        LiveRaceWithRacemapAndLeaderBoardPlace.Tokenizer.class,
        IdlePreEventPlace.Tokenizer.class,
        PreRaceCompetitorsFlagsPlace.Tokenizer.class,
        PreRaceCompetitorsImagePlace.Tokenizer.class,
        PreRaceLeaderboardWithCompetitorPlace.Tokenizer.class,
        PreRaceLeaderboardWithFlagPlace.Tokenizer.class,
        PreRaceRacemapPlace.Tokenizer.class,
        })
public interface AutoPlayHistoryMapper extends PlaceHistoryMapper {
}
