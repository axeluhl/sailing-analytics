package com.sap.sailing.domain.markpassinghash;

import org.json.simple.JSONObject;

public interface TrackedRaceHashFingerprint {

    JSONObject toJson();

    public int getCompetitor();

    public int getStart();

    public int getEnd();

    public int getWaypoints();

    public int getNumberOfGPSFixes();

    public int getGpsFixes();

}
