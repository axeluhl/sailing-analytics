package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import java.util.function.Function;

public class MarkTemplatesPlace extends AbstractCourseCreationPlace {
    public MarkTemplatesPlace(String token) {
        super(token);
    }

    public static class Tokenizer extends TablePlaceTokenizer<MarkTemplatesPlace> {      
        @Override
        protected Function<String, MarkTemplatesPlace> getPlaceFactory() {
            return MarkTemplatesPlace::new;
        }
    }
    
}
