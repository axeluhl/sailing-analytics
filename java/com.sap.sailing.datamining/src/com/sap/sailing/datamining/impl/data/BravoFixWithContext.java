package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasBravoFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.BravoFix;

public class BravoFixWithContext implements HasBravoFixContext {
    private static final long serialVersionUID = -4537126043228674949L;

    private final HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext;
    
    private final BravoFix bravoFix;
    private Wind wind;

    public BravoFixWithContext(HasTrackedLegOfCompetitorContext trackedLegOfCompetitorContext, BravoFix bravoFix) {
        this.trackedLegOfCompetitorContext = trackedLegOfCompetitorContext;
        this.bravoFix = bravoFix;
    }
    
    @Override
    public HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext() {
        return trackedLegOfCompetitorContext;
    }

    @Override
    public BravoFix getBravoFix() {
        return bravoFix;
    }
    
    @Override
    public Wind getWindInternal() {
        return wind;
    }

    @Override
    public void setWindInternal(Wind wind) {
        this.wind = wind;
    }

    @Override
    public Position getPosition() {
        return getTrackedLegOfCompetitorContext().getTrackedLegOfCompetitor().getTrackedLeg().getTrackedRace()
                .getTrack(getTrackedLegOfCompetitorContext().getCompetitor())
                .getEstimatedPosition(getTimePoint(), /* extrapolate */ true);
    }
}
