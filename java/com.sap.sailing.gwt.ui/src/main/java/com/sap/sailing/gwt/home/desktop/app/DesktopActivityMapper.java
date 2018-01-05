package com.sap.sailing.gwt.home.desktop.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.desktop.places.aboutus.AboutUsActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.aboutus.AboutUsPlace;
import com.sap.sailing.gwt.home.desktop.places.contact.ContactActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.contact.ContactPlace;
import com.sap.sailing.gwt.home.desktop.places.error.ErrorActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.event.EventActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.events.EventsActivity;
import com.sap.sailing.gwt.home.desktop.places.morelogininformation.MoreLoginInformationActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.solutions.SolutionsActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.sponsoring.SponsoringActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.sponsoring.SponsoringPlace;
import com.sap.sailing.gwt.home.desktop.places.start.StartActivity;
import com.sap.sailing.gwt.home.desktop.places.user.profile.UserProfileActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewActivityProxy;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace;
import com.sap.sailing.gwt.home.shared.SwitchingEntryPoint;
import com.sap.sailing.gwt.home.shared.app.ApplicationPlaceUpdater;
import com.sap.sailing.gwt.home.shared.places.error.ErrorPlace;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.events.EventsPlace;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.imprint.ImprintActivityProxy;
import com.sap.sailing.gwt.home.shared.places.imprint.ImprintPlace;
import com.sap.sailing.gwt.home.shared.places.morelogininformation.MoreLoginInformationPlace;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultActivityProxy;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultPlace;
import com.sap.sailing.gwt.home.shared.places.solutions.SolutionsPlace;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationActivityProxy;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sailing.gwt.home.shared.places.user.passwordreset.PasswordResetActivityProxy;
import com.sap.sailing.gwt.home.shared.places.user.passwordreset.PasswordResetPlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sse.gwt.client.formfactor.DeviceDetector;

public class DesktopActivityMapper implements ActivityMapper {
    private final DesktopClientFactory clientFactory;
    private final ApplicationPlaceUpdater placeUpdater = new ApplicationPlaceUpdater();

    public DesktopActivityMapper(DesktopClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place rawPlace) {
        Place place = placeUpdater.getRealPlace(rawPlace);
        if (DeviceDetector.isMobile() //
                && !SwitchingEntryPoint.viewIsLockedToDesktop()
                && SwitchingEntryPoint.hasMobileVersion(place)) {
            SwitchingEntryPoint.reloadApp();
            return null;
        }
        if (place instanceof AboutUsPlace) {
            return new AboutUsActivityProxy((AboutUsPlace) place, clientFactory);
        } else if (place instanceof ErrorPlace) {
            return new ErrorActivityProxy((ErrorPlace) place, clientFactory);
        } else if (place instanceof ContactPlace) {
            return new ContactActivityProxy((ContactPlace) place, clientFactory);
        } else if (place instanceof AbstractEventPlace) {
            AbstractEventPlace eventPlace = (AbstractEventPlace) place;
            return new EventActivityProxy(eventPlace, clientFactory, clientFactory.getHomePlacesNavigator());
        } else if (place instanceof AbstractSeriesPlace) {
            return new com.sap.sailing.gwt.home.desktop.places.fakeseries.SeriesActivityProxy((AbstractSeriesPlace) place, clientFactory, clientFactory.getHomePlacesNavigator());
        } else if (place instanceof EventsPlace) {
            return new EventsActivity((EventsPlace) place, clientFactory, clientFactory.getHomePlacesNavigator());
        } else if (place instanceof AbstractUserProfilePlace) {
            return new UserProfileActivityProxy((AbstractUserProfilePlace) place, clientFactory, clientFactory.getHomePlacesNavigator());
        } else if (place instanceof StartPlace) {
            return new StartActivity((StartPlace) place, clientFactory);
        } else if (place instanceof SponsoringPlace) {
            return new SponsoringActivityProxy((SponsoringPlace) place, clientFactory);
        } else if (place instanceof SolutionsPlace) {
            return new SolutionsActivityProxy((SolutionsPlace) place, clientFactory);
        } else if (place instanceof WhatsNewPlace) {
            return new WhatsNewActivityProxy((WhatsNewPlace) place, clientFactory);
        } else if (place instanceof SearchResultPlace) {
            return new SearchResultActivityProxy((SearchResultPlace) place, clientFactory);
        } else if (place instanceof ConfirmationPlace) {
            return new ConfirmationActivityProxy((ConfirmationPlace) place, clientFactory);
        } else if (place instanceof PasswordResetPlace) {
            return new PasswordResetActivityProxy((PasswordResetPlace) place, clientFactory);
        } else if (place instanceof ImprintPlace) {
            return new ImprintActivityProxy((ImprintPlace) place);
        } else if (place instanceof MoreLoginInformationPlace) {
            return new MoreLoginInformationActivityProxy((MoreLoginInformationPlace) place, clientFactory);
        } else {
            return null;
        }
    }
}
