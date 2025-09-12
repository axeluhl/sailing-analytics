package com.sap.sailing.domain.maneuverhash;


import com.sap.sailing.domain.tracking.TrackedRace;
import org.json.simple.JSONObject;

public interface ManeuverRaceFingerprintFactory {
    ManeuverRaceFingerprint createManeuverFringerprint (TrackedRace TrackedRace);
    
    ManeuverRaceFingerprint fromJson (JSONObject json); // Prüfen ob relevent ist...

}
