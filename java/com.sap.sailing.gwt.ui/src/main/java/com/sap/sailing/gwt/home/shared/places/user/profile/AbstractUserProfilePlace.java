package com.sap.sailing.gwt.home.shared.places.user.profile;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;

public abstract class AbstractUserProfilePlace extends Place {

    public String getTitle(String eventName) {
        // TODO
        return TextMessages.INSTANCE.sapSailing() + " - " + eventName;
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
        
        abstract PLACE getRealPlace();
    }
}
