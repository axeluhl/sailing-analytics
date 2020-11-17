package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import java.util.function.Supplier;

public class MarkTemplatesPlace extends AbstractCourseCreationPlace {
    
    public static class Tokenizer extends TablePlaceTokenizer<MarkTemplatesPlace> {      

        @Override
        protected Supplier<MarkTemplatesPlace> getPlaceFactory() {
            return MarkTemplatesPlace::new;
        }
    }
    
}
