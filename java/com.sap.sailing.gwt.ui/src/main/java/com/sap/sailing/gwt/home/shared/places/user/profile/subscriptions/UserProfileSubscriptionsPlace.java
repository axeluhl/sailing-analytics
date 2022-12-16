package com.sap.sailing.gwt.home.shared.places.user.profile.subscriptions;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.common.client.navigation.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;

/**
 * User profile subscription place
 * 
 * @author Tu Tran
 */
public class UserProfileSubscriptionsPlace extends AbstractUserProfilePlace implements HasMobileVersion {
    @Prefix(PlaceTokenPrefixes.UserSubscription)
    public static class Tokenizer extends AbstractUserProfilePlace.Tokenizer<UserProfileSubscriptionsPlace> {
        @Override
        protected UserProfileSubscriptionsPlace getRealPlace() {
            return new UserProfileSubscriptionsPlace();
        }
    }
}
