package com.sap.sailing.gwt.home.client.app;

import com.sap.sailing.gwt.home.client.app.aboutus.AboutUsClientFactory;
import com.sap.sailing.gwt.home.client.app.contact.ContactClientFactory;
import com.sap.sailing.gwt.home.client.app.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.app.events.EventsClientFactory;
import com.sap.sailing.gwt.home.client.app.start.StartClientFactory;

public interface ApplicationClientFactory extends AboutUsClientFactory, ContactClientFactory, EventClientFactory, EventsClientFactory, StartClientFactory {
}
