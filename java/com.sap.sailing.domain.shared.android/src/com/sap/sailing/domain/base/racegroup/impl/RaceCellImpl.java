package com.sap.sailing.domain.base.racegroup.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sse.common.impl.NamedImpl;

public class RaceCellImpl extends NamedImpl implements RaceCell {
    private static final long serialVersionUID = 971598420407273594L;

    private final RaceLog raceLog;
    
    /**
     * See {@code RaceColumn.getFactor()}
     */
    private final double factor;
    
    /**
     * See {@code RaceColumn.getExplicitFactor()}
     */
    private final Double explicitFactor;

    public RaceCellImpl(String raceColumnName, RaceLog raceLog, double factor, Double explicitFactor) {
        super(raceColumnName);
        this.raceLog = raceLog;
        this.factor = factor;
        this.explicitFactor = explicitFactor;
    }

    @Override
    public RaceLog getRaceLog() {
        return raceLog;
    }

    /**
     * Represents the effective factor / multiplier for the scores in this column. This factor may have been determined
     * by a default rule or by a factor set explicitly. See {@link #getExplicitFactor()}.
     */
    @Override
    public double getFactor() {
        return factor;
    }

    /**
     * Represents the explicit factor / multiplier set for the scores in this column. If {@code null},
     * the score multiplying {@link #getFactor() factor} is determined by default rules.
     */
    @Override
    public Double getExplicitFactor() {
        return explicitFactor;
    }
}
