package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import java.util.function.Supplier;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class UserManagementPlace extends AbstractFilterablePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.ADVANCED;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<UserManagementPlace> {      

        @Override
        protected Supplier<UserManagementPlace> getPlaceFactory() {
            return UserManagementPlace::new;
        }
    }
}
