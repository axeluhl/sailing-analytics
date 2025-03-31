package com.sap.sailing.domain.abstractlog.race;


/**
 * This RaceLog event type confirms the entered positioning list. Since we do not guarantee the order of incoming events
 * from the app (due to possible connectivity issues), this event type contains the the confirmed positioning list
 * again. Although this racelog event type contains the same information such as
 * {@link RaceLogFinishPositioningListChangedEvent} we keep it for backwards compatibility reasons.
 */
public interface RaceLogFinishPositioningConfirmedEvent extends RaceLogFinishPositioningEvent {
    
}
