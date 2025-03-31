package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import com.google.gwt.place.shared.PlaceTokenizer;

public class MasterDataImportPlace extends AbstractAdvancedPlace {
    public MasterDataImportPlace(String token) {
        super(token);
    }

    public static class Tokenizer implements PlaceTokenizer<MasterDataImportPlace> {
        @Override
        public String getToken(final MasterDataImportPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public MasterDataImportPlace getPlace(final String token) {
            return new MasterDataImportPlace(token);
        }
    }
}
