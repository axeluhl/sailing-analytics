package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import java.util.function.Supplier;

public class MarkPropertiesPlace extends AbstractCourseCreationPlace {
    
    public MarkPropertiesPlace() { 
    }

    public static class Tokenizer extends TablePlaceTokenizer<MarkPropertiesPlace> {      

        @Override
        protected Supplier<MarkPropertiesPlace> getPlaceFactory() {
            return MarkPropertiesPlace::new;
        }
    }
    
}
