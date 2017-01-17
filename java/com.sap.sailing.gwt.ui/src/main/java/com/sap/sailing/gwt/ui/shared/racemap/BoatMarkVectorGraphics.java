package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.Iterator;

import com.google.gwt.canvas.dom.client.Context2d;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.gwt.ui.shared.CoursePositionsDTO;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;

public class BoatMarkVectorGraphics extends AbstractMarkVectorGraphics {

    private final static Distance BOAT_MARK_HEIGHT_IN_METERS = new MeterDistance(6.2);
    private final static Distance BOAT_MARK_WIDTH_IN_METERS = new MeterDistance(3.5);
    private final static double BOAT_MARK_SELECTION_SCALE = 3.5;
    private final static double BOAT_MARK_SELECTION_TRANSLATE_X = 50;
    private final static double BOAT_MARK_SELECTION_TRANSLATE_Y = -130;
    private final String markIdAsString;

    public BoatMarkVectorGraphics(MarkType type, String color, String shape, String pattern, String markIdAsString) {
        super(type, color, shape, pattern);
        this.anchorPointX = BOAT_MARK_HEIGHT_IN_METERS.getMeters() / 2;
        this.anchorPointY = BOAT_MARK_WIDTH_IN_METERS.getMeters() / 2;
        this.markHeightInMeters = BOAT_MARK_HEIGHT_IN_METERS;
        this.markWidthInMeters = BOAT_MARK_WIDTH_IN_METERS;
        this.markIdAsString = markIdAsString;
    }

    protected void setUpScaleAndTranslateForMarkSelection(Context2d ctx) {
        ctx.scale(BOAT_MARK_SELECTION_SCALE, BOAT_MARK_SELECTION_SCALE);
        ctx.translate(BOAT_MARK_SELECTION_TRANSLATE_X, BOAT_MARK_SELECTION_TRANSLATE_Y);
    }

    @Override
    protected void drawMarkBody(Context2d ctx, boolean isSelected, String color) {
        ctx.beginPath();
        ctx.setFillStyle("#000000");
        ctx.setStrokeStyle("#FFFFFF");
        ctx.setLineWidth(30.0);
        ctx.setLineCap("butt");
        ctx.setLineJoin("miter");
        ctx.setMiterLimit(3.0);
        ctx.moveTo(33, 30.36);
        ctx.quadraticCurveTo(103.47, 18.96, 194.65, 18.55);
        ctx.quadraticCurveTo(285.83, 18.13, 370.62, 34.74);
        ctx.quadraticCurveTo(455.42, 51.34, 520.29, 87.99);
        ctx.quadraticCurveTo(585.16, 124.64, 603, 187.36);
        ctx.quadraticCurveTo(584.67, 244.84, 520.84, 277.54);
        ctx.quadraticCurveTo(457, 310.24, 372.97, 324.79);
        ctx.quadraticCurveTo(288.94, 339.34, 197.37, 339.08);
        ctx.quadraticCurveTo(105.8, 338.81, 32, 330.36);
        ctx.lineTo(33, 30.362205);
        ctx.fill();
        ctx.stroke();

        ctx.setFillStyle("#FFFFFF");
        ctx.beginPath();
        ctx.beginPath();
        ctx.setLineWidth(1.0);
        ctx.setLineJoin("miter");
        ctx.setMiterLimit(4.0);
        ctx.moveTo(81, 78);
        ctx.lineTo(349, 78);
        ctx.lineTo(349, 291);
        ctx.lineTo(81, 291);
        ctx.lineTo(81, 78);
        ctx.fill();
        ctx.stroke();

        ctx.setFillStyle("#000000");
        ctx.beginPath();
        ctx.moveTo(337, 186.36);
        ctx.quadraticCurveTo(337, 180.56, 334.76, 175.46);
        ctx.quadraticCurveTo(332.52, 170.36, 328.65, 166.56);
        ctx.quadraticCurveTo(324.78, 162.76, 319.59, 160.56);
        ctx.quadraticCurveTo(314.4, 158.36, 308.5, 158.36);
        ctx.quadraticCurveTo(302.6, 158.36, 297.41, 160.56);
        ctx.quadraticCurveTo(292.22, 162.76, 288.35, 166.56);
        ctx.quadraticCurveTo(284.48, 170.36, 282.24, 175.46);
        ctx.quadraticCurveTo(280, 180.56, 280, 186.36);
        ctx.quadraticCurveTo(280, 192.16, 282.24, 197.26);
        ctx.quadraticCurveTo(284.48, 202.36, 288.35, 206.16);
        ctx.quadraticCurveTo(292.22, 209.96, 297.41, 212.16);
        ctx.quadraticCurveTo(302.6, 214.36, 308.5, 214.36);
        ctx.quadraticCurveTo(314.4, 214.36, 319.59, 212.16);
        ctx.quadraticCurveTo(324.78, 209.96, 328.65, 206.16);
        ctx.quadraticCurveTo(332.52, 202.36, 334.76, 197.26);
        ctx.quadraticCurveTo(337, 192.16, 337, 186.36);
        ctx.stroke();
        ctx.fill();
        ctx.beginPath();
    }

