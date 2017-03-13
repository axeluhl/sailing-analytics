package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide0.Slide0Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide3.Slide3Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide4.Slide4Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide5.Slide5Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide6.Slide6Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide7.Slide7Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide8.Slide8Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide9.Slide9Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slideinit.SlideInitPlace;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.StartPlaceSixtyInch;

@WithTokenizers({ SlideInitPlace.Tokenizer.class, StartPlaceSixtyInch.Tokenizer.class, Slide0Place.Tokenizer.class,
        Slide1Place.Tokenizer.class, Slide2Place.Tokenizer.class, Slide3Place.Tokenizer.class,
        Slide4Place.Tokenizer.class, Slide5Place.Tokenizer.class, Slide6Place.Tokenizer.class,
        Slide7Place.Tokenizer.class, Slide8Place.Tokenizer.class, Slide9Place.Tokenizer.class })
public interface AutoPlayHistoryMapperSixtyInch extends PlaceHistoryMapper {
}
