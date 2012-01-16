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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((raceName == null) ? 0 : raceName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventNameAndRaceName other = (EventNameAndRaceName) obj;
        if (raceName == null) {
            if (other.raceName != null)
                return false;
        } else if (!raceName.equals(other.raceName))
            return false;
        return true;
    }
}
