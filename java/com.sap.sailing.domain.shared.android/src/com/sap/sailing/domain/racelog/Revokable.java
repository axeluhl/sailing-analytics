package com.sap.sailing.domain.racelog;


/**
 * Any {@link RaceLogEvent} marked as {@code Revokable} can be revoked by a {@link RevokeEvent}.
 * This means that if an {@code RevokeEvent} {@code r} is added to the {@code RaceLog}, that
 * {@link RevokeEvent#getRevokedEventId() revokes} an already existing event {@code e}, then the
 * revoked event {@code e} will subsequently not appear in the event-iterators of the {@code RaceLog}
 * ({@link RaceLog#getUnrevokedEvents()}, {@link RaceLog#getUnrevokedEventsDescending()}).
 * <p>
 * The event {@code r} can only successufully revoke {@code e}, if {@code r}'s priority is higher
 * (author prio, timepoint), and if {@code e} is of an event type that implements {@code Revokable}.
 * @author Fredrik Teschke
 *
 */
public interface Revokable extends RaceLogEvent {

}
