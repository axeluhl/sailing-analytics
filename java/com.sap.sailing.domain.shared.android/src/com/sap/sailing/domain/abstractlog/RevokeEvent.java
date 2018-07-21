package com.sap.sailing.domain.abstractlog;

import java.io.Serializable;

import com.sap.sailing.domain.abstractlog.race.RaceLog;

/**
 * Revokes the event referenced by {@link #getRevokedEventId()}.
 * A {@link RevokeEvent} cannot itself be revoked. Instead, e.g. the original event should be resubmitted.
 * @author Fredrik Teschke
 * @see Revokable
 *
 */
public interface RevokeEvent<VisitorT> extends AbstractLogEvent<VisitorT> {
    Serializable getRevokedEventId();
    
    /**
     * Short info of the revoked event, automatically added by {@link RaceLog#revokeEvent}.
     */
    String getRevokedEventShortInfo();
    
    /**
     * Optional reason for revocation of event.
     */
    String getReason();

    /**
     * {@link Class#getSimpleName() Simple class name} of the revoked event.
     */
    String getRevokedEventType();
}
