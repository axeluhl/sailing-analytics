package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import com.google.gwt.place.shared.PlaceTokenizer;

public class CourseTemplatesPlace extends AbstractCourseCreationPlace {
    
    public CourseTemplatesPlace() { 
    }

    public static class Tokenizer implements PlaceTokenizer<CourseTemplatesPlace> {
        @Override
        public String getToken(final CourseTemplatesPlace place) {
            return "";
        }

        @Override
        public CourseTemplatesPlace getPlace(final String token) {
            return new CourseTemplatesPlace();
        }
    }
    
}
