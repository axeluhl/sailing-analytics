package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class ReplicationPlace extends AbstractAdvancedPlace {
    public ReplicationPlace(String token) {
        super(token);
    }

    public static class Tokenizer implements PlaceTokenizer<ReplicationPlace> {
        @Override
        public String getToken(final ReplicationPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public ReplicationPlace getPlace(final String token) {
            return new ReplicationPlace(token);
        }
    }
}
