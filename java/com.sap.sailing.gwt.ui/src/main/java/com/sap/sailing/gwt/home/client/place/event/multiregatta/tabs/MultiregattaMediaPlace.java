package com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs;

import com.google.gwt.place.shared.Prefix;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.shared.ExperimentalFeatures;
import com.sap.sailing.gwt.home.shared.app.MobileSupport;
import com.sap.sailing.gwt.home.shared.places.PlaceTokenPrefixes;
import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;

public class MultiregattaMediaPlace extends AbstractMultiregattaEventPlace implements MobileSupport {
    public MultiregattaMediaPlace(String id) {
        super(id);
    }
    
    public MultiregattaMediaPlace(EventContext context) {
        super(context);
    }

    @Prefix(PlaceTokenPrefixes.MultiregattaMedia)
    public static class Tokenizer extends AbstractEventPlace.Tokenizer<MultiregattaMediaPlace> {
        @Override
        protected MultiregattaMediaPlace getRealPlace(EventContext context) {
            return new MultiregattaMediaPlace(context);
        }
    }
    
    @Override
    public boolean hasMobileVersion() {
        return ExperimentalFeatures.SHOW_MEDIA_PAGE_ON_MOBILE;
    }
}
