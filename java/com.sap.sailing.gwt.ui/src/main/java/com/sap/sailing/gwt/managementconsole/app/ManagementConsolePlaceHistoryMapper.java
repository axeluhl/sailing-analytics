package com.sap.sailing.gwt.managementconsole.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.managementconsole.places.event.media.EventMediaPlace;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewPlace;
import com.sap.sailing.gwt.managementconsole.places.showcase.ShowcasePlace;

@WithTokenizers({ ShowcasePlace.Tokenizer.class, EventOverviewPlace.Tokenizer.class, EventMediaPlace.Tokenizer.class })
public interface ManagementConsolePlaceHistoryMapper extends PlaceHistoryMapper {
}