    /**
     * A boat that acts as a mark which is part of a line needs to be drawn in some orientation. In real life the boat
     * would usually be anchoring, therefore displaying the bow to windward. Especially for reaching starts this is,
     * however, a somewhat unusual, unfortunate way of displaying the start line / start boat. At least for reaching
     * starts we therefore would rather like to rotate the boat such that if it is part of the start line its bow points
     * generally towards the next waypoint, with the keel oriented in a right angle to the start line. But downwind
     * starts are also possible, and there it may make the most sense to still generally point the bow to windward, keel
     * in right angle to start line or, if available, really aligned to the wind.
     * <p>
     * 
     * The {@link CoursePositionsDTO} object optionally contains wind information for
     * {@link CoursePositionsDTO#startLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind start} and
     * {@link CoursePositionsDTO#finishLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind finish} line. If these values are available then together
     * with the line orientation that we get from its marks' positions we can infer the combined wind direction and
     * rotate the boat mark accordingly for an upwind/downwind start/finish. In the absence of wind information we
     * default to the algorithm for a reaching start/finish.
     * <p>
     * 
     * Only for a reaching start/finish we'd like to draw the boat in a right angle to the line with the bow pointing in
     * the direction in which the line is being crossed, so generally to the next waypoint for the start line, and away
     * from the previous waypoint for the finish line. This can be computed using the cross-track error (XTE) off the
     * bearing from the boat mark to the pin end, as follows (this example for the start line): if the next waypoint has
     * a positive XTE (bearing "right" of the bearing from start boat to pin end), the start boat is on the starboard
     * side of the line and needs to be rotated 90deg clockwise from the bearing to the pin end; in case of a negative
     * XTE the start boat happens to be on the unusual port end of the line and needs to be rotated 90deg
     * counter-clockwise from the bearing to the pin end.
     * <p>
     */
    @Override
    public Bearing getRotationInDegrees(CoursePositionsDTO coursePositionsDTO) {
        MarkDTO firstMark = null;
        MarkDTO secondMark = null;
        boolean isStart = true;
        boolean isFinish;
        Position previousWaypointPosition = null;
        final Iterator<Position> waypointPosIter = coursePositionsDTO.waypointPositions.iterator();
        Position currentWaypointPosition = waypointPosIter.hasNext() ? waypointPosIter.next() : null;
        for (Iterator<WaypointDTO> waypointIter = coursePositionsDTO.course.waypoints.iterator(); waypointIter.hasNext(); ) {
            WaypointDTO currentWaypoint = waypointIter.next();
            Position nextWaypointPosition = waypointPosIter.hasNext() ? waypointPosIter.next() : null;
            isFinish = !waypointIter.hasNext();
            if (currentWaypoint.passingInstructions == PassingInstruction.Line) {
                Iterator<MarkDTO> marks = currentWaypoint.controlPoint.getMarks().iterator();
                firstMark = marks.next();
                if (marks.hasNext()) {
                    secondMark = marks.next();
                    if (markIdAsString.equals(firstMark.getIdAsString())) {
                        return getLineBearing(firstMark.position, secondMark.position, isStart,
                                coursePositionsDTO.startLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind, nextWaypointPosition,
                                isFinish, coursePositionsDTO.finishLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind,
                                previousWaypointPosition, /* boatIsFirstMark */ true);
                    } else if (markIdAsString.equals(secondMark.getIdAsString())) {
                        return getLineBearing(secondMark.position, firstMark.position, isStart,
                                coursePositionsDTO.startLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind, nextWaypointPosition,
                                isFinish, coursePositionsDTO.finishLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind,
                                previousWaypointPosition, /* boatIsFirstMark */ false);
                    }
                }
            }
            isStart = false;
            previousWaypointPosition = currentWaypointPosition;
            currentWaypointPosition = nextWaypointPosition;
        }
        return null;
    }
    
