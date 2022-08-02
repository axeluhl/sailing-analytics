package com.sap.sailing.domain.markpassinghash.impl;

import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.markpassinghash.TrackedRaceHashFingerprint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class TrackedRaceHashFingerprintImpl implements TrackedRaceHashFingerprint {
    private static final Logger logger = Logger.getLogger(TrackedRaceHashFingerprintImpl.class.getName());

    private final int competitorHash;
    private final int startHash; // TODO use TimePoint instead?
    private final int endHash; // TODO use TimePoint instead?
    private final int waypointsHash;
    private final int numberOfGPSFixesHash;
    private final int gpsFixesHash;
    
    private static enum JSON_FIELDS { COMPETITOR, START_TIME, END_TIME, EAYPOINTS, NUMBEROFGPSFIXES, GPSFIXES };

    public TrackedRaceHashFingerprintImpl(TrackedRace trackedRace) {
    }
    
    public TrackedRaceHashFingerprintImpl(JSONObject json) {
        JSONObject result = new JSONObject();
        result.put(COMPETITOR, hashValues.get(TypeOfHash.COMPETITOR));
        result.put(START, hashValues.get(TypeOfHash.START));
        result.put(END, hashValues.get(TypeOfHash.END));
        result.put(WAYYPOINTS, hashValues.get(TypeOfHash.WAYPOINTS));
        result.put(NUMBEROFGPSFIXES, hashValues.get(TypeOfHash.NUMBEROFGPSFIXES));
        result.put(GPSFIXES, hashValues.get(TypeOfHash.GPSFIXES));
        return result;
    }

    @Override
    public JSONObject toJson() {
        return null;
    }

    @Override
    public boolean matches(TrackedRace trackedRace) {
        TrackedRaceHashForMarkPassingCalculationFactoryImpl factory = new TrackedRaceHashForMarkPassingCalculationFactoryImpl();
        final boolean result;
        if (getWaypoints() != trackedRaceHashFingerprint.getWaypoints()) {
            result = false;
        } else if (competitorHash != calculateHashForCompetitors(trackedRace)) {
            result = false;
        } else if (getStart() != trackedRaceHashFingerprint.getStart()) {
            result = false;
        } else if (getEnd() != trackedRaceHashFingerprint.getEnd()) {
            result = false;
        } else if (getNumberOfGPSFixes() != trackedRaceHashFingerprint.getNumberOfGPSFixes()) {
            result = false;
        } else if (getGpsFixes() != trackedRaceHashFingerprint.getGpsFixes()) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }
    
    public TrackedRaceHashFingerprintImpl create(TrackedRace trackedRace) {
        int hashForWaypoints = calculateHashForWaypoints(trackedRace);
        int hashForCompetitors = calculateHashForCompetitors(trackedRace);
        int hashForStart = calculateHashForStart(trackedRace);
        int hashForEnd = calculateHashForEnd(trackedRace);
        int hashForNumberOfGPSFixes = calculateHashForNumberOfGPSFixes(trackedRace);
        int hashForGPSFixes = calculateHashForGPSFixes(trackedRace);
        return new TrackedRaceHashFingerprintImpl(hashForWaypoints, hashForCompetitors, hashForStart, hashForEnd,
                hashForNumberOfGPSFixes, hashForGPSFixes);
    }

    private int calculateHashForCompetitors(TrackedRace trackedRace) {
        int hashForCompetitors = 0;
        for (Competitor c : trackedRace.getRace().getCompetitors()) {
            hashForCompetitors = hashForCompetitors + calculateHashForCompetitor(c);
        }
        return hashForCompetitors;
    }

    private int calculateHashForCompetitor(Competitor c) {
        int res = 0;
        try {
            res = res ^ c.getId().hashCode();
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for competitor " + c + " failed: " + e);
        }
        return res;
    }

    private int calculateHashForStart(TrackedRace trackedRace) {
        int res = 0;
        try {
            res = res ^ calculateHashForTimePoint(trackedRace.getStartOfTracking());
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("An error occured when getting the start of tracking: " + e);
        }
        try {
            // Maybe that could be solved by using .getStartOfRace(boolean inferred)
            if (trackedRace.getTrackedRegatta().getRegatta().useStartTimeInference() == false) {
                res = res ^ calculateHashForTimePoint(trackedRace.getStartOfRace());
                res = (res << 5) - res;
            }
        } catch (Exception e) {
            logger.info("An error occured when getting the starttime: " + e);
        }
        return res;
    }

    private int calculateHashForEnd(TrackedRace trackedRace) {
        int res = 0;
        try {
            res = res ^ calculateHashForTimePoint(trackedRace.getEndOfTracking());
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for end of Tracking failed: " + e);
        }
        return res;
    }

    private int calculateHashForNumberOfGPSFixes(TrackedRace trackedRace) {
        Iterable<Waypoint> waypoints = trackedRace.getRace().getCourse().getWaypoints();
        int count = 0;
        for (Waypoint w : waypoints) {
            for (Mark m : w.getMarks()) {
                Iterable<GPSFix> gpsTrack = null;
                try {
                    trackedRace.getTrack(m).lockForRead();
                    gpsTrack = trackedRace.getTrack(m).getFixes();
                    // Unused warning since we need the individual Fixes only to count
                    for (int i = 0; i < Util.size(gpsTrack); i++) {
                        count++;
                    }
                } catch (Exception e) {
                    logger.info("Counting of GPSFixes produced an error: " + e);
                } finally {
                    trackedRace.getTrack(m).unlockAfterRead();
                }
            }
        }
        return count;
    }

    private int calculateHashForGPSFixes(TrackedRace trackedRace) {
        Iterable<Waypoint> waypoints = trackedRace.getRace().getCourse().getWaypoints();
        int res = 0;
        for (Waypoint w : waypoints) {
            for (Mark m : w.getMarks()) {
                Iterable<GPSFix> gpsTrack = null;
                try {
                    trackedRace.getTrack(m).lockForRead();
                    gpsTrack = trackedRace.getTrack(m).getFixes();
                    for (GPSFix gf : gpsTrack) {
                        res = res ^ calculateHashForTimePoint(gf.getTimePoint());
                        res = (res << 5) - res;
                        res = res ^ calculateHashForPosition(gf.getPosition());
                        res = (res << 5) - res;
                    }
                } catch (Exception e) {
                    logger.info("Hash calculation for GPSFixes of the mark " + m + "failed: " + e);
                } finally {
                    trackedRace.getTrack(m).unlockAfterRead();
                }
            }
        }
        return res;
    }

    private int calculateHashForTimePoint(TimePoint tp) {
        int res = 0;
        // How problematic is this cast?
        try {
            res = res ^ (int) tp.asMillis();
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Ther");
        }

        return res;
    }

    private int calculateHashForPosition(Position p) {
        int res = 0;
        res = res ^ (int) p.getLatDeg();
        res = res ^ (int) p.getLngDeg();
        res = (res << 5) - res;
        return res;
    }

    private int calculateHashForWaypoints(TrackedRace trackedRace) {
        Iterable<Waypoint> waypoints = trackedRace.getRace().getCourse().getWaypoints();
        int res = 0;
        for (Waypoint p : waypoints) {
            res = res ^ p.getId().hashCode();
            try {
                res = res ^ p.getPassingInstructions().hashCode();
            } catch (Exception e) {
                logger.info("Hash calculation for Waypoints failed: " + e);
            }
        }
        return res;
    }
}
