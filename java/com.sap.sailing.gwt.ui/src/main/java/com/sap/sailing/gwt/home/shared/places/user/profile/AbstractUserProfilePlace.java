package com.sap.sailing.gwt.home.shared.places.user.profile;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.shared.app.HasLocationTitle;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractUserProfilePlace extends Place implements HasLocationTitle {
    @Override
    public String getLocationTitle() {
        return StringMessages.INSTANCE.profile();
    }
    
    public static abstract class Tokenizer<PLACE extends AbstractUserProfilePlace> implements PlaceTokenizer<PLACE> {
        @Override
        public PLACE getPlace(String token) {
            return getRealPlace();
        }
        
        @Override
        public String getToken(PLACE place) {
            return "";
        }
        
        protected abstract PLACE getRealPlace();
    }
}
