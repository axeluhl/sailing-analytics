package com.sap.sailing.gwt.home.client.app;

import com.sap.sailing.gwt.home.client.place.aboutus.AboutUsClientFactory;
import com.sap.sailing.gwt.home.client.place.contact.ContactClientFactory;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.events.EventsClientFactory;
import com.sap.sailing.gwt.home.client.place.leaderboard.LeaderboardClientFactory;
import com.sap.sailing.gwt.home.client.place.searchresult.SearchResultClientFactory;
import com.sap.sailing.gwt.home.client.place.solutions.SolutionsClientFactory;
import com.sap.sailing.gwt.home.client.place.sponsoring.SponsoringClientFactory;
import com.sap.sailing.gwt.home.client.place.start.StartClientFactory;

public interface ApplicationClientFactory extends AboutUsClientFactory, ContactClientFactory, EventClientFactory,
    EventsClientFactory, StartClientFactory, SponsoringClientFactory, SolutionsClientFactory, SearchResultClientFactory,
    LeaderboardClientFactory {
    HomePlacesNavigator getHomePlacesNavigator();
}
