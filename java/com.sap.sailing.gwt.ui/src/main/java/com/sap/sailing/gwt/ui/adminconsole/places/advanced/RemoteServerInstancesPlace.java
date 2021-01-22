package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;

public class RemoteServerInstancesPlace extends AbstractFilterablePlace {
    public RemoteServerInstancesPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<RemoteServerInstancesPlace> {      
        @Override
        protected Function<String, RemoteServerInstancesPlace> getPlaceFactory() {
            return RemoteServerInstancesPlace::new;
        }
    }
}
