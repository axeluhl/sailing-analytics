package com.sap.sailing.domain.racelog;

public interface RaceLogChangedListener {
    
    /**
     * Called when an event is added to the {@link RaceLog}.
     * @param event that was added
     */
    public void eventAdded(RaceLogEvent event);
}
