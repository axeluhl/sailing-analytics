package com.sap.sailing.domain.base.racegroup.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.common.TargetTimeInfo;
import com.sap.sse.common.Util;
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
    
    private final int zeroBasedIndexInFleet;
    
    private final TargetTimeInfo targetTime;

    /**
     * @param zeroBasedIndexInFleet
     *            A Series offers a sequence of RaceColumns, each of them split according to the Fleets modeled for the
     *            Series. A {@link RaceCell} describes a "slot" in this grid, defined by the series, the fleet and the
     *            race column. While this object's {@link #getName() name} represents the race column's name, this
     *            doesn't tell anything about the "horizontal" position in the "grid" or in other words what the index
     *            is of the race column in which this cell lies. Indices returned by this method start with zero,
     *            meaning the first race column in the series. This corresponds to what one would get by asking
     *            {@link Util#indexOf(Iterable, Object) Util.indexOf(series.getRaceColumns(), thisCellsRaceColumn)},
     *            except in case the first race column is a "virtual" one that holds a non-discardable carry-forward
     *            result. In this case, the second Race Column, which is the first "non-virtual" one, receives index 0.
     */
    public RaceCellImpl(String raceColumnName, RaceLog raceLog, double factor, Double explicitFactor, int zeroBasedIndexInFleet, TargetTimeInfo targetTime) {
        super(raceColumnName);
        this.raceLog = raceLog;
        this.factor = factor;
        this.explicitFactor = explicitFactor;
        this.zeroBasedIndexInFleet = zeroBasedIndexInFleet;
        this.targetTime = targetTime;
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

    @Override
    public int getZeroBasedIndexInFleet() {
        return zeroBasedIndexInFleet;
    }

    @Override
    public TargetTimeInfo getTargetTime() {
        return targetTime;
    }

    @Override
    public String toString() {
        return "RaceCellImpl [factor=" + factor + ", explicitFactor=" + explicitFactor + ", zeroBasedIndexInFleet="
                + zeroBasedIndexInFleet + ", name=" + getName() + ", targetTime: " + getTargetTime() + "]";
    }
}
