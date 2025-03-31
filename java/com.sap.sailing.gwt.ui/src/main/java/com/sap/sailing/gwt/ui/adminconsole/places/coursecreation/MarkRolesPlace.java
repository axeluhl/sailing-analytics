package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import java.util.function.Function;

public class MarkRolesPlace extends AbstractCourseCreationPlace {
    public MarkRolesPlace(String token) {
        super(token);
    }

    public static class Tokenizer extends TablePlaceTokenizer<MarkRolesPlace> {      
        @Override
        protected Function<String, MarkRolesPlace> getPlaceFactory() {
            return MarkRolesPlace::new;
        }
    }
}
