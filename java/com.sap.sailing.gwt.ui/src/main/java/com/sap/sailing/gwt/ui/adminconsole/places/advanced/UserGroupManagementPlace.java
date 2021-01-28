package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class UserGroupManagementPlace extends AbstractFilterablePlace {
    public UserGroupManagementPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<UserGroupManagementPlace> {      
        @Override
        protected Function<String, UserGroupManagementPlace> getPlaceFactory() {
            return UserGroupManagementPlace::new;
        }
    }
}
