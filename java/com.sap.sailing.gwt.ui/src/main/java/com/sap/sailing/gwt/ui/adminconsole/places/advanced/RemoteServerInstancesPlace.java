package com.sap.sailing.gwt.ui.adminconsole.places.advanced;

import java.util.function.Supplier;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class RemoteServerInstancesPlace extends AbstractFilterablePlace {

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.ADVANCED;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<RemoteServerInstancesPlace> {      

        @Override
        protected Supplier<RemoteServerInstancesPlace> getPlaceFactory() {
            return RemoteServerInstancesPlace::new;
        }
    }
}
