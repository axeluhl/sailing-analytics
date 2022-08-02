package com.sap.sailing.domain.markpassinghash.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.markpassinghash.TrackedRaceHashForMaskPassingCalculationFactory;
import com.sap.sailing.domain.tracking.impl.TrackedRaceImpl;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class TrackedRaceHashForMarkPassingCalculationFactoryImpl
        implements TrackedRaceHashForMaskPassingCalculationFactory {

    private static final Logger logger = Logger.getLogger(TrackedRaceHashForMarkPassingComparatorImpl.class.getName());

    @Override
    public TrackedRaceHashFingerprintImpl create(TrackedRaceImpl trackedRace) {
        int hashForCompetitors = 0;
        int hashForWaypoints = calculateHashForWaypoints(trackedRace);
        for (Competitor c : trackedRace.getRace().getCompetitors()) {
            hashForCompetitors = hashForCompetitors + calculateHashForCompetitor(c);
        }
        int hashForStart = calculateHashForStart(trackedRace);
        int hashForEnd = calculateHashForEnd(trackedRace);
        int hashForNumberOfGPSFixes = calculateHashForNumberOfGPSFixes(trackedRace);
        int hashForGPSFixes = calculateHashForGPSFixes(trackedRace);

        return new TrackedRaceHashFingerprintImpl(hashForWaypoints, hashForCompetitors, hashForStart, hashForEnd,
                hashForNumberOfGPSFixes, hashForGPSFixes);
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

    private int calculateHashForStart(TrackedRaceImpl trackedRace) {
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

    private int calculateHashForEnd(TrackedRaceImpl trackedRace) {
        int res = 0;
        try {
            res = res ^ calculateHashForTimePoint(trackedRace.getEndOfTracking());
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for end of Tracking failed: " + e);
        }
        return res;
    }

    private int calculateHashForNumberOfGPSFixes(TrackedRaceImpl trackedRace) {
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

    private int calculateHashForGPSFixes(TrackedRaceImpl trackedRace) {
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

    private int calculateHashForWaypoints(TrackedRaceImpl trackedRace) {
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
