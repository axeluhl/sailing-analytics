package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class FileStoragePlace extends AbstractAdvancedPlace {
    
    public FileStoragePlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<FileStoragePlace> {
        @Override
        public String getToken(final FileStoragePlace place) {
            return "";
        }

        @Override
        public FileStoragePlace getPlace(final String token) {
            return new FileStoragePlace();
        }
    }
    
}
