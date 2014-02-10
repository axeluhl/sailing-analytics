package com.sap.sailing.domain.racelog.tracking;

import java.io.Serializable;

import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;

/**
 * The event referenced by {@link #getRevokedEventId()} should be revoked, that is to say the {@link RaceLog}
 * should ignore it in {@link RaceLog#getUnrevokedEvents()}.
 * 
 * A {@link RevokeEvent} cannot itself be revoked. Instead, e.g. the original event should be resubmitted.
 * @author Fredrik Teschke
 *
 */
public interface RevokeEvent extends RaceLogEvent {
    Serializable getRevokedEventId();
}
