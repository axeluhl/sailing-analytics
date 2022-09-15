package com.sap.sailing.domain.markpassinghash;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tracking.TrackedRace;

public interface MarkPassingHashFingerprint {
    JSONObject toJson();

    boolean matches(TrackedRace trackedRace);
}
