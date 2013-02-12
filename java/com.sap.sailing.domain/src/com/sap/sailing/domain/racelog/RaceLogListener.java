package com.sap.sailing.domain.racelog;

/**
 * Implementors may use this interface to listen for added {@link RaceLogEvent}.
 */
public interface RaceLogListener {
	
	/**
	 * This method is called for any event that is added to the host.
	 * @param event
	 */
	void eventReceived(RaceLogEvent event);
	
	/**
	 * This method is called additionally if the event is a {@link RaceLogFlagEvent}.
	 * @param flagEvent
	 */
	void flagEventReceived(RaceLogFlagEvent flagEvent);
	
	/**
	 * This method is called additionally if the event is a {@link RaceLogStartTimeEvent}.
	 * @param startTimeEvent
	 */
	void startTimeEventReceived(RaceLogStartTimeEvent startTimeEvent);
}
