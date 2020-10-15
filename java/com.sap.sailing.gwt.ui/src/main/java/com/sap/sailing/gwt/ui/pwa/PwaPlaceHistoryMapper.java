package com.sap.sailing.gwt.ui.pwa;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.ui.pwa.mobile.places.events.MobileEventsPlace;

@WithTokenizers({MobileEventsPlace.Tokenizer.class})
public interface PwaPlaceHistoryMapper extends PlaceHistoryMapper {

}