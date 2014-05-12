package com.sap.sailing.domain.racelog;

import java.io.Serializable;

/**
 * Revokes the event referenced by {@link #getRevokedEventId()}.
 * A {@link RevokeEvent} cannot itself be revoked. Instead, e.g. the original event should be resubmitted.
 * @author Fredrik Teschke
 * @see Revokable
 *
 */
public interface RevokeEvent extends RaceLogEvent {
    Serializable getRevokedEventId();
}
