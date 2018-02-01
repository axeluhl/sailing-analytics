package com.sap.sailing.gwt.autoplay.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.autoplay.client.places.autoplaystart.AutoPlayStartPlace;

@WithTokenizers({ //
        AutoPlayStartPlace.Tokenizer.class
})
public interface AutoPlayHistoryMapper extends PlaceHistoryMapper {
}
