package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class AudioAndVideoPlace extends AbstractFilterablePlace {
    public AudioAndVideoPlace(String token) {
        super(token);
    }

    // TODO bug5288 redundant with how AdminConsoleViewImpl assembles panels in tabs
    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.RACES;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<AudioAndVideoPlace> {      
        @Override
        protected Function<String, AudioAndVideoPlace> getPlaceFactory() {
            return AudioAndVideoPlace::new;
        }
    }
}
