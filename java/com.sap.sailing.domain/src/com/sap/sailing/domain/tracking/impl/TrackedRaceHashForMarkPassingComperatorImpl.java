package com.sap.sailing.domain.tracking.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.tracking.TrackedRaceHashForMarkPassingComperator;
import com.sap.sse.common.TimePoint;

public class TrackedRaceHashForMarkPassingComperatorImpl implements TrackedRaceHashForMarkPassingComperator{

    public enum typeOfHash {
        COMPETITOR,
        BOAT,
        START,
        END,
        WAYPOINTS,
        NUMBEROFGPSFIXES,
        GPSFIXES
      }
    
    private static final Logger logger = Logger.getLogger(TrackedRaceHashForMarkPassingComperatorImpl.class.getName());
    
    private TrackedRaceImpl trackedRace;
    private final Iterable<Waypoint> waypoints;
    private Map<typeOfHash, Integer> hashValues;
    
    public TrackedRaceHashForMarkPassingComperatorImpl (TrackedRaceImpl trackedRace) {
        this.trackedRace = trackedRace;
        this.waypoints = trackedRace.getRace().getCourse().getWaypoints();
        this.hashValues = new HashMap<>();
    }
    
    public void CalculateHash() {
        int competitorHash = 0;
        int boatHash = 0;
        hashValues.put(typeOfHash.WAYPOINTS, CalculateHashForWaypoints());
        for (Competitor c : trackedRace.getRace().getCompetitors()) {
            competitorHash = competitorHash + CalculateHashForCompetitor(c);
            boatHash = boatHash + CalculateHashForBoat(trackedRace.getRace().getBoatOfCompetitor(c));
        }
        hashValues.put(typeOfHash.COMPETITOR, competitorHash);
        hashValues.put(typeOfHash.BOAT, boatHash);
        hashValues.put(typeOfHash.START, CalculateHashForStart());
        hashValues.put(typeOfHash.END, CalculateHashForEnd());
        
        hashValues.put(typeOfHash.NUMBEROFGPSFIXES, CalculateHashForNumberOfGPSFixes());
        hashValues.put(typeOfHash.GPSFIXES, CalculateHashForGPSFixes());
        
        trackedRace.setHashValuesForMarkPassingCalculation(hashValues);
    }
    
    public int CalculateHashForCompetitor (Competitor c) {
        int res = 0; 
        try {
            res = res ^ c.getId().hashCode();
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for competitor " + c + " failed: " + e);
        }
        return res;
    }
    
    public int CalculateHashForBoat (Boat b) {
        int res = 0; 
        try {
            res = res ^ b.getBoatClass().getName().hashCode();
            res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for competitor " + b + " failed: " + e);
        }
        return res; 
    }
    
    public int CalculateHashForStart () {
        int res = 0;
        try {
        res = res ^ CalculateHashForTimePoint(trackedRace.getStartOfTracking());
        res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("An error occured when getting the start of tracking: " + e);
        }
        try {
            //Maybe that could be solved by using .getStartOfRace(boolean inferred)
            if( trackedRace.getTrackedRegatta().getRegatta().useStartTimeInference() == false) {
                res = res ^ CalculateHashForTimePoint(trackedRace.getStartOfRace());
                res = (res << 5) - res;
            } 
        } catch (Exception e) {
            logger.info("An error occured when getting the starttime: " + e);
        }
        return res;
    }
    
    public int CalculateHashForEnd () {
        int res = 0; 
        try {
        res = res ^ CalculateHashForTimePoint(trackedRace.getEndOfTracking());
        res = (res << 5) - res;
        } catch (Exception e) {
            logger.info("Hash calculation for end of Tracking failed: " + e);
        }
        return res;
    }
    
    public int CalculateHashForNumberOfGPSFixes () {
        int count = 0;
        for (Waypoint w : waypoints) {
            for (Mark m : w.getMarks()) {
                Iterable<GPSFix> gpsTrack = null;
                try {
                    trackedRace.getTrack(m).lockForRead();
                    gpsTrack = trackedRace.getTrack(m).getFixes();
                    //Unused warning since we need the individual Fixes only to count
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
    
    
    public int CalculateHashForGPSFixes () {
        int res = 0;
        for (Waypoint w : waypoints) {
            for (Mark m : w.getMarks()) {
                Iterable<GPSFix> gpsTrack = null;
                try {
                    trackedRace.getTrack(m).lockForRead();
                    gpsTrack = trackedRace.getTrack(m).getFixes();
                    for (GPSFix gf : gpsTrack) {
                        res = res ^ CalculateHashForTimePoint(gf.getTimePoint());
                        res = (res << 5) - res;
                        res = res ^ CalculateHashForPosition(gf.getPosition());
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
    
    public int CalculateHashForTimePoint (TimePoint tp) {
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
    
    public int CalculateHashForPosition (Position p) {
        int res = 0;
            res = res ^ (int) p.getLatDeg();
            res = res ^ (int) p.getLngDeg();
            res = (res << 5) - res;
        return res;
    }
    
    public int CalculateHashForWaypoints () {
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
