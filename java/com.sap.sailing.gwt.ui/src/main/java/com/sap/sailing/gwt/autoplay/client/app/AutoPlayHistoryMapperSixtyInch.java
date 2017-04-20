package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.afterliveraceLoop.boats.RaceEndWithCompetitorsTop3Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.afterliveraceLoop.boats.RaceEndWithFlagesTop3Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.idleloop.idleupnext.IdleUpNextPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.liferaceloop.racemap.LifeRaceWithRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.competitors.PreRaceCompetitorsFlagsPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.competitors.PreRaceCompetitorsImagePlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.leaderboard.PreRaceLeaderBoardWithCompetitorPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.leaderboard.PreRaceLeaderBoardWithFlagPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.preliveraceloop.racemap.PreRaceRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.start.SlideInitPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.places.start.StartPlaceSixtyInch;

@WithTokenizers({ SlideInitPlace.Tokenizer.class, StartPlaceSixtyInch.Tokenizer.class,
        PreRaceCompetitorsFlagsPlace.Tokenizer.class, PreRaceCompetitorsImagePlace.Tokenizer.class,
        IdleUpNextPlace.Tokenizer.class, PreRaceLeaderBoardWithCompetitorPlace.Tokenizer.class,
        PreRaceLeaderBoardWithFlagPlace.Tokenizer.class, PreRaceRacemapPlace.Tokenizer.class,
        LifeRaceWithRacemapPlace.Tokenizer.class, RaceEndWithCompetitorsTop3Place.Tokenizer.class,
        RaceEndWithFlagesTop3Place.Tokenizer.class })
public interface AutoPlayHistoryMapperSixtyInch extends PlaceHistoryMapper {
}
