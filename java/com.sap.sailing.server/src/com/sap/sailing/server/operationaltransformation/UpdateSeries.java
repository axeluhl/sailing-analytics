package com.sap.sailing.server.operationaltransformation;

import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.leaderboard.impl.ThresholdBasedResultDiscardingRuleImpl;
import com.sap.sailing.server.RacingEventService;
import com.sap.sailing.server.RacingEventServiceOperation;

public class UpdateSeries extends AbstractSeriesOperation<Void> {
    private static final long serialVersionUID = 6356089749049112710L;
    
    private final boolean isMedal;
    private final int[] resultDiscardingThresholds;

    public UpdateSeries(RegattaIdentifier regattaIdentifier, String seriesName, boolean isMedal, int[] resultDiscardingThresholds) {
        super(regattaIdentifier, seriesName);
        this.isMedal = isMedal;
        this.resultDiscardingThresholds = resultDiscardingThresholds;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        Series series = getSeries(toState);
        series.setIsMedal(isMedal);
        series.setResultDiscardingRule(new ThresholdBasedResultDiscardingRuleImpl(resultDiscardingThresholds));
        if (series.getRegatta().isPersistent()) {
            toState.updateStoredRegatta(series.getRegatta());
        }
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformClientOp(RacingEventServiceOperation<?> serverOp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RacingEventServiceOperation<?> transformServerOp(RacingEventServiceOperation<?> clientOp) {
        // TODO Auto-generated method stub
        return null;
    }
}
