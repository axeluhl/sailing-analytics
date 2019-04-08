package com.sap.sailing.domain.abstractlog.race;

/**
 * Captures a temporary race result description. The result is not final yet and needs to be confirmed by
 * a {@link RaceLogFinishPositioningConfirmedEvent} before it is adopted by other parts of the model. This
 * type of event can be used to store the interims state of a result editing process before the result
 * if committed.
 */
public interface RaceLogFinishPositioningListChangedEvent extends RaceLogFinishPositioningEvent {
    
}