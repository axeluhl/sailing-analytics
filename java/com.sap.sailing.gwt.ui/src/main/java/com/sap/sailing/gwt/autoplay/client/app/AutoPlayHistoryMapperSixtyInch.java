package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.PreRaceLeaderBoardWithFlagsPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.IdleUpNextPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4.Slide4Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide5.Slide5Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6.PreRaceRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.LifeRaceWithRacemapPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.RaceEndWithBoatsPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide9.Slide9Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.StartPlaceSixtyInch;

@WithTokenizers({ SlideInitPlace.Tokenizer.class, StartPlaceSixtyInch.Tokenizer.class, PreRaceLeaderBoardWithFlagsPlace.Tokenizer.class,
        PreRaceLeaderBoardWithFlagsPlace.Tokenizer.class, IdleUpNextPlace.Tokenizer.class, Slide4Place.Tokenizer.class,
        Slide5Place.Tokenizer.class, PreRaceRacemapPlace.Tokenizer.class,
        LifeRaceWithRacemapPlace.Tokenizer.class, RaceEndWithBoatsPlace.Tokenizer.class, Slide9Place.Tokenizer.class })
public interface AutoPlayHistoryMapperSixtyInch extends PlaceHistoryMapper {
}
