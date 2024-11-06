package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.shared.TackTypeSegmentsDataMiningSettings;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;

/**
 * Equality is based on the {@link #getTrackedLegOfCompetitor()} only.
 */
public class TrackedLegOfCompetitorWithContext extends TrackedLegSliceOfCompetitorWithContext implements HasTrackedLegOfCompetitorContext {
    private static final long serialVersionUID = 5944904146286262768L;

    public TrackedLegOfCompetitorWithContext(HasTrackedLegContext trackedLegContext, TrackedLegOfCompetitor trackedLegOfCompetitor, TackTypeSegmentsDataMiningSettings settings) {
        super(trackedLegContext, trackedLegOfCompetitor, settings, 1);
    }
}
