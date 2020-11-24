package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class UserManagementPlace extends AbstractFilterablePlace {
    public UserManagementPlace(String token) {
        super(token);
    }

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.ADVANCED;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<UserManagementPlace> {      

        @Override
        protected Function<String, UserManagementPlace> getPlaceFactory() {
            return UserManagementPlace::new;
        }
    }
}
