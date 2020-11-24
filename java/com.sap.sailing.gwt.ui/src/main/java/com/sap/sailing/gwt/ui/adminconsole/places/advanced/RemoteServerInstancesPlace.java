package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class RemoteServerInstancesPlace extends AbstractFilterablePlace {
    public RemoteServerInstancesPlace(String token) {
        super(token);
    }

    // TODO bug5288 this method shouldn't have static information redundant with how AdminConsoleViewImpl assembles panels in tabs
    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.ADVANCED;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<RemoteServerInstancesPlace> {      
        @Override
        protected Function<String, RemoteServerInstancesPlace> getPlaceFactory() {
            return RemoteServerInstancesPlace::new;
        }
    }
}
