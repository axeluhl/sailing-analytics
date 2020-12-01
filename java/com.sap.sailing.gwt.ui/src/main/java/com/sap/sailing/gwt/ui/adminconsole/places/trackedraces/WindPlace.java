package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;

public class WindPlace extends AbstractFilterablePlace {
    public WindPlace(String token) {
        super(token);
    }

    public static class Tokenizer extends TablePlaceTokenizer<WindPlace> {      

        @Override
        protected Function<String, WindPlace> getPlaceFactory() {
            return WindPlace::new;
        }
    }
}
