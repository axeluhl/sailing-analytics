package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.base.impl.RaceColumnInSeriesImpl;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.leaderboard.ResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.regattalike.IsRegattaLike;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A leaderboard that is based on the definition of a {@link Regatta} with its {@link Series} and {@link Fleet}. The regatta
 * leaderboard listens to its {@link Regatta} as a {@link RaceColumnListener} and forwards all link/unlink events for
 * {@link TrackedRace}s being linked to / unlinked from race columns to all {@link RaceColumnListener}s subscribed with
 * this leaderboard.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class RegattaLeaderboardImpl extends AbstractLeaderboardImpl implements RegattaLeaderboard {
    private static final long serialVersionUID = 2370461218294770084L;
    private final Regatta regatta;
    
    public RegattaLeaderboardImpl(Regatta regatta, ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(resultDiscardingRule);
        this.regatta = regatta;
        regatta.addRaceColumnListener(this);
    }

    /**
     * Updates the display name of this regatta leaderboard so that the regatta's name is no longer used as the default name.
     */
    @Override
    public void setName(String newName) {
        setDisplayName(newName);
    }

    @Override
    public Regatta getRegatta() {
        return regatta;
    }

    @Override
    public String getName() {
        return getRegatta().getName();
    }

    @Override
    public Iterable<RaceColumn> getRaceColumns() {
        List<RaceColumn> result = new ArrayList<RaceColumn>();
        for (Series series : getRegatta().getSeries()) {
            for (RaceColumn raceColumn : series.getRaceColumns()) {
                result.add(raceColumn);
            }
        }
        return result;
    }

    @Override
    public RaceColumnInSeries getRaceColumnByName(String columnName) {
        return (RaceColumnInSeriesImpl) super.getRaceColumnByName(columnName);
    }

    @Override
    public ScoringScheme getScoringScheme() {
        return regatta.getScoringScheme();
    }

    @Override
    public CourseArea getDefaultCourseArea() {
        return regatta.getDefaultCourseArea();
    }

    /**
     * If the regatta' series {@link Regatta#definesSeriesDiscardThresholds() define} their own result discarding rules, this leaderboard uses
     * a composite result discarding rule of type {@link PerSeriesResultDiscardingRuleImpl} that contains discards within each series according
     * to the series' discarding rules. Otherwise, the default cross-leaderboard result discarding rule is obtained from the super class
     * implementation.
     */
    @Override
    public ResultDiscardingRule getResultDiscardingRule() {
        if (regatta.definesSeriesDiscardThresholds()) {
            return new PerSeriesResultDiscardingRuleImpl(regatta);
        } else {
            return super.getResultDiscardingRule();
        }
    }
    
    @Override
    public IsRegattaLike getRegattaLike() {
        return regatta;
    }

    @Override
    public Iterable<Competitor> getAllCompetitors() {
        return regatta.getAllCompetitors();
    }
}
