package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasManeuverContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.Maneuver;

public class ManeuverWithContext implements HasManeuverContext {

    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitor;
    private final Maneuver maneuver;

    public ManeuverWithContext(HasTrackedLegOfCompetitorContext trackedLegOfCompetitor, Maneuver maneuver) {
        this.trackedLegOfCompetitor = trackedLegOfCompetitor;
        this.maneuver = maneuver;
    }

    @Override
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext() {
        return trackedLegOfCompetitor;
    }

    @Override
    public Maneuver getManeuver() {
        return maneuver;
    }

    @Override
    public ManeuverType getManeuverType() {
        return getManeuver().getType();
    }

    @Override
    public Tack getTack() {
        return getManeuver().getNewTack();
    }

    @Override
    public SpeedWithBearing getSpeedBefore() {
        return getManeuver().getSpeedWithBearingBefore();
    }

    @Override
    public SpeedWithBearing getSpeedAfter() {
        return getManeuver().getSpeedWithBearingAfter();
    }

    @Override
    public Double getDirectionChangeInDegrees() {
        return getManeuver().getDirectionChangeInDegrees();
    }

    @Override
    public Double getAbsoluteDirectionChangeInDegrees() {
        return Math.abs(getManeuver().getDirectionChangeInDegrees());
    }

    @Override
    public NauticalSide getToSide() {
        return getManeuver().getDirectionChangeInDegrees() >= 0 ? NauticalSide.STARBOARD : NauticalSide.PORT;
    }

    @Override
    public Distance getManeuverLoss() {
        return getManeuver().getManeuverLoss();
    }

}
