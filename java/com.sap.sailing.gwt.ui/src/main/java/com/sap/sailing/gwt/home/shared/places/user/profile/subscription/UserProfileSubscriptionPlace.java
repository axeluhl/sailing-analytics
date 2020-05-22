package com.sap.sailing.gwt.home.shared.places.user.profile.subscription;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;

/**
 * User profile subscription place
 * 
 * @author Tu Tran
 */
public class UserProfileSubscriptionPlace extends AbstractUserProfilePlace implements HasMobileVersion {
    @Prefix(PlaceTokenPrefixes.UserSubscription)
    public static class Tokenizer extends AbstractUserProfilePlace.Tokenizer<UserProfileSubscriptionPlace> {
        @Override
        protected UserProfileSubscriptionPlace getRealPlace() {
            return new UserProfileSubscriptionPlace();
        }
    }
}
