package com.sap.sailing.gwt.home.shared.places.user.profile;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;


public class UserProfileDefaultPlace extends AbstractUserProfilePlace implements HasMobileVersion {

    @Prefix(PlaceTokenPrefixes.UserProfileDefault)
    public static class Tokenizer extends AbstractUserProfilePlace.Tokenizer<UserProfileDefaultPlace> {
        @Override
        protected UserProfileDefaultPlace getRealPlace() {
            return new UserProfileDefaultPlace();
        }
    }
}
