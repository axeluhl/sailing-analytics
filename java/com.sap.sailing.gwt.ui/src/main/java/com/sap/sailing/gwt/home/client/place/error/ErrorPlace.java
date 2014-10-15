package com.sap.sailing.gwt.home.client.place.error;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.AbstractBasePlace;

public class ErrorPlace extends AbstractBasePlace {
    
    public static class Tokenizer implements PlaceTokenizer<ErrorPlace> {
        @Override
        public String getToken(ErrorPlace place) {
            return null;
        }

        @Override
        public ErrorPlace getPlace(String token) {
            return new ErrorPlace();
        }
    }
}
