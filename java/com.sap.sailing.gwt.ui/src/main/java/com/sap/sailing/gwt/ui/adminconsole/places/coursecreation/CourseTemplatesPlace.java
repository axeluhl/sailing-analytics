package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import java.util.function.Supplier;

public class CourseTemplatesPlace extends AbstractCourseCreationPlace {
    
    public static class Tokenizer extends TablePlaceTokenizer<CourseTemplatesPlace> {      

        @Override
        protected Supplier<CourseTemplatesPlace> getPlaceFactory() {
            return CourseTemplatesPlace::new;
        }
    }
    
}
