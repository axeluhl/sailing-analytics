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
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class TrackedRaceHashFingerprintImpl implements TrackedRaceHashFingerprint {
    private static final Logger logger = Logger.getLogger(TrackedRaceHashFingerprintImpl.class.getName());

    private final int competitorHash;
    private final int startHash; // TODO use TimePoint instead? -> problem would be that we use startOfTracking and startOfRace if it is allowed
    private final int endHash; // TODO use TimePoint instead?
    private final int waypointsHash;
    private final int numberOfGPSFixesHash;
    private final int gpsFixesHash;

    private static enum JSON_FIELDS {
        COMPETITOR, START_TIME, END_TIME, WAYPOINTS, NUMBEROFGPSFIXES, GPSFIXES
    };

    public TrackedRaceHashFingerprintImpl(TrackedRace trackedRace) {
        this.competitorHash = calculateHashForCompetitors(trackedRace);
        this.startHash = calculateHashForStart(trackedRace);
        this.endHash = calculateHashForStart(trackedRace);
        this.waypointsHash = calculateHashForWaypoints(trackedRace);
        this.numberOfGPSFixesHash = calculateHashForNumberOfGPSFixes(trackedRace);
        this.gpsFixesHash = calculateHashForGPSFixes(trackedRace);
    }

    public TrackedRaceHashFingerprintImpl(JSONObject json) {
        int competitorHash = 0;
        int startHash = 0;
        int endHash = 0;
        int waypointsHash = 0;
        int numberOfGPSFixesHash = 0;
        int gpsFixesHash = 0;
        try {
            competitorHash = (int) json.get(JSON_FIELDS.COMPETITOR);
            startHash = (int) json.get(JSON_FIELDS.START_TIME);
            endHash = (int) json.get(JSON_FIELDS.END_TIME);
            waypointsHash = (int) json.get(JSON_FIELDS.WAYPOINTS);
            numberOfGPSFixesHash = (int) json.get(JSON_FIELDS.NUMBEROFGPSFIXES);
            gpsFixesHash = (int) json.get(JSON_FIELDS.GPSFIXES);
        } catch (Exception e) {
            logger.info("An error occured: " + e);
        }
        this.competitorHash = competitorHash;
        this.startHash = startHash;
        this.endHash = endHash;
        this.waypointsHash = waypointsHash;
        this.numberOfGPSFixesHash = numberOfGPSFixesHash;
        this.gpsFixesHash = gpsFixesHash;
    }

    @Override
    public JSONObject toJson() {
        JSONObject result = new JSONObject();
        result.put(JSON_FIELDS.COMPETITOR, competitorHash);
        result.put(JSON_FIELDS.START_TIME, startHash);
        result.put(JSON_FIELDS.END_TIME, endHash);
        result.put(JSON_FIELDS.WAYPOINTS, waypointsHash);
        result.put(JSON_FIELDS.NUMBEROFGPSFIXES, numberOfGPSFixesHash);
        result.put(JSON_FIELDS.GPSFIXES, gpsFixesHash);
        return result;
    }

    @Override
    public boolean matches(TrackedRace trackedRace) {
        final boolean result;
        if (waypointsHash != calculateHashForWaypoints(trackedRace)) {
            result = false;
        } else if (competitorHash != calculateHashForCompetitors(trackedRace)) {
            result = false;
        } else if (startHash != calculateHashForStart(trackedRace)) {
            result = false;
        } else if (endHash != calculateHashForEnd(trackedRace)) {
            result = false;
        } else if (numberOfGPSFixesHash != calculateHashForNumberOfGPSFixes(trackedRace)) {
            result = false;
        } else if (gpsFixesHash != calculateHashForGPSFixes(trackedRace)) {
            result = false;
        } else {
            result = true;
        }
        return result;
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
        int res = 1;
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
            Iterable<Mark> marks = p.getMarks();
            for (Mark m : marks) {
                try {
                    res = res ^ m.getOriginatingMarkPropertiesIdOrNull().hashCode();
                    res = res ^ m.getOriginatingMarkTemplateIdOrNull().hashCode();
                    res = res ^ m.getType().hashCode();
                    res = (res << 5) - res;
                } catch (Exception e) {
                    logger.info("Getting the OriginationMarkPropertiesId and OriginationgMarkTemplateId for the mark " + m + " failed: " + e);
                }
                Iterable<Mark> controlPoints = m.getMarks();
                for (Mark cp : controlPoints) {
                    try {
                        res = res ^ cp.getOriginatingMarkPropertiesIdOrNull().hashCode();
                        res = res ^ cp.getOriginatingMarkTemplateIdOrNull().hashCode();
                        res = (res << 5) - res;
                    } catch (Exception e) {
                        logger.info("Getting the OriginationMarkPropertiesId and OriginationgMarkTemplateId  for the controlPoint " + cp + " failed: " + e);
                    }
                }
            }
            res = res ^ p.getName().hashCode();
            try {
                res = res ^ p.getPassingInstructions().hashCode();
            } catch (Exception e) {
                logger.info("Hash calculation for Waypoints failed: " + e);
            }
            res = (res << 5) - res;
        }
        return res;
    }
}
