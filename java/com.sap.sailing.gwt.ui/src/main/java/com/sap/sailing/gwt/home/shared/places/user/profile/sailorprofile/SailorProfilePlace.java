package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.UUID;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;

public class SailorProfilePlace extends AbstractUserProfilePlace implements HasMobileVersion {

    private final UUID sailorProfileUuid;
    private final boolean createNew;

    public SailorProfilePlace(UUID sailorProfileUuid) {
        this(sailorProfileUuid, false);
    }

    private SailorProfilePlace(UUID sailorProfileUuid, boolean createNew) {
        super();
        this.createNew = createNew;
        this.sailorProfileUuid = sailorProfileUuid;
    }

    public SailorProfilePlace(boolean createNew) {
        this(null, createNew);
    }

    public SailorProfilePlace() {
        this(null);
    }

    public UUID getSailorProfileUuid() {
        return sailorProfileUuid;
    }

    public boolean isCreateNew() {
        return createNew;
    }

    @Prefix(PlaceTokenPrefixes.SailorProfile)
    public static class Tokenizer implements PlaceTokenizer<SailorProfilePlace> {
        @Override
        public SailorProfilePlace getPlace(String token) {
            if (token == null || "".equals(token)) {
                return new SailorProfilePlace(null);
            } else {
                try {
                    UUID uuid = UUID.fromString(token);
                    return new SailorProfilePlace(uuid);
                } catch (Exception e) {
                    return new SailorProfilePlace(null);
                }
            }
        }

        @Override
        public String getToken(SailorProfilePlace place) {
            return place.getSailorProfileUuid() == null ? "" : place.getSailorProfileUuid().toString();
        }
    }
}
