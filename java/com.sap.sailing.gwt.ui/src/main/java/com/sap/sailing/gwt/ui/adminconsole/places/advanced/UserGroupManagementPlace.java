package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import java.util.function.Supplier;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class UserGroupManagementPlace extends AbstractFilterablePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.ADVANCED;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<UserGroupManagementPlace> {      

        @Override
        protected Supplier<UserGroupManagementPlace> getPlaceFactory() {
            return UserGroupManagementPlace::new;
        }
    }
}
