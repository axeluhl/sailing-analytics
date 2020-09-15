package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsolePlace;

@WithTokenizers({AdminConsolePlace.Tokenizer.class})
public interface AdminConsolePlaceHistoryMapper extends PlaceHistoryMapper {

}
