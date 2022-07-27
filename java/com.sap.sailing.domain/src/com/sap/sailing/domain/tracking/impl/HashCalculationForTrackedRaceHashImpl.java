package com.sap.sailing.domain.tracking.impl;

import java.util.NavigableSet;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sse.common.TimePoint;

public class HashCalculationForTrackedRaceHashImpl {
    private final TrackedRaceImpl trackedRace;
    private static final Logger logger = Logger.getLogger(HashCalculationForTrackedRaceHashImpl.class.getName());
    
    public HashCalculationForTrackedRaceHashImpl (TrackedRaceImpl trackedRace) {
        this.trackedRace = trackedRace;
    }
    
    public int calculateHashForWaypoints (Iterable<Waypoint> waypoints) {
        int res = 0;
        for(Waypoint w : waypoints) {
            res = res ^ w.getName().hashCode();
            // We could also include further information, such as marks, but it would slow down the computation
        }
        res = res * 31;
        return res;
    }
    
    
    
    public int calculateHashForMarkpassings (NavigableSet<MarkPassing> markpassings) {
        int res = 0; 
        for(MarkPassing m : markpassings) {
            res = res ^ calculateHashForWaypoint(m.getWaypoint());
            //Didn't include competitor and original Markpassing, since I think that they aren't Important and consume calculating power
        }
        return res;
    }
    
    public int calculateHashForWaypoint (Waypoint w) {
        int res = 0;
        res = res ^ w.getName().hashCode();
        res = res * 31;
            // We could also include further information, such as marks, but it would slow down the computation
        return res;
    }
    
    public int calculateHashForBoat (Boat b) {
        int res = 0; 
        res = res ^ b.getSailID().hashCode();
        //res = res ^ b.getName().hashCode(); -> null pointer 
        res = res * 31;
        return res; 
    }
    
    public int calculateHashForTimePoint (TimePoint tp) {
        int res = 0; 
        //How problematic is this cast? 
        res = res ^ (int) tp.asMillis();
        return res; 
    }
    
    public int calculateHashForCompetitor (Competitor c) {
        int res = 0; 
        res = res ^ c.getName().hashCode();
        //res = res ^ c.getId().
        res = res  ^c.getShortInfo().hashCode();
        return res;
    }
    
    public int calculateHashForGPS (GPSFix gf) {
       int res = 0;
           res = res ^ calculateHashForTimePoint(gf.getTimePoint());
           res = res ^ calculateHashForPosition(gf.getPosition());
           res = res * 31;
       return res;
    }
    
    public int calculateHashForWind (Wind w) {
        int res = 0;
//        try {
//            res = res ^ (int)Math.round(w.getFrom().getDegrees());
//            res = res * 31;
//        } catch (Exception e) {
//            logger.info("Hash calculation for wind degrees failed: " + e);
//        }
        
        res = res ^ (int)Math.round(w.getKilometersPerHour());
        res = res * 31;
        return res;
    }
    
    public int calculateHashForPosition (Position p) {
        int res = 0;
            res = res ^ (int) p.getLatDeg();
            res = res ^ (int) p.getLngDeg();
            res = res * 31;
        return res;
    }
    
    public int calculateHashForStart () {
        int res = 0;
        try {
            res = res ^ calculateHashForTimePoint(trackedRace.getStartOfRace());
            res = res ^ calculateHashForTimePoint(trackedRace.getStartOfTracking());
            res = res * 31;
            } catch (Exception e) {
                logger.info("Hash calculation for race start failed: " + e);
            }
        
        return res;
    }
    
    public int calculateHashForEnd () {
        int res = 0; 
        try {
        res = res ^ calculateHashForTimePoint(trackedRace.getEndOfRace());
        res = res ^ calculateHashForTimePoint(trackedRace.getEndOfTracking());
        res = res * 31;
        } catch (Exception e) {
            logger.info("Hash calculation for race end failed");
        }
       
        return res;
    }
    
    public int calculateHashForFinish () {
        int res = 0;
        try {
            res = res ^ calculateHashForTimePoint(trackedRace.getFinishedTime());
            res = res ^ calculateHashForTimePoint(trackedRace.getFinishingTime());
            res = res * 31;
            } catch (Exception e) {
                logger.info("Hash calculation for race finish failed: " + e);
            }
        return res; 
    }
    
//    public int calculateHashForFinish (Position p, TimePoint tp) {
//        int res = 0;
//        logger.info(trackedRace.getWind(p, tp).toString());
//        return res;
//    }
}
