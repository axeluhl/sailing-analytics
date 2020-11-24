package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class FileStoragePlace extends AbstractAdvancedPlace {
    public FileStoragePlace(String token) {
        super(token);
    }

    public static class Tokenizer implements PlaceTokenizer<FileStoragePlace> {
        @Override
        public String getToken(final FileStoragePlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public FileStoragePlace getPlace(final String token) {
            return new FileStoragePlace(token);
        }
    }
}
