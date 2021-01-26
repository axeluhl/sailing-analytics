package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class UserManagementPlace extends AbstractFilterablePlace {
    public UserManagementPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<UserManagementPlace> {      

        @Override
        protected Function<String, UserManagementPlace> getPlaceFactory() {
            return UserManagementPlace::new;
        }
    }
}
