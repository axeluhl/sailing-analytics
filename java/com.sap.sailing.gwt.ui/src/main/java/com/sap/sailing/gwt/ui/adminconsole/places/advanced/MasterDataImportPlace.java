package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class MasterDataImportPlace extends AbstractAdvancedPlace {
    
    public MasterDataImportPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<MasterDataImportPlace> {
        @Override
        public String getToken(final MasterDataImportPlace place) {
            return "";
        }

        @Override
        public MasterDataImportPlace getPlace(final String token) {
            return new MasterDataImportPlace();
        }
    }
    
}
