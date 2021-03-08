package com.sap.sailing.gwt.ui.adminconsole.places.connectors;

import java.util.function.Function;

import com.sap.sse.gwt.adminconsole.AbstractFilterablePlace;

public class IgtimiAccountsPlace extends AbstractFilterablePlace {

    public IgtimiAccountsPlace(String token) {
        super(token);
    }
    
    public static class Tokenizer extends TablePlaceTokenizer<IgtimiAccountsPlace> {      

        @Override
        protected Function<String, IgtimiAccountsPlace> getPlaceFactory() {
            return IgtimiAccountsPlace::new;
        }
    }
}
