package com.sap.sailing.domain.maneuverhash;


import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.maneuverdetection.ManeuverDetector;
//import com.sap.sailing.domain.markpassingcalculation.MarkPassingCalculator;
//import com.sap.sailing.domain.markpassinghash.MarkPassingRaceFingerprintFactory;
//import com.sap.sailing.domain.tracking;
import com.sap.sailing.domain.tracking.TrackedRace;

public interface ManeuverRaceFingerprint {
    
    /**
     * Returns a {@link JSONObject} of the hash values.
     */
    JSONObject toJson();

    /**
     * Incrementally computes the composite fingerprint of the {@code trackedRace} and compares to this fingerprint
     * component by component. The fingerprint components are computed in ascending order of computational complexity,
     * trying to fail early / fast.<p>
     * 
     * The implementation may require to obtain the race's {@link Course#lockForRead() read lock}. Note that conversely
     * updates to the course that happen under the course's write lock will trigger listeners which may synchronize
     * on certain objects, such as the {@link ManeuverDetector}. Therefore, should this method be called
     * while holding an object monitor ("synchronized") that a course update listener may also require, make
     * sure to first obtain at least the course read lock before invoking this method. See also bug 5803.
     * 
     * @return {@code true} if the {@code trackedRace} produces a fingerprint equal to this one if passed to
     *         {@link MarkPassingRaceFingerprintFactory#createFingerprint(TrackedRace)}
     */
    boolean matches(TrackedRace trackedRace);

}
