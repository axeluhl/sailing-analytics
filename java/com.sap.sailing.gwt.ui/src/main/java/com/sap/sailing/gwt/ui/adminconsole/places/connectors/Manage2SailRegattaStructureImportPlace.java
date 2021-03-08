package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import com.google.gwt.place.shared.PlaceTokenizer;

public class Manage2SailRegattaStructureImportPlace extends AbstractConnectorsPlace {
    public Manage2SailRegattaStructureImportPlace(String token) {
        super(token);
    }

    public static class Tokenizer implements PlaceTokenizer<Manage2SailRegattaStructureImportPlace> {
        @Override
        public String getToken(final Manage2SailRegattaStructureImportPlace place) {
            return place.getParametersAsToken();
        }

        @Override
        public Manage2SailRegattaStructureImportPlace getPlace(final String token) {
            return new Manage2SailRegattaStructureImportPlace(token);
        }
    }
    
}
