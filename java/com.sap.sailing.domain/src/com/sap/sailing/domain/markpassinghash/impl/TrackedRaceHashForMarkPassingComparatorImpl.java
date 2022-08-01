package com.sap.sailing.domain.markpassinghash.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.markpassinghash.TrackedRaceHashForMarkPassingComparator;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class TrackedRaceHashForMarkPassingComparatorImpl implements TrackedRaceHashForMarkPassingComparator {

    public enum TypeOfHash {
        COMPETITOR, START, END, WAYPOINTS, NUMBEROFGPSFIXES, GPSFIXES
    }

    private static final Logger logger = Logger.getLogger(TrackedRaceHashForMarkPassingComparatorImpl.class.getName());

    private TrackedRaceImpl trackedRace;
    private final Iterable<Waypoint> waypoints;
    private Map<TypeOfHash, Integer> hashValues;

    public TrackedRaceHashForMarkPassingComparatorImpl(TrackedRaceImpl trackedRace) {
        this.trackedRace = trackedRace;
        this.waypoints = trackedRace.getRace().getCourse().getWaypoints();
        this.hashValues = new HashMap<>();
    }

    public void calculateHash() {
        int competitorHash = 0;
        hashValues.put(TypeOfHash.WAYPOINTS, calculateHashForWaypoints());
        for (Competitor c : trackedRace.getRace().getCompetitors()) {
            competitorHash = competitorHash + calculateHashForCompetitor(c);
        }
        hashValues.put(TypeOfHash.COMPETITOR, competitorHash);
        hashValues.put(TypeOfHash.START, calculateHashForStart());
        hashValues.put(TypeOfHash.END, calculateHashForEnd());
        hashValues.put(TypeOfHash.NUMBEROFGPSFIXES, calculateHashForNumberOfGPSFixes());
        hashValues.put(TypeOfHash.GPSFIXES, calculateHashForGPSFixes());

        trackedRace.setHashValuesForMarkPassingCalculation(hashValues);
    }

    public int calculateHashForCompetitor(Competitor c) {
        int res = 0;
        try {
            res = res ^ c.getId().hashCode();
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for competitor " + c + " failed: " + e);
        }
        return res;
    }

    public int calculateHashForBoat(Boat b) {
        int res = 0;
        try {
            res = res ^ b.getBoatClass().getName().hashCode();
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for competitor " + b + " failed: " + e);
        }
        return res;
    }

    public int calculateHashForStart() {
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

    public int calculateHashForEnd() {
        int res = 0;
        try {
            res = res ^ calculateHashForTimePoint(trackedRace.getEndOfTracking());
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for end of Tracking failed: " + e);
        }
        return res;
    }

    public int calculateHashForNumberOfGPSFixes() {
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

    public int calculateHashForGPSFixes() {
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

    public int calculateHashForTimePoint(TimePoint tp) {
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

    public int calculateHashForPosition(Position p) {
        int res = 0;
        res = res ^ (int) p.getLatDeg();
        res = res ^ (int) p.getLngDeg();
        res = (res << 5) - res;
        return res;
    }

    public int calculateHashForWaypoints() {
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