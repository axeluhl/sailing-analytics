package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide1.Slide1Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.slides.slide2.Slide2Place;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.StartPlaceSixtyInch;

@WithTokenizers({ StartPlaceSixtyInch.Tokenizer.class, Slide1Place.Tokenizer.class, Slide2Place.Tokenizer.class })
public interface AutoPlayHistoryMapperSixtyInch extends PlaceHistoryMapper {
}
