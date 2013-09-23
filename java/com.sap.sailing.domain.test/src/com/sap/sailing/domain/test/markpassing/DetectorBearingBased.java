package com.sap.sailing.domain.test.markpassing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.impl.MarkPassingImpl;

public class DetectorBearingBased implements DetectorMarkPassing {

    @Override
    public LinkedHashMap<Waypoint, MarkPassing> computeMarkpasses(
            DynamicGPSFixTrack<Competitor, GPSFixMoving> gpsFixes,
            LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> markTracks, TimePoint raceStart) {

        LinkedHashMap<Waypoint, MarkPassing> computedMarkPasses = new LinkedHashMap<Waypoint, MarkPassing>();

        for (Waypoint w : markTracks.keySet()) {

            if (!markTracks.keySet().iterator().hasNext()// or start line!!)
            )

                if (markTracks.get(w).size() == 1) {

                    LinkedList<GPSFixMoving> possibleMarkPasses = new LinkedList<GPSFixMoving>();

                    for (GPSFixMoving gpsFix : gpsFixes.getFixes()) {

                        Distance distance = distanceToSingleWayPoint(markTracks, gpsFix, w);

                        if (distance.getMeters() > 100) {

                            possibleMarkPasses.add(gpsFix);

                        }
                    }
                    for (GPSFixMoving possibleGPSFix : possibleMarkPasses) {

                        if (possibleGPSFix.getTimePoint().before(raceStart)) {

                            possibleMarkPasses.remove(possibleGPSFix);
                        }

                    }

                    while (possibleMarkPasses.size() > 1) {

                        if (distanceToSingleWayPoint(markTracks, possibleMarkPasses.get(0), w).getMeters() < distanceToSingleWayPoint(
                                markTracks, possibleMarkPasses.get(1), w).getMeters()) {
                            possibleMarkPasses.remove(0);

                        } else {
                            possibleMarkPasses.remove(1);

                        }

                    }

                    MarkPassing m = new MarkPassingImpl(possibleMarkPasses.get(0).getTimePoint(), w,
                            gpsFixes.getTrackedItem());
                    computedMarkPasses.put(w, m);

                }
            // if gate!!

        }
        return computedMarkPasses;

    }

    private Distance distanceToSingleWayPoint(
            LinkedHashMap<Waypoint, ArrayList<DynamicGPSFixTrack<Mark, GPSFix>>> markTracks, GPSFixMoving gps,
            Waypoint w) {

        return gps.getPosition().getDistance(
                markTracks.get(w).iterator().next().getEstimatedPosition(gps.getTimePoint(), true));

    }

}
