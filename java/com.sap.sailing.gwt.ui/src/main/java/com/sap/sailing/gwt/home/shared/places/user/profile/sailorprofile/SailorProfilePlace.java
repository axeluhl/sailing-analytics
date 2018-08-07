package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.UUID;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;

public class SailorProfilePlace extends AbstractUserProfilePlace implements HasMobileVersion {

    private final UUID sailorProfileUuid;

    public SailorProfilePlace(UUID sailorProfileUuid) {
        super();
        this.sailorProfileUuid = sailorProfileUuid;
    }

    public SailorProfilePlace() {
        this(null);
    }

    public UUID getSailorProfileUuid() {
        return sailorProfileUuid;
    }

    @Prefix(PlaceTokenPrefixes.SailorProfile)
    public static class Tokenizer implements PlaceTokenizer<SailorProfilePlace> {
        @Override
        public SailorProfilePlace getPlace(String token) {
            try {
                UUID uuid = UUID.fromString(token);
                return new SailorProfilePlace(uuid);
            } catch (Exception e) {
                return new SailorProfilePlace(null);
            }
        }

        @Override
        public String getToken(SailorProfilePlace place) {
            return "";
        }
    }
}
