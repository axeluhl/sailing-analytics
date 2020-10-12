package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import com.google.gwt.place.shared.PlaceTokenizer;

public class Manage2SailRegattaStructureImportPlace extends AbstractConnectorsPlace {
    
    public Manage2SailRegattaStructureImportPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<Manage2SailRegattaStructureImportPlace> {
        @Override
        public String getToken(final Manage2SailRegattaStructureImportPlace place) {
            return "";
        }

        @Override
        public Manage2SailRegattaStructureImportPlace getPlace(final String token) {
            return new Manage2SailRegattaStructureImportPlace();
        }
    }
    
}
