package com.sap.sailing.gwt.home.shared.places.imprint;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.common.client.navigation.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.AbstractBasePlace;

public class ImprintPlace extends AbstractBasePlace implements HasLocationTitle, HasMobileVersion {
    public ImprintPlace(String token) {
        super(token);
    }

    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.impressum();
    }

    @Prefix(PlaceTokenPrefixes.Imprint)
    public static class Tokenizer implements PlaceTokenizer<ImprintPlace> {
        @Override
        public String getToken(ImprintPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public ImprintPlace getPlace(String token) {
            return new ImprintPlace(token);
        }
    }
}
