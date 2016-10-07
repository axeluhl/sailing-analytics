package com.sap.sailing.gwt.home.shared.places.user.profile.preferences;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;


public class UserProfilePreferencesPlace extends AbstractUserProfilePlace implements HasMobileVersion {

    @Prefix(PlaceTokenPrefixes.UserProfilePreferences)
    public static class Tokenizer extends AbstractUserProfilePlace.Tokenizer<UserProfilePreferencesPlace> {
        @Override
        protected UserProfilePreferencesPlace getRealPlace() {
            return new UserProfilePreferencesPlace();
        }
    }
}
