package com.sap.sailing.datamining.shared;

import com.sap.sse.common.Duration;
import com.sap.sse.common.settings.SerializableSettings;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class ManeuverSettings extends SerializableSettings {

    private static final long serialVersionUID = -393430331972342L;

    public abstract Duration getMinManeuverDuration();

    public abstract Duration getMaxManeuverDuration();

    public abstract Double getMinManeuverEnteringSpeedInKnots();

    public abstract Double getMaxManeuverEnteringSpeedInKnots();

    public abstract Double getMinManeuverExitingSpeedInKnots();

    public abstract Double getMaxManeuverExitingSpeedInKnots();

    public abstract Double getMinManeuverEnteringAbsTWA();

    public abstract Double getMaxManeuverEnteringAbsTWA();

    public abstract Double getMinManeuverExitingAbsTWA();

    public abstract Double getMaxManeuverExitingAbsTWA();

    public abstract Double getMinAbsCourseChangeInDegrees();

    public abstract Double getMaxAbsCourseChangeInDegrees();

    public abstract Duration getMinDurationFromPrecedingManeuver();

    public abstract Duration getMaxDurationFromPrecedingManeuver();

    public abstract Duration getMinDurationToFollowingManeuver();

    public abstract Duration getMaxDurationToFollowingManeuver();

    public abstract boolean isMainCurveAnalysis();

}
