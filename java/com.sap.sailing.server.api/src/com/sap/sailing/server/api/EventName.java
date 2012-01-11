package com.sap.sailing.server.api;

public class EventName implements EventIdentifier {
    private static final long serialVersionUID = 5975000495693192305L;
    
    private String eventName;

    EventName() {}
    
    public EventName(String eventName) {
        super();
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }
    
    @Override
    public Object getEvent(EventFetcher eventFetcher) {
        return eventFetcher.getEvent(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventName == null) ? 0 : eventName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventName other = (EventName) obj;
        if (eventName == null) {
            if (other.eventName != null)
                return false;
        } else if (!eventName.equals(other.eventName))
            return false;
        return true;
    }
    
}
