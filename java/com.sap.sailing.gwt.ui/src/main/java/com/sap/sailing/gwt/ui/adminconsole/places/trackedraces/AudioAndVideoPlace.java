package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class AudioAndVideoPlace extends AbstractFilterablePlace {
    public AudioAndVideoPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<AudioAndVideoPlace> {      
        @Override
        protected Function<String, AudioAndVideoPlace> getPlaceFactory() {
            return AudioAndVideoPlace::new;
        }
    }
}