    private Bearing getLineBearing(Position boatMarkPosition, Position pinEndPosition, boolean isStart,
            Double startLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind, Position nextWaypointPosition, boolean isFinish,
            Double finishLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind, Position previousWaypointPosition, boolean boatIsFirstMark) {
        // by default, assume the boat is on the starboard side of the line
        final Bearing bearingFromBoatMarkToPinEnd = boatMarkPosition.getBearingGreatCircle(pinEndPosition);
        final Bearing result;
        if (isStart) {
            if (nextWaypointPosition == null) {
                result = null;
            } else {
                if (isReach(startLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind)) {
                    result = bearingOrReverseDependingOnXTEToOther(boatMarkPosition, bearingFromBoatMarkToPinEnd, nextWaypointPosition);
                } else {
                    final boolean pinEndOnPortWhenApproachingLine = pinEndPosition.crossTrackError(boatMarkPosition, boatMarkPosition.getBearingGreatCircle(nextWaypointPosition)).compareTo(Distance.NULL) < 0;
                    result = getWindFrom(startLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind,
                            bearingFromBoatMarkToPinEnd, pinEndOnPortWhenApproachingLine).add(new DegreeBearingImpl(-90) /* bow points east with rotation 0deg */);
                }
            }
        } else {
            if (previousWaypointPosition == null) {
                result = null;
            } else {
                if (isReach(finishLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind)) {
                    result = bearingOrReverseDependingOnXTEToOther(pinEndPosition, bearingFromBoatMarkToPinEnd.reverse(), previousWaypointPosition);
                } else {
                    final boolean pinEndOnPortWhenApproachingLine = pinEndPosition.crossTrackError(previousWaypointPosition, previousWaypointPosition.getBearingGreatCircle(boatMarkPosition)).compareTo(Distance.NULL) < 0;
                    result = getWindFrom(finishLineAngleFromPortToStarboardWhenApproachingLineToCombinedWind,
                            bearingFromBoatMarkToPinEnd, pinEndOnPortWhenApproachingLine).add(new DegreeBearingImpl(-90) /* bow points east with rotation 0deg */);
                }
            }
        }
        return result;
    }

    private Bearing getWindFrom(Double lineAngleFromPortToStarboardWhenApproachingLineToCombinedWind,
            final Bearing bearingFromBoatMarkToPinEnd, final boolean pinEndOnPortWhenApproachingLine) {
        final Bearing windFrom = pinEndOnPortWhenApproachingLine ?
                // pin end on port, as usual:
                bearingFromBoatMarkToPinEnd.reverse().add(new DegreeBearingImpl(lineAngleFromPortToStarboardWhenApproachingLineToCombinedWind)) :
                // start boat on port; that's unusual:
                bearingFromBoatMarkToPinEnd.add(new DegreeBearingImpl(lineAngleFromPortToStarboardWhenApproachingLineToCombinedWind));
        return windFrom;
    }

    private Bearing bearingOrReverseDependingOnXTEToOther(Position pos, Bearing bearing, Position other) {
        final Bearing result;
        if (pos.crossTrackError(other, bearing).compareTo(Distance.NULL) > 0) {
            result = bearing.reverse();
        } else {
            result = bearing;
        }
        return result;
    }

    private boolean isReach(Double lineAngleToCombinedWind) {
        return lineAngleToCombinedWind == null ||
                Math.abs(90-Math.abs(lineAngleToCombinedWind)) > LegType.UPWIND_DOWNWIND_TOLERANCE_IN_DEG;
    }

}
