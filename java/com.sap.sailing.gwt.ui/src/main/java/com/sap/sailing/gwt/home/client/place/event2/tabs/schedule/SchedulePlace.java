package com.sap.sailing.gwt.home.client.place.event2.tabs.schedule;

import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public class SchedulePlace extends AbstractEventRegattaPlace {
    public SchedulePlace(String id, String regattaId) {
        super(id, regattaId);
    }
    
    public SchedulePlace(EventContext context) {
        super(context);
    }

    public static class Tokenizer implements PlaceTokenizer<SchedulePlace> {
        @Override
        public String getToken(SchedulePlace place) {
            return place.getEventUuidAsString();
        }

        @Override
        public SchedulePlace getPlace(String token) {
            // TODO
            return new SchedulePlace(token, "");
        }
    }
}
