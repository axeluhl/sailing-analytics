package com.sap.sailing.domain.tracking.impl;

import java.util.UUID;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sse.common.TimePoint;

public class HashCalculationForTrackedRaceHashImpl {
    private static final Logger logger = Logger.getLogger(HashCalculationForTrackedRaceHashImpl.class.getName());
    
    private final TrackedRaceImpl trackedRace;
    private final Iterable<Waypoint> waypoints;
    
    
    public HashCalculationForTrackedRaceHashImpl (TrackedRaceImpl trackedRace) {
        this.trackedRace = trackedRace;
        this.waypoints = trackedRace.getRace().getCourse().getWaypoints();
    }
    
    public int calculateHashForCompetitor (Competitor c) {
        int res = 0; 
        try {
            res = res ^ c.getId().hashCode();
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for competitor " + c + " failed: " + e);
        }
        return res;
    }
    
    public int calculateHashForBoat (Boat b) {
        int res = 0; 
        try {
            res = res ^ b.getBoatClass().getName().hashCode();
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for competitor " + b + " failed: " + e);
        }
        return res; 
    }
    
    public int calculateHashForStart () {
        int res = 0;
        try {
        res = res ^ calculateHashForTimePoint(trackedRace.getStartOfTracking());
        res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("An error occured when getting the start of tracking: " + e);
        }
        try {
            //Maybe that could be solved by using .getStartOfRace(boolean inferred)
            if( trackedRace.getTrackedRegatta().getRegatta().useStartTimeInference() == false) {
                res = res ^ calculateHashForTimePoint(trackedRace.getStartOfRace());
                res = (res << 5) - res;
            } 
        } catch (Exception e) {
            logger.info("An error occured when getting the starttime: " + e);
        }
        return res;
    }
    
    public int calculateHashForEnd () {
        int res = 0; 
        try {
        res = res ^ calculateHashForTimePoint(trackedRace.getEndOfTracking());
        res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for end of Tracking failed: " + e);
        }
        return res;
    }
    
    public int calculateHashForNumberOfGPSFixes () {
        int count = 0;
        for (Waypoint w : waypoints) {
            for (Mark m : w.getMarks()) {
                Iterable<GPSFix> gpsTrack = null;
                try {
                    trackedRace.getTrack(m).lockForRead();
                    gpsTrack = trackedRace.getTrack(m).getFixes();
                    for (GPSFix gf : gpsTrack) {
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
    
    
    public int calculateHashForGPSFixes () {
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
    
    public int calculateHashForTimePoint (TimePoint tp) {
        int res = 0; 
        //How problematic is this cast? 
        try {
            res = res ^ (int) tp.asMillis();
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Ther");
        }
         
        return res; 
    }
    
    public int calculateHashForPosition (Position p) {
        int res = 0;
            res = res ^ (int) p.getLatDeg();
            res = res ^ (int) p.getLngDeg();
            res = (res << 5) - res;
        return res;
    }
    
    public int calculateHashForWaypoints () {
        int res = 0;
        for(Waypoint p : waypoints) {
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
