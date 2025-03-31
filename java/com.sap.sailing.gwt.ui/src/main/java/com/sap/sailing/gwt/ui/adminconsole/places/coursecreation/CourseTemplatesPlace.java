package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import java.util.function.Function;

public class CourseTemplatesPlace extends AbstractCourseCreationPlace {
    public CourseTemplatesPlace(String token) {
        super(token);
    }

    public static class Tokenizer extends TablePlaceTokenizer<CourseTemplatesPlace> {      
        @Override
        protected Function<String, CourseTemplatesPlace> getPlaceFactory() {
            return CourseTemplatesPlace::new;
        }
    }
}
