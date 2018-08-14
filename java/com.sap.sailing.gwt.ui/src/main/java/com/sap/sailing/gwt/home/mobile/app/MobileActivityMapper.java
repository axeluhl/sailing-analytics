package com.sap.sailing.gwt.home.mobile.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.mobile.places.error.ErrorActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.event.EventActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.events.EventsActivity;
import com.sap.sailing.gwt.home.mobile.places.morelogininformation.MoreLoginInformationActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.series.SeriesActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.series.minileaderboard.SeriesMiniOverallLeaderboardActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.series.minileaderboard.SeriesMiniOverallLeaderboardPlace;
import com.sap.sailing.gwt.home.mobile.places.solutions.SolutionsActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.start.StartActivity;
import com.sap.sailing.gwt.home.mobile.places.user.authentication.AuthenticationActivityProxy;
import com.sap.sailing.gwt.home.mobile.places.user.authentication.AuthenticationPlace;
import com.sap.sailing.gwt.home.mobile.places.user.profile.UserProfileActivityProxy;
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
import com.sap.sse.gwt.client.DOMUtils;

public class MobileActivityMapper implements ActivityMapper {
    private final MobileApplicationClientFactory clientFactory;
    private final ApplicationPlaceUpdater placeUpdater = new ApplicationPlaceUpdater();

    public MobileActivityMapper(MobileApplicationClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place rawPlace) {
        Place place = placeUpdater.getRealPlace(rawPlace);
        if (!SwitchingEntryPoint.hasMobileVersion(place)) {
            GWT.log("Place has no mobile view: " + place.getClass().getName());
            SwitchingEntryPoint.reloadApp();
            return null;
        }
        DOMUtils.scrollToTop(clientFactory.getRoot());
        if (place instanceof ErrorPlace) {
            return new ErrorActivityProxy((ErrorPlace) place, clientFactory);
        } else if (place instanceof StartPlace) {
            return new StartActivity((StartPlace) place, clientFactory);
        } else if (place instanceof EventsPlace) {
            return new EventsActivity((EventsPlace) place, clientFactory);
        } else if (place instanceof AbstractEventPlace) {
            return new EventActivityProxy((AbstractEventPlace) place, clientFactory);
        } else if (place instanceof SeriesMiniOverallLeaderboardPlace) {
            return new SeriesMiniOverallLeaderboardActivityProxy((SeriesMiniOverallLeaderboardPlace) place, clientFactory);
        } else if (place instanceof AbstractSeriesPlace) {
            return new SeriesActivityProxy((AbstractSeriesPlace) place, clientFactory);
        } else if (place instanceof SearchResultPlace) {
            return new SearchResultActivityProxy((SearchResultPlace) place, clientFactory);
        } else if (place instanceof SolutionsPlace) {
            return new SolutionsActivityProxy((SolutionsPlace) place, clientFactory);
        } else if (place instanceof AbstractUserProfilePlace) {
            return new UserProfileActivityProxy((AbstractUserProfilePlace) place, clientFactory);
        } else if (place instanceof AuthenticationPlace) {
            return new AuthenticationActivityProxy((AuthenticationPlace) place, clientFactory);
        } else if (place instanceof ConfirmationPlace) {
            return new ConfirmationActivityProxy((ConfirmationPlace) place, clientFactory);
        } else if (place instanceof PasswordResetPlace) {
            return new PasswordResetActivityProxy((PasswordResetPlace) place, clientFactory);
        } else if (place instanceof AbstractUserProfilePlace) {
            return new UserProfileActivityProxy((AbstractUserProfilePlace) place, clientFactory);
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
