package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import java.util.function.Function;

public class MarkPropertiesPlace extends AbstractCourseCreationPlace {
    public MarkPropertiesPlace(String token) {
        super(token);
    }

    public static class Tokenizer extends TablePlaceTokenizer<MarkPropertiesPlace> {      
        @Override
        protected Function<String, MarkPropertiesPlace> getPlaceFactory() {
            return MarkPropertiesPlace::new;
        }
    }
}
