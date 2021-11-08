package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class YellowBrickEventsPlace extends AbstractFilterablePlace {
    public YellowBrickEventsPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<YellowBrickEventsPlace> {      
        @Override
        protected Function<String, YellowBrickEventsPlace> getPlaceFactory() {
            return YellowBrickEventsPlace::new;
        }
    }
}
