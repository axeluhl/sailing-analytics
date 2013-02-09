package com.sap.sailing.domain.racelog;

/**
 * Capsulating store interface providing a {@link RaceLog} for a given regatta / race combination.
 *
 */
public interface RaceLogStore {
	RaceLog getRaceLog(RaceColumnIdentifier identifier);
}
