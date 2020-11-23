package com.sap.sailing.gwt.ui.adminconsole.places.regattas;

import java.util.function.Supplier;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;

public class RegattasPlace extends AbstractFilterablePlace {

    public static class Tokenizer extends TablePlaceTokenizer<RegattasPlace> {      

        @Override
        protected Supplier<RegattasPlace> getPlaceFactory() {
            return RegattasPlace::new;
        }
    }
    
}