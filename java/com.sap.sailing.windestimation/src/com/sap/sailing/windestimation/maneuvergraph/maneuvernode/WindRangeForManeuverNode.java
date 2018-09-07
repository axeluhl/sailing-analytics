package com.sap.sailing.windestimation.maneuvergraph.maneuvernode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sap.sailing.domain.common.BearingChangeAnalyzer;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.windestimation.data.CoarseGrainedManeuverType;
import com.sap.sailing.windestimation.data.CoarseGrainedPointOfSail;
import com.sap.sailing.windestimation.data.FineGrainedManeuverType;
import com.sap.sailing.windestimation.data.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.impl.DegreeBearingImpl;

public class WindRangeForManeuverNode {

    private final double fromPortside;
    private final double angleTowardStarboard;

    public WindRangeForManeuverNode(double fromPortside, double angleTowardStarboard) {
        this.fromPortside = fromPortside;
        this.angleTowardStarboard = angleTowardStarboard;
    }

    public double getFromPortside() {
        return fromPortside;
    }

    public double getAngleTowardStarboard() {
        return angleTowardStarboard;
    }

    public double getAvgWindCourse() {
        double avgWindCourse = fromPortside + angleTowardStarboard / 2.0;
        if (avgWindCourse > 360) {
            avgWindCourse -= 360;
        } else if (avgWindCourse < 0) {
            avgWindCourse += 360;
        }
        return avgWindCourse;
    }

    public IntersectedWindRange toIntersected() {
        return new IntersectedWindRange(fromPortside, angleTowardStarboard, 0);
    }

    public IntersectedWindRange intersect(WindRangeForManeuverNode nextWindRange) {
        double deviationFromPortsideBoundaryTowardStarboard = nextWindRange.fromPortside - fromPortside;
        if (deviationFromPortsideBoundaryTowardStarboard < 0) {
            deviationFromPortsideBoundaryTowardStarboard += 360;
        }
        double deviationFromPortsideTowardStarboardInDegrees = deviationFromPortsideBoundaryTowardStarboard
                - angleTowardStarboard;
        double newFromPortside;
        double newAngleTowardStarboard;
        double violationRange;
        if (deviationFromPortsideTowardStarboardInDegrees <= 0) {
            // other.fromPortside is within the range
            newFromPortside = nextWindRange.fromPortside;
            violationRange = 0;
            if (deviationFromPortsideTowardStarboardInDegrees + nextWindRange.angleTowardStarboard < 0) {
                newAngleTowardStarboard = nextWindRange.angleTowardStarboard;
            } else {
                newAngleTowardStarboard = Math.abs(deviationFromPortsideTowardStarboardInDegrees);
            }
        } else {
            double deviationFromPortsideBoundaryTowardPortside = 360 - deviationFromPortsideBoundaryTowardStarboard;
            double deviationFromPortsideTowardPortsideInDegrees = deviationFromPortsideBoundaryTowardPortside
                    - nextWindRange.angleTowardStarboard;
            if (deviationFromPortsideTowardPortsideInDegrees <= 0) {
                // fromPortside is within the other range
                newFromPortside = fromPortside;
                violationRange = 0;
                if (deviationFromPortsideTowardPortsideInDegrees + angleTowardStarboard < 0) {
                    newAngleTowardStarboard = angleTowardStarboard;
                } else {
                    newAngleTowardStarboard = Math.abs(deviationFromPortsideTowardPortsideInDegrees);
                }
            } else {
                newFromPortside = nextWindRange.fromPortside;
                newAngleTowardStarboard = nextWindRange.angleTowardStarboard;
                if (deviationFromPortsideTowardStarboardInDegrees < deviationFromPortsideTowardPortsideInDegrees) {
                    // newFromPortside = nextWindRange.angleTowardStarboard -
                    // deviationFromPortsideTowardStarboardInDegrees;
                    // newAngleTowardStarboard = deviationFromPortsideTowardStarboardInDegrees;
                    violationRange = newAngleTowardStarboard;
                } else {
                    // newFromPortside = angleTowardStarboard - deviationFromPortsideTowardPortsideInDegrees;
                    // newAngleTowardStarboard = deviationFromPortsideTowardPortsideInDegrees;
                    violationRange = newAngleTowardStarboard;
                }
            }
        }
        return new IntersectedWindRange(newFromPortside, newAngleTowardStarboard, violationRange);
    }

    public boolean isWindCourseWithinRange(double windCourseInDegrees) {
        double deviationFromPortsideBoundaryTowardStarboard = windCourseInDegrees - fromPortside;
        if (deviationFromPortsideBoundaryTowardStarboard < 0) {
            deviationFromPortsideBoundaryTowardStarboard += 360;
        }
        double deviationFromPortsideTowardStarboardInDegrees = deviationFromPortsideBoundaryTowardStarboard
                - angleTowardStarboard;
        if (deviationFromPortsideTowardStarboardInDegrees <= 0) {
            return true;
        }
        return false;
    }

    public List<FineGrainedPointOfSail> getPossiblePointOfSails(double boatCourseInDegrees, Tack tackAfter) {
        Tack targetTack = tackAfter;
        if (targetTack == null) {
            Bearing windFrom = new DegreeBearingImpl(fromPortside);
            Bearing windTo = new DegreeBearingImpl((fromPortside + angleTowardStarboard) % 360);
            Bearing twaFrom = windFrom.reverse().getDifferenceTo(new DegreeBearingImpl(boatCourseInDegrees));
            Bearing twaTo = windTo.reverse().getDifferenceTo(new DegreeBearingImpl(boatCourseInDegrees));
            if (twaFrom.getDegrees() * twaTo.getDegrees() >= 0) {
                // sign is equal
                targetTack = twaFrom.getDegrees() < 0 ? Tack.PORT : Tack.STARBOARD;
            }
        }
        List<FineGrainedPointOfSail> result = new ArrayList<>();
        for (FineGrainedPointOfSail pointOfSail : FineGrainedPointOfSail.values()) {
            if (tackAfter == null || pointOfSail.getTack() == tackAfter) {
                double windCourse = pointOfSail.getWindCourse(boatCourseInDegrees);
                if (isWindCourseWithinRange(windCourse)) {
                    result.add(pointOfSail);
                }
            }
        }
        return result;
    }

    public List<FineGrainedPointOfSail> getBestSuitablePointOfSails(ManeuverNode maneuverNode,
            ManeuverForEstimation maneuver, Bearing course) {
        List<FineGrainedPointOfSail> pointOfSailsManeuver = getPossiblePointOfSails(course.getDegrees(),
                maneuverNode.getTackAfter());
        Set<CoarseGrainedPointOfSail> coarseGrainedPointOfSails = pointOfSailsManeuver.stream()
                .map(pointOfSail -> pointOfSail.getCoarseGrainedPointOfSail()).collect(Collectors.toSet());
        if (coarseGrainedPointOfSails.size() == 1) {
            return pointOfSailsManeuver;
        } else {
            return Collections.emptyList();
        }
    }

    public List<FineGrainedManeuverType> getBestSuitableManeuverTypes(ManeuverNode maneuverNode,
            ManeuverForEstimation maneuver) {
        List<FineGrainedPointOfSail> possiblePointOfSails = getPossiblePointOfSails(
                maneuver.getSpeedWithBearingAfter().getBearing().getDegrees(), maneuverNode.getTackAfter());
        List<FineGrainedManeuverType> maneuverTypes = new ArrayList<>();
        List<CoarseGrainedManeuverType> coarseGrainedManeuverTypes = new ArrayList<>();
        for (FineGrainedPointOfSail pointOfSailAfter : possiblePointOfSails) {
            FineGrainedManeuverType maneuverType = getTypeOfCleanManeuver(maneuver, pointOfSailAfter);
            maneuverTypes.add(maneuverType);
            coarseGrainedManeuverTypes.add(maneuverType.getCoarseGrainedManeuverType());
        }
        if (coarseGrainedManeuverTypes.size() == 1) {
            return maneuverTypes;
        }
        return Collections.emptyList();
    }

    public FineGrainedManeuverType getTypeOfCleanManeuver(ManeuverForEstimation maneuver,
            FineGrainedPointOfSail pointOfSailAfterManeuver) {
        Bearing windCourse = new DegreeBearingImpl(
                pointOfSailAfterManeuver.getWindCourse(maneuver.getSpeedWithBearingAfter().getBearing().getDegrees()));
        BearingChangeAnalyzer bearingChangeAnalyzer = BearingChangeAnalyzer.INSTANCE;
        double directionChangeInDegrees = maneuver.getCourseChangeInDegrees();
        int numberOfTacks = bearingChangeAnalyzer.didPass(maneuver.getSpeedWithBearingBefore().getBearing(),
                directionChangeInDegrees, maneuver.getSpeedWithBearingAfter().getBearing(), windCourse.reverse());
        int numberOfJibes = bearingChangeAnalyzer.didPass(maneuver.getSpeedWithBearingBefore().getBearing(),
                directionChangeInDegrees, maneuver.getSpeedWithBearingAfter().getBearing(), windCourse);
        if (numberOfTacks > 0 && numberOfJibes > 0) {
            return FineGrainedManeuverType._360;
        }
        if (numberOfTacks > 0 || numberOfJibes > 0) {
            if (Math.abs(directionChangeInDegrees) > 120) {
                return numberOfTacks > 1 ? FineGrainedManeuverType._180_TACK : FineGrainedManeuverType._180_JIBE;
            }
            return numberOfTacks > 0 ? FineGrainedManeuverType.TACK : FineGrainedManeuverType.JIBE;
        }
        boolean bearAway = pointOfSailAfterManeuver.getTack() == Tack.STARBOARD && directionChangeInDegrees > 0
                || pointOfSailAfterManeuver.getTack() == Tack.PORT && directionChangeInDegrees < 0;
        LegType legTypeBeforeManeuver = pointOfSailAfterManeuver
                .getNextPointOfSail(maneuver.getCourseChangeInDegrees() * -1).getLegType();
        LegType legTypeAfterManeuver = pointOfSailAfterManeuver.getLegType();
        if (bearAway) {
            switch (legTypeBeforeManeuver) {
            case UPWIND:
                switch (legTypeAfterManeuver) {
                case UPWIND:
                    return FineGrainedManeuverType.BEAR_AWAY_AT_UPWIND;
                case REACHING:
                    return FineGrainedManeuverType.BEAR_AWAY_FROM_UPWIND_UNTIL_REACHING;
                case DOWNWIND:
                    return FineGrainedManeuverType.BEAR_AWAY_FROM_UPWIND_UNTIL_DOWNWIND;
                }
            case REACHING:
                return legTypeAfterManeuver == LegType.DOWNWIND
                        ? FineGrainedManeuverType.BEAR_AWAY_FROM_REACHING_UNTIL_DOWNWIND
                        : FineGrainedManeuverType.BEAR_AWAY_AT_REACHING;
            case DOWNWIND:
                return FineGrainedManeuverType.BEAR_AWAY_AT_DOWNWIND;
            }
        } else {
            switch (legTypeBeforeManeuver) {
            case DOWNWIND:
                switch (legTypeAfterManeuver) {
                case DOWNWIND:
                    return FineGrainedManeuverType.HEAD_UP_AT_DOWNWIND;
                case REACHING:
                    return FineGrainedManeuverType.HEAD_UP_FROM_DOWNWIND_UNTIL_REACHING;
                case UPWIND:
                    return FineGrainedManeuverType.HEAD_UP_FROM_DOWNWIND_UNTIL_UPWIND;
                }
            case REACHING:
                return legTypeAfterManeuver == LegType.UPWIND
                        ? FineGrainedManeuverType.HEAD_UP_FROM_REACHING_UNTIL_UPWIND
                        : FineGrainedManeuverType.HEAD_UP_AT_REACHING;
            case UPWIND:
                return FineGrainedManeuverType.HEAD_UP_AT_UPWIND;
            }
        }
        throw new IllegalStateException();
    }

}
