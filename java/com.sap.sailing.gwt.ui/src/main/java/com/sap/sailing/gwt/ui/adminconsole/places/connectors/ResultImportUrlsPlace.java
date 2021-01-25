package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import com.google.gwt.place.shared.PlaceTokenizer;

public class ResultImportUrlsPlace extends AbstractConnectorsPlace {
    public ResultImportUrlsPlace(String token) {
        super(token);
    }

    public static class Tokenizer implements PlaceTokenizer<ResultImportUrlsPlace> {
        @Override
        public String getToken(final ResultImportUrlsPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public ResultImportUrlsPlace getPlace(final String token) {
            return new ResultImportUrlsPlace(token);
        }
    }
}
