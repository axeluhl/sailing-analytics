package com.sap.sailing.gwt.home.shared.places.imprint;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.home.shared.app.HasMobileVersion;

public class ImprintPlace extends AbstractBasePlace implements HasLocationTitle, HasMobileVersion {
    public static class Tokenizer implements PlaceTokenizer<ImprintPlace> {
        @Override
        public String getToken(ImprintPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public ImprintPlace getPlace(String url) {
            return new ImprintPlace();
        }
    }

    @Override
    public String getLocationTitle() {
        return TextMessages.INSTANCE.impressum();
    }
}
