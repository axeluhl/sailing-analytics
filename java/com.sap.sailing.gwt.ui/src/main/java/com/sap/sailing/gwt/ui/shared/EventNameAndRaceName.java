package com.sap.sailing.gwt.ui.shared;

public class EventNameAndRaceName implements EventAndRaceIdentifier {
    private String eventName;
    private String raceName;
    
    EventNameAndRaceName() {}
    
    public EventNameAndRaceName(String eventName, String raceName) {
        super();
        this.eventName = eventName;
        this.raceName = raceName;
    }
    public String getEventName() {
        return eventName;
    }
    public String getRaceName() {
        return raceName;
    }
    
    @Override
    public Object getRace(RaceFetcher raceFetcher) {
        return raceFetcher.getRace(this);
    }
    @Override
    public Object getTrackedRace(RaceFetcher raceFetcher) {
        return raceFetcher.getTrackedRace(this);
    }
    @Override
    public Object getExistingTrackedRace(RaceFetcher raceFetcher) {
        return raceFetcher.getExistingTrackedRace(this);
    }
    @Override
    public Object getEvent(EventFetcher eventFetcher) {
        return eventFetcher.getEvent(this);
    }

    @Override
    public String toString() {
        return raceName;
    }
    
    
}
