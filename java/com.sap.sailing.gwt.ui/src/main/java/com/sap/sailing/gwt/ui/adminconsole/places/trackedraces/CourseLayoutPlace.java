package com.sap.sailing.gwt.ui.adminconsole.places.trackedraces;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class CourseLayoutPlace extends AbstractFilterablePlace {
    public CourseLayoutPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<CourseLayoutPlace> {      
        @Override
        protected Function<String, CourseLayoutPlace> getPlaceFactory() {
            return CourseLayoutPlace::new;
        }
    }
}
