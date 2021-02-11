package com.sap.sailing.gwt.managementconsole.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewPlace;
import com.sap.sailing.gwt.managementconsole.places.showcase.ShowcasePlace;

@WithTokenizers({ ShowcasePlace.Tokenizer.class, EventOverviewPlace.Tokenizer.class })
public interface ManagementConsolePlaceHistoryMapper extends PlaceHistoryMapper {
}
