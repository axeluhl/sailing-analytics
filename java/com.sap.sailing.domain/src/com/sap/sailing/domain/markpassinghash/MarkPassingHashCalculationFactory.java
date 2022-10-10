package com.sap.sailing.domain.markpassinghash;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.markpassinghash.impl.MarkPassingHashCalculationFactoryImpl;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * Factory for the creation of a {@link MarkPassingHashFingerprint}.
 *
 * @author Fabian Kallenbach (i550803)
 */
public interface MarkPassingHashCalculationFactory {
    MarkPassingHashCalculationFactory INSTANCE = new MarkPassingHashCalculationFactoryImpl();

    /**
     * Creates a {@link MarkPassingHashFingerprint} out of a given {@link TrackedRace}.
     */
    MarkPassingHashFingerprint createFingerprint(TrackedRace trackedRace);

    /**
     * Creates a {@link MarkPassingHashFingerprint} out of a given {@link JSONObject}.
     */
    MarkPassingHashFingerprint fromJson(JSONObject json);
}