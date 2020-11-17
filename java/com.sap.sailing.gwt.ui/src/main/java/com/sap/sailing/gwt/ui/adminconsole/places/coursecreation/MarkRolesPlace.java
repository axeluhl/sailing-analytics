package com.sap.sailing.gwt.ui.adminconsole.places.coursecreation;

import java.util.function.Supplier;

public class MarkRolesPlace extends AbstractCourseCreationPlace {
    
    public static class Tokenizer extends TablePlaceTokenizer<MarkRolesPlace> {      

        @Override
        protected Supplier<MarkRolesPlace> getPlaceFactory() {
            return MarkRolesPlace::new;
        }
    }
    
}
