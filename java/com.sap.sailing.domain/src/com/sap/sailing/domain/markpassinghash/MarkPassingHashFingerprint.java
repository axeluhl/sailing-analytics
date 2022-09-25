package com.sap.sailing.domain.markpassinghash;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * This class represents several hash values of a race, which will be appointed during instantiation. These can be used
 * to determine whether the {@link Markpassing}s of a race changed.
 * 
 * @author Fabian Kallenbach (i550803)
 */
public interface MarkPassingHashFingerprint {
    /**
     * Returns a {@link JSONObject} of the hash values.
     */
    JSONObject toJson();

    /**
     * Calculates the hash values of the {@link TrackedRace}. Returns <code>true</code> if the calculated hash values of
     * the {@link TrackedRace} equals the one established during the instantiation of this class.
     * 
     * The calculation itself is based on a fail fast approache and returns <code>false</code> in the moment that one of
     * the vauels isn't matching and then doesn't execute the calculations for the remaining hash values.
     */
    boolean matches(TrackedRace trackedRace);
}
