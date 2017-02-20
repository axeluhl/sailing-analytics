package com.sap.sailing.gwt.autoplay.client.place.player;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;

public class PlayerPlace extends Place {
    private final String contextAndSettings;
    
    public PlayerPlace(String contextAndSettings) {
        this.contextAndSettings = contextAndSettings;
    }

    public String getContext() {
        return contextAndSettings;
    }

    public static class Tokenizer implements PlaceTokenizer<PlayerPlace> {
        SettingsToStringSerializer stringSerializer = new SettingsToStringSerializer();
        @Override
        public String getToken(PlayerPlace place) {
            return place.getContext();
        }

        @Override
        public PlayerPlace getPlace(String settingsAndContext) {
            return new PlayerPlace(settingsAndContext);
        }
    }

}
