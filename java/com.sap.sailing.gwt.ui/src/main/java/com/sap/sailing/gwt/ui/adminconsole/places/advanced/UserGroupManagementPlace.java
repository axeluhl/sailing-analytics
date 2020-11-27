package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class UserGroupManagementPlace extends AbstractFilterablePlace {
    public UserGroupManagementPlace(String token) {
        super(token);
    }

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.ADVANCED;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<UserGroupManagementPlace> {      
        @Override
        protected Function<String, UserGroupManagementPlace> getPlaceFactory() {
            return UserGroupManagementPlace::new;
        }
    }
}
