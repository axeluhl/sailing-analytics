package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasManeuverContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.Maneuver;

public class ManeuverWithContext implements HasManeuverContext {
    private static final long serialVersionUID = 7717196485074392156L;
    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitor;
    private final Maneuver maneuver;
    private Wind wind;

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
    public NauticalSide getToSide() {
        return getManeuver().getDirectionChangeInDegrees() >= 0 ? NauticalSide.STARBOARD : NauticalSide.PORT;
    }

    @Override
    public Double getAbsoluteDirectionChangeInDegrees() {
        return Math.abs(getManeuver().getDirectionChangeInDegrees());
    }
    
    @Override
    public Distance getManeuverLoss() {
        return getManeuver().getManeuverLoss();
    }

    @Override
    public Wind getWindInternal() {
        return wind;
    }

    @Override
    public void setWindInternal(Wind wind) {
        this.wind = wind;
    }
}
