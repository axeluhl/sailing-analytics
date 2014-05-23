package com.sap.sailing.gwt.home.client.app;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.sap.sailing.gwt.home.client.app.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.client.app.contact.ContactPlace;
import com.sap.sailing.gwt.home.client.app.event.EventPlace;
import com.sap.sailing.gwt.home.client.app.events.EventsPlace;
import com.sap.sailing.gwt.home.client.app.start.StartPlace;

@WithTokenizers({ AboutUsPlace.Tokenizer.class, ContactPlace.Tokenizer.class, EventPlace.Tokenizer.class, EventsPlace.Tokenizer.class, StartPlace.Tokenizer.class })
public interface ApplicationHistoryMapper extends PlaceHistoryMapper {
}
