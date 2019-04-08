package com.sap.sailing.gwt.home.shared.places.user.profile.settings;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;


public class UserProfileSettingsPlace extends AbstractUserProfilePlace implements HasMobileVersion {

    @Prefix(PlaceTokenPrefixes.UserProfileSettings)
    public static class Tokenizer extends AbstractUserProfilePlace.Tokenizer<UserProfileSettingsPlace> {
        @Override
        protected UserProfileSettingsPlace getRealPlace() {
            return new UserProfileSettingsPlace();
        }
    }
}
