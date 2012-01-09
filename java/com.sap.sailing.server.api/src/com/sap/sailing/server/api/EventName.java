package com.sap.sailing.server.api;

public class EventName implements EventIdentifier {
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
    public boolean equals(Object obj) {
        boolean result = false;
        
        if (obj instanceof EventName) {
            EventName e = (EventName) obj;
            result = e.eventName.equals(this.eventName);
        }
        
        return result;
    }
}
