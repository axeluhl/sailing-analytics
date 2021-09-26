package com.sap.sailing.gwt.ui.adminconsole.places.regattas;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class RegattasPlace extends AbstractFilterablePlace {
    public RegattasPlace(String token) {
        super(token);
    }

    public static class Tokenizer extends TablePlaceTokenizer<RegattasPlace> {      
        @Override
        protected Function<String, RegattasPlace> getPlaceFactory() {
            return RegattasPlace::new;
        }
    }
}