package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.impl.RaceColumnListeners;

public abstract class SimpleAbstractRaceColumn implements RaceColumn {
    private static final long serialVersionUID = -3590156714385187908L;
    private final RaceColumnListeners raceColumnListeners;

    /**
     * The factor by which a medal race score is multiplied by default in the overall point scheme.
     * 
     * @see #getFactor()
     */
    private static final double DEFAULT_MEDAL_RACE_FACTOR = 2.0;

    /**
     * If <code>null</code>, the {@link #getFactor() factor} defaults to 1 for non-medal and {@link #DEFAULT_MEDAL_RACE_FACTOR} for
     * medal races. Otherwise, the explicit factor is used.
     */
    private Double explicitFactor;
    
    public SimpleAbstractRaceColumn() {
        raceColumnListeners = new RaceColumnListeners();
    }
    
    @Override
    public Pair<Competitor, RaceColumn> getKey(Competitor competitor) {
        return new Pair<Competitor, RaceColumn>(competitor, this);
    }

    public RaceColumnListeners getRaceColumnListeners() {
        return raceColumnListeners;
    }

    @Override
    public void addRaceColumnListener(RaceColumnListener listener) {
        getRaceColumnListeners().addRaceColumnListener(listener);
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        getRaceColumnListeners().removeRaceColumnListener(listener);
    }

    @Override
    public RaceDefinition getRaceDefinition(Fleet fleet) {
        TrackedRace trackedRace = getTrackedRace(fleet);
        RaceDefinition result = null;
        if (trackedRace != null) {
            result = trackedRace.getRace();
        }
        return result;
    }

    @Override
    public double getFactor() {
        return explicitFactor == null ? (isMedalRace() ? DEFAULT_MEDAL_RACE_FACTOR : 1.) : explicitFactor;
    }
    
    @Override
    public void setFactor(Double factor) {
        Double oldExplicitFactor = getExplicitFactor();
        explicitFactor = factor;
        raceColumnListeners.notifyListenersAboutFactorChanged(this, oldExplicitFactor, factor);
    }

    @Override
    public Double getExplicitFactor() {
        return explicitFactor;
    }

    /**
     * Returns <code>false</code>, meaning that by default when aggregating total scores across a leaderboard, this race column's score
     * is added to the aggregate of previous scores.
     */
    @Override
    public boolean isStartsWithZeroScore() {
        return false;
    }

    @Override
    public boolean isDiscardable() {
        return !isMedalRace();
    }

    @Override
    public boolean isCarryForward() {
        return false;
    }

    /**
     * Implements this by delegating to and negating the result of {@link #hasSplitFleetContiguousScoring()}. This is a
     * reasonable default implementation because if the score is not split up by fleet but scored contiguously
     * ascending/descending, this usually means that the fleets re-convene after this series instead of remaining split.
     */
    @Override
    public boolean isTotalOrderDefinedByFleet() {
        return !hasSplitFleetContiguousScoring();
    }

    @Override
    public boolean hasSplitFleetContiguousScoring() {
        return false;
    }

}
