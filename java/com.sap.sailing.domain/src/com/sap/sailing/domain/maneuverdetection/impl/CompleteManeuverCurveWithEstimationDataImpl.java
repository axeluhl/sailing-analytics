package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverMainCurveWithEstimationData;
import com.sap.sse.common.Bearing;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompleteManeuverCurveWithEstimationDataImpl implements CompleteManeuverCurveWithEstimationData {

    private final ManeuverMainCurveWithEstimationData mainCurve;
    private final ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData curveWithUnstableCourseAndSpeed;
    private final Wind wind;
    private final int tackingCount;
    private final int jibingCount;
    private final boolean maneuverStartsByRunningAwayFromTheWind;
    private final Bearing relativeBearingToNextMarkBeforeManeuver;
    private final Bearing relativeBearingToNextMarkAfterManeuver;
    private final boolean markPassing;

    public CompleteManeuverCurveWithEstimationDataImpl(ManeuverMainCurveWithEstimationData mainCurve,
            ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData curveWithUnstableCourseAndSpeed, Wind wind,
            int tackingCount, int jibingCount, boolean maneuverStartsByRunningAwayFromTheWind,
            Bearing relativeBearingToNextMarkBeforeManeuver,
            Bearing relativeBearingToNextMarkAfterManeuver, boolean markPassing) {
        this.mainCurve = mainCurve;
        this.curveWithUnstableCourseAndSpeed = curveWithUnstableCourseAndSpeed;
        this.wind = wind;
        this.tackingCount = tackingCount;
        this.jibingCount = jibingCount;
        this.maneuverStartsByRunningAwayFromTheWind = maneuverStartsByRunningAwayFromTheWind;
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
    public boolean isManeuverStartsByRunningAwayFromTheWind() {
        return maneuverStartsByRunningAwayFromTheWind;
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

}
