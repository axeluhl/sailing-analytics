package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.NoFixesException;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;

public class IncrementalManeuverDetectorImpl extends ManeuverDetectorImpl {
    
    private Map<GPSFixMoving, Iterable<GPSFixMoving>> douglasPeuckerFixesGroupes = new HashMap<>();

    public IncrementalManeuverDetectorImpl(TrackedRace trackedRace) {
        super(trackedRace);
    }

    @Override
    public List<Maneuver> detectManeuvers(Competitor competitor) throws NoWindException, NoFixesException {
        //TODO generate ManeuverSpots, check the unused ignoreMarkPassings flag
        Pair<TimePoint,TimePoint> startAndEndTimePoints = getTrackingStartAndEndTimePoints(competitor);
        if(startAndEndTimePoints != null) {
            TimePoint earliestManeuverStart = startAndEndTimePoints.getA();
            TimePoint latestManeuverEnd = startAndEndTimePoints.getB();
            Iterable<GPSFixMoving> douglasPeuckerFixes = trackedRace.approximate(competitor,
                    competitor.getBoat().getBoatClass().getMaximumDistanceForCourseApproximation(), earliestManeuverStart, latestManeuverEnd);
            if(douglasPeuckerFixesGroupes == null) {
                return detectManeuvers(competitor, douglasPeuckerFixes, earliestManeuverStart, latestManeuverEnd, false);
            } else {
                List<Maneuver> result = new ArrayList<>();
                ManeuverSpot previouslyDetectedManeuverSpotWithSameBeginning = null;
                List<GPSFixMoving> iteratedButNotProcessedNewDouglasPeuckerFixes = new ArrayList<>();
                boolean skipNextCallOfNewDouglasPeuckerFixesIterator = false;
                GPSFixMoving newDouglasPeuckerFix = null;
                for (Iterator<GPSFixMoving> newDouglasPeuckerFixesIterator = douglasPeuckerFixes.iterator(); newDouglasPeuckerFixesIterator.hasNext();) {
                    if(skipNextCallOfNewDouglasPeuckerFixesIterator) {
                        skipNextCallOfNewDouglasPeuckerFixesIterator = false;
                    } else {
                        newDouglasPeuckerFix = newDouglasPeuckerFixesIterator.next();
                        iteratedButNotProcessedNewDouglasPeuckerFixes.add(newDouglasPeuckerFix);
                    }
                    ManeuverSpot maneuverSpot = getExistingManeuverSpotByFirstDouglasPeuckerFix(newDouglasPeuckerFix);
                    if(maneuverSpot != null) {
                        boolean douglasPeuckerFixesGroupIsNearlySame = true;
                        if(previouslyDetectedManeuverSpotWithSameBeginning != null) {
                            for (Maneuver maneuver : previouslyDetectedManeuverSpotWithSameBeginning.getManeuvers()) {
                                result.add(maneuver);
                            }
                            previouslyDetectedManeuverSpotWithSameBeginning = null;
                        } else if(iteratedButNotProcessedNewDouglasPeuckerFixes.size() > 1) {
                            if(checkWhetherDouglasPeuckerFixesCanBeGroupedTogether(iteratedButNotProcessedNewDouglasPeuckerFixes.get(iteratedButNotProcessedNewDouglasPeuckerFixes.size() - 2), newDouglasPeuckerFix)) {
                                douglasPeuckerFixesGroupIsNearlySame = false;
                            } else {
                                iteratedButNotProcessedNewDouglasPeuckerFixes.remove(iteratedButNotProcessedNewDouglasPeuckerFixes.size() - 1);
                                result.addAll(detectManeuvers(competitor, iteratedButNotProcessedNewDouglasPeuckerFixes, earliestManeuverStart, latestManeuverEnd, false));
                                iteratedButNotProcessedNewDouglasPeuckerFixes.clear();
                                iteratedButNotProcessedNewDouglasPeuckerFixes.add(newDouglasPeuckerFix);
                            }
                        }
                        
                        if(douglasPeuckerFixesGroupIsNearlySame) {
                            boolean firstLoop = true;
                            for (Iterator<GPSFixMoving> existingDouglasPeuckerFixesGroupIterator = maneuverSpot.getDouglasPeuckerFixes().iterator(); existingDouglasPeuckerFixesGroupIterator.hasNext();) {
                                if(firstLoop) {
                                    firstLoop = false;
                                } else {
                                    GPSFixMoving existingDouglasPeuckerFix = existingDouglasPeuckerFixesGroupIterator.next();
                                    if(!checkDouglasPeuckerFixesForBeingNearlySame(existingDouglasPeuckerFix, newDouglasPeuckerFix)) {
                                        douglasPeuckerFixesGroupIsNearlySame = false;
                                        skipNextCallOfNewDouglasPeuckerFixesIterator = true;
                                        break;
                                    }
                                }
                                if(existingDouglasPeuckerFixesGroupIterator.hasNext()) {
                                    if(newDouglasPeuckerFixesIterator.hasNext()) {
                                        newDouglasPeuckerFix = newDouglasPeuckerFixesIterator.next();
                                        iteratedButNotProcessedNewDouglasPeuckerFixes.add(newDouglasPeuckerFix);
                                    } else {
                                        douglasPeuckerFixesGroupIsNearlySame = false;
                                        break;
                                    }
                                }
                            }
                            if(douglasPeuckerFixesGroupIsNearlySame && !checkManeuverSpotWindsNearlySame(maneuverSpot)) {
                                douglasPeuckerFixesGroupIsNearlySame = false;
                            }
                            if(douglasPeuckerFixesGroupIsNearlySame) {
                                previouslyDetectedManeuverSpotWithSameBeginning = maneuverSpot;
                                iteratedButNotProcessedNewDouglasPeuckerFixes.clear();
                            }
                        }
                    } else if(previouslyDetectedManeuverSpotWithSameBeginning != null) {
                        iteratedButNotProcessedNewDouglasPeuckerFixes.clear();
                        for (GPSFixMoving fix : previouslyDetectedManeuverSpotWithSameBeginning.getDouglasPeuckerFixes()) {
                            iteratedButNotProcessedNewDouglasPeuckerFixes.add(fix);
                        }
                        iteratedButNotProcessedNewDouglasPeuckerFixes.add(newDouglasPeuckerFix);
                    }
                }
                if(previouslyDetectedManeuverSpotWithSameBeginning != null) {
                    for (Maneuver maneuver : previouslyDetectedManeuverSpotWithSameBeginning.getManeuvers()) {
                        result.add(maneuver);
                    }
                } else if(!iteratedButNotProcessedNewDouglasPeuckerFixes.isEmpty()) {
                    result.addAll(detectManeuvers(competitor, iteratedButNotProcessedNewDouglasPeuckerFixes, earliestManeuverStart, latestManeuverEnd, false));
                }
            }
        }
        throw new NoFixesException();
    }

    private boolean checkWhetherDouglasPeuckerFixesCanBeGroupedTogether(GPSFixMoving gpsFixMoving,
            GPSFixMoving newDouglasPeuckerFix) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean checkManeuverSpotWindsNearlySame(ManeuverSpot maneuverSpot) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean checkDouglasPeuckerFixesForBeingNearlySame(GPSFixMoving existingDouglasPeuckerFix,
            GPSFixMoving newDouglasPeuckerFix) {
        // TODO Auto-generated method stub
        return false;
    }

    private ManeuverSpot getExistingManeuverSpotByFirstDouglasPeuckerFix(GPSFixMoving newDouglasPeuckerFix) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
