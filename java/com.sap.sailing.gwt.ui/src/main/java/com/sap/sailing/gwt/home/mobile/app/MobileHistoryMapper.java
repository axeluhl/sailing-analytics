package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.home.client.place.event.legacy.EventPlace;
import com.sap.sailing.gwt.home.client.place.events.EventsPlace;
import com.sap.sailing.gwt.home.client.place.start.StartPlace;

@WithTokenizers({ //
EventPlace.Tokenizer.class,//
        EventsPlace.Tokenizer.class,//
        StartPlace.Tokenizer.class
})
public interface MobileHistoryMapper extends PlaceHistoryMapper {
}
