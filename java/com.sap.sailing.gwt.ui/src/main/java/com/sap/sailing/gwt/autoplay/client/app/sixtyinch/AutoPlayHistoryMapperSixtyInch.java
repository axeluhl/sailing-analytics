package com.sap.sailing.gwt.autoplay.client.app.sixtyinch;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceLoop.boats.RaceEndWithCompetitorsTop3Place;
import com.sap.sailing.gwt.autoplay.client.places.screens.afterliveraceLoop.boats.RaceEndWithFlagesTop3Place;
import com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext.IdleUpNextPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.liferaceloop.racemap.LifeRaceWithRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors.PreRaceCompetitorsFlagsPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.competitors.PreRaceCompetitorsImagePlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreRaceLeaderBoardWithCompetitorPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.leaderboard.PreRaceLeaderBoardWithFlagPlace;
import com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap.PreRaceRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.places.startsixtyinch.SlideInitPlace;
import com.sap.sailing.gwt.autoplay.client.places.startsixtyinch.StartPlaceSixtyInch;

@WithTokenizers({ SlideInitPlace.Tokenizer.class, StartPlaceSixtyInch.Tokenizer.class,
        PreRaceCompetitorsFlagsPlace.Tokenizer.class, PreRaceCompetitorsImagePlace.Tokenizer.class,
        IdleUpNextPlace.Tokenizer.class, PreRaceLeaderBoardWithCompetitorPlace.Tokenizer.class,
        PreRaceLeaderBoardWithFlagPlace.Tokenizer.class, PreRaceRacemapPlace.Tokenizer.class,
        LifeRaceWithRacemapPlace.Tokenizer.class, RaceEndWithCompetitorsTop3Place.Tokenizer.class,
        RaceEndWithFlagesTop3Place.Tokenizer.class })
public interface AutoPlayHistoryMapperSixtyInch extends PlaceHistoryMapper {
}
