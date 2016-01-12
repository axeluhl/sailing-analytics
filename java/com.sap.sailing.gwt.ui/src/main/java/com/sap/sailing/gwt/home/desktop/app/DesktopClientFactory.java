package com.sap.sailing.gwt.home.desktop.app;

import com.sap.sailing.gwt.home.client.place.event.legacy.SeriesClientFactory;
import com.sap.sailing.gwt.home.desktop.places.aboutus.AboutUsClientFactory;
import com.sap.sailing.gwt.home.desktop.places.contact.ContactClientFactory;
import com.sap.sailing.gwt.home.desktop.places.event.EventClientFactory;
import com.sap.sailing.gwt.home.desktop.places.events.EventsClientFactory;
import com.sap.sailing.gwt.home.desktop.places.solutions.SolutionsClientFactory;
import com.sap.sailing.gwt.home.desktop.places.sponsoring.SponsoringClientFactory;
import com.sap.sailing.gwt.home.desktop.places.start.StartClientFactory;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileClientFactory;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewClientFactory;
import com.sap.sailing.gwt.home.shared.places.error.ErrorClientFactory;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultClientFactory;
import com.sap.sailing.gwt.home.shared.places.user.passwordreset.PasswordResetClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementClientFactory;

public interface DesktopClientFactory extends AboutUsClientFactory, ContactClientFactory, EventClientFactory,
        EventsClientFactory, StartClientFactory, SponsoringClientFactory, SolutionsClientFactory,
        SearchResultClientFactory, WhatsNewClientFactory, SeriesClientFactory, UserProfileClientFactory,
        ErrorClientFactory, UserManagementClientFactory, PasswordResetClientFactory {

    DesktopPlacesNavigator getHomePlacesNavigator();

    DesktopResettableNavigationPathDisplay getNavigationPathDisplay();
}
