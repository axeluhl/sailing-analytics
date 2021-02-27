package com.sap.sailing.gwt.managementconsole.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.managementconsole.places.event.create.CreateEventPlace;
import com.sap.sailing.gwt.managementconsole.places.event.media.EventMediaPlace;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewPlace;
import com.sap.sailing.gwt.managementconsole.places.regatta.create.AddRegattaPlace;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewPlace;
import com.sap.sailing.gwt.managementconsole.places.showcase.ShowcasePlace;

@WithTokenizers({ ShowcasePlace.Tokenizer.class, EventOverviewPlace.Tokenizer.class, EventMediaPlace.Tokenizer.class, 
    RegattaOverviewPlace.Tokenizer.class, CreateEventPlace.Tokenizer.class, AddRegattaPlace.Tokenizer.class})
public interface ManagementConsolePlaceHistoryMapper extends PlaceHistoryMapper {
}
