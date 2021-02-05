package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class BoatsPlace extends AbstractFilterablePlace {
    public BoatsPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<BoatsPlace> {      
        @Override
        protected Function<String, BoatsPlace> getPlaceFactory() {
            return BoatsPlace::new;
        }
    }
}
