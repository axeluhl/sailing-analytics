package com.sap.sailing.gwt.home.shared.places.morelogininformation;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;

public class MoreLoginInformationPlace extends Place implements HasMobileVersion {
    @Prefix(PlaceTokenPrefixes.AboutAccount)
    public static class Tokenizer implements PlaceTokenizer<MoreLoginInformationPlace> {
        @Override
        public String getToken(MoreLoginInformationPlace place) {
            return null;
        }

        @Override
        public MoreLoginInformationPlace getPlace(String token) {
            return new MoreLoginInformationPlace();
        }
    }
}
