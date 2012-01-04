package com.sap.sailing.server.api;


public class EventNameAndRaceName extends EventName implements EventAndRaceIdentifier {
    private static final long serialVersionUID = 3599904513673776450L;
    private String raceName;
    
    EventNameAndRaceName() {}
    
    public EventNameAndRaceName(String eventName, String raceName) {
        super(eventName);
        this.raceName = raceName;
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
    public String toString() {
        return raceName;
    }
}
