package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sailing.gwt.ui.adminconsole.places.AbstractFilterablePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleViewImpl;

public class IgtimiAccountsPlace extends AbstractFilterablePlace {

    public IgtimiAccountsPlace(String token) {
        super(token);
    }

    @Override
    public String getVerticalTabName() {
        return AdminConsoleViewImpl.CONNECTORS;
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<IgtimiAccountsPlace> {      

        @Override
        protected Function<String, IgtimiAccountsPlace> getPlaceFactory() {
            return IgtimiAccountsPlace::new;
        }
    }
}
