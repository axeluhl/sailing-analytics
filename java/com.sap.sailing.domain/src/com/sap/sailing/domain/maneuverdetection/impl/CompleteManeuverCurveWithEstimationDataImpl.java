package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverMainCurveWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompleteManeuverCurveWithEstimationDataImpl implements CompleteManeuverCurveWithEstimationData {

    private static final long serialVersionUID = 8858661029172491784L;
    private final ManeuverMainCurveWithEstimationData mainCurve;
    private final ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData curveWithUnstableCourseAndSpeed;
    private final Wind wind;
    private final int tackingCount;
    private final int jibingCount;
    private final boolean maneuverStartsByRunningAwayFromWind;
    private final Bearing relativeBearingToNextMarkBeforeManeuver;
    private final Bearing relativeBearingToNextMarkAfterManeuver;
    private final boolean markPassing;
    private Position position;

    public CompleteManeuverCurveWithEstimationDataImpl(Position position, ManeuverMainCurveWithEstimationData mainCurve,
            ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData curveWithUnstableCourseAndSpeed, Wind wind,
            int tackingCount, int jibingCount, boolean maneuverStartsByRunningAwayFromWind,
            Bearing relativeBearingToNextMarkBeforeManeuver, Bearing relativeBearingToNextMarkAfterManeuver,
            boolean markPassing) {
        this.position = position;
        this.mainCurve = mainCurve;
        this.curveWithUnstableCourseAndSpeed = curveWithUnstableCourseAndSpeed;
        this.wind = wind;
        this.tackingCount = tackingCount;
        this.jibingCount = jibingCount;
        this.maneuverStartsByRunningAwayFromWind = maneuverStartsByRunningAwayFromWind;
        this.relativeBearingToNextMarkBeforeManeuver = relativeBearingToNextMarkBeforeManeuver;
        this.relativeBearingToNextMarkAfterManeuver = relativeBearingToNextMarkAfterManeuver;
        this.markPassing = markPassing;
    }

    @Override
    public ManeuverMainCurveWithEstimationData getMainCurve() {
        return mainCurve;
    }

    @Override
    public ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData getCurveWithUnstableCourseAndSpeed() {
        return curveWithUnstableCourseAndSpeed;
    }

    @Override
    public Wind getWind() {
        return wind;
    }

    @Override
    public int getTackingCount() {
        return tackingCount;
    }

    @Override
    public int getJibingCount() {
        return jibingCount;
    }

    @Override
    public boolean isManeuverStartsByRunningAwayFromWind() {
        return maneuverStartsByRunningAwayFromWind;
    }

    @Override
    public Bearing getRelativeBearingToNextMarkBeforeManeuver() {
        return relativeBearingToNextMarkBeforeManeuver;
    }

    @Override
    public Bearing getRelativeBearingToNextMarkAfterManeuver() {
        return relativeBearingToNextMarkAfterManeuver;
    }

    @Override
    public boolean isMarkPassing() {
        return markPassing;
    }

    @Override
    public Position getPosition() {
        return position;
    }

}
