package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.PreRaceLeaderBoardWithCompetitorPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.PreRaceLeaderBoardWithFlagPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.IdleUpNextPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4.PreRaceCompetitorsFlagsPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4.PreRaceCompetitorsImagePlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6.PreRaceRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.LifeRaceWithRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.RaceEndWithCompetitorsTop3Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.RaceEndWithFlagesTop3Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.StartPlaceSixtyInch;

@WithTokenizers({ SlideInitPlace.Tokenizer.class, StartPlaceSixtyInch.Tokenizer.class,
        PreRaceCompetitorsFlagsPlace.Tokenizer.class, PreRaceCompetitorsImagePlace.Tokenizer.class,
        IdleUpNextPlace.Tokenizer.class, PreRaceLeaderBoardWithCompetitorPlace.Tokenizer.class,
        PreRaceLeaderBoardWithFlagPlace.Tokenizer.class, PreRaceRacemapPlace.Tokenizer.class,
        LifeRaceWithRacemapPlace.Tokenizer.class, RaceEndWithCompetitorsTop3Place.Tokenizer.class,
        RaceEndWithFlagesTop3Place.Tokenizer.class })
public interface AutoPlayHistoryMapperSixtyInch extends PlaceHistoryMapper {
}
