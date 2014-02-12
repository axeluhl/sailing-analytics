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
    private final boolean startsWithZeroScore;
    private final boolean firstColumnIsNonDiscardableCarryForward;
    private final boolean hasSplitFleetContiguousScoring;

    public UpdateSeries(RegattaIdentifier regattaIdentifier, String seriesName, boolean isMedal,
            int[] resultDiscardingThresholds, boolean startsWithZeroScore,
            boolean firstColumnIsNonDiscardableCarryForward, boolean hasSplitFleetContiguousScoring) {
        super(regattaIdentifier, seriesName);
        this.isMedal = isMedal;
        this.resultDiscardingThresholds = resultDiscardingThresholds;
        this.startsWithZeroScore = startsWithZeroScore;
        this.firstColumnIsNonDiscardableCarryForward = firstColumnIsNonDiscardableCarryForward;
        this.hasSplitFleetContiguousScoring = hasSplitFleetContiguousScoring;
    }

    @Override
    public Void internalApplyTo(RacingEventService toState) throws Exception {
        Series series = getSeries(toState);
        series.setIsMedal(isMedal);
        series.setResultDiscardingRule(resultDiscardingThresholds == null ?
                null : new ThresholdBasedResultDiscardingRuleImpl(resultDiscardingThresholds));
        series.setStartsWithZeroScore(startsWithZeroScore);
        series.setFirstColumnIsNonDiscardableCarryForward(firstColumnIsNonDiscardableCarryForward);
        series.setSplitFleetContiguousScoring(hasSplitFleetContiguousScoring);
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
