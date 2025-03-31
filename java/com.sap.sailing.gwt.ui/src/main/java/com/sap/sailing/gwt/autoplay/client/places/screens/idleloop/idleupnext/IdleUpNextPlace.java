package com.sap.sailing.gwt.autoplay.client.places.screens.idleloop.idleupnext;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sse.common.Util.Pair;

public class IdleUpNextPlace extends Place {
    public static class Tokenizer implements PlaceTokenizer<IdleUpNextPlace> {
        @Override
        public String getToken(IdleUpNextPlace place) {
            return "";
        }

        @Override
        public IdleUpNextPlace getPlace(String token) {
            return new IdleUpNextPlace();
        }
    }

    private ArrayList<Pair<RegattaAndRaceIdentifier, Date>> raceToStartOfRace;

    public void setUpData(ArrayList<Pair<RegattaAndRaceIdentifier, Date>> raceToStartOfRace) {
        this.raceToStartOfRace = raceToStartOfRace;
    }

    public ArrayList<Pair<RegattaAndRaceIdentifier, Date>> getRaceToStartOfRace() {
        return raceToStartOfRace;
    }
}
