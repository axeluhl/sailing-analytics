package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;


public class SailorProfilePlace extends AbstractUserProfilePlace implements HasMobileVersion {

    @Prefix(PlaceTokenPrefixes.SailorProfile)
    public static class Tokenizer extends AbstractUserProfilePlace.Tokenizer<SailorProfilePlace> {
        @Override
        protected SailorProfilePlace getRealPlace() {
            return new SailorProfilePlace();
        }
    }
}
