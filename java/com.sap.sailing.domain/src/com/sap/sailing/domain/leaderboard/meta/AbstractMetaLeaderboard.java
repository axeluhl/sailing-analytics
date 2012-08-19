package com.sap.sailing.domain.leaderboard.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrectionListener;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.AbstractSimpleLeaderboardImpl;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * A leaderboard whose columns are defined by leaderboards. This can be useful for a regatta series where many regattas
 * shall feed into a single overall scoring board.
 * <p>
 * 
 * For now, the implementation provided by this class is as trivial as possible. No discards, no carried points,
 * no "tracked races," but the possibility to apply score corrections and competitor renamings.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public abstract class AbstractMetaLeaderboard extends AbstractSimpleLeaderboardImpl implements Leaderboard {
    private static final long serialVersionUID = 2368754404068836260L;
    private final Fleet metaFleet;
    private final ScoringScheme scoringScheme;
    private final String name;
    private final WeakHashMap<Leaderboard, RaceColumn> columnsForLeaderboards;
    
    private class ScoreCorrectionChangeForwarder implements ScoreCorrectionListener {
        @Override
        public void correctedScoreChanced(Competitor competitor, Double oldCorrectedScore, Double newCorrectedScore) {
            getScoreCorrection().notifyListeners(competitor, oldCorrectedScore, newCorrectedScore);
        }

        @Override
        public void maxPointsReasonChanced(Competitor competitor, MaxPointsReason oldMaxPointsReason,
                MaxPointsReason newMaxPointsReason) {
            getScoreCorrection().notifyListeners(competitor, oldMaxPointsReason, newMaxPointsReason);
        }
    }
    
    public AbstractMetaLeaderboard(String name, ScoringScheme scoringScheme, ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(new MetaLeaderboardScoreCorrection(), resultDiscardingRule);
        getScoreCorrection().setMetaLeaderboard(this);
        metaFleet = new FleetImpl("MetaFleet");
        this.scoringScheme = scoringScheme;
        this.name = name;
        columnsForLeaderboards = new WeakHashMap<>();
    }

    protected abstract Iterable<Leaderboard> getLeaderboards();
    
    @Override
    public MetaLeaderboardScoreCorrection getScoreCorrection() {
        return (MetaLeaderboardScoreCorrection) super.getScoreCorrection();
    }

    @Override
    public Iterable<Competitor> getCompetitors() {
        Set<Competitor> result = new HashSet<Competitor>();
        for (Leaderboard leaderboard : getLeaderboards()) {
            Util.addAll(leaderboard.getCompetitors(), result);
        }
        return result;
    }

    @Override
    public Fleet getFleet(String fleetName) {
        return fleetName.equals(metaFleet.getName()) ? metaFleet : null;
    }

    @Override
    public int getTrackedRank(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException {
        return ((MetaLeaderboardColumn) race).getLeaderboard().getCompetitorsFromBestToWorst(timePoint).indexOf(competitor)+1;
    }

    @Override
    public Iterable<RaceColumn> getRaceColumns() {
        List<RaceColumn> result = new ArrayList<RaceColumn>(Util.size(getLeaderboards()));
        for (Leaderboard leaderboard : getLeaderboards()) {
            result.add(getColumnForLeaderboard(leaderboard));
        }
        return result;
    }

    private RaceColumn getColumnForLeaderboard(Leaderboard leaderboard) {
        RaceColumn result = columnsForLeaderboards.get(leaderboard);
        if (result == null) {
            result = new MetaLeaderboardColumn(leaderboard, metaFleet);
            result.addRaceColumnListener(this); // forwards RaceColumnListener events from the sub-leaderboards to this leaderboard's listeners
            // also forward score correction changes applied to a sub-leaderboard to the score correction listeners on
            // this leaderboard's score correction object
            leaderboard.getScoreCorrection().addScoreCorrectionListener(new ScoreCorrectionChangeForwarder());
            columnsForLeaderboards.put(leaderboard, result);
        }
        return result;
    }

    @Override
    public Competitor getCompetitorByIdAsString(String idAsString) {
        for (Leaderboard leaderboard : getLeaderboards()) {
            Competitor result = leaderboard.getCompetitorByIdAsString(idAsString);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Starting from the last leaderboard in {@link #leaderboards}, looks for the first leadaerboard that returns a
     * non-<code>null</code> result for {@link Leaderboard#getDelayToLiveInMillis()} and returns it if found. If no such
     * leaderboard exists in {@link #leaderboards}, <code>null</code> is returned.
     */
    @Override
    public Long getDelayToLiveInMillis() {
        for (int i = Util.size(getLeaderboards()) - 1; i >= 0; i--) {
            Long result = Util.get(getLeaderboards(), i).getDelayToLiveInMillis();
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Iterable<TrackedRace> getTrackedRaces() {
        Set<TrackedRace> result = new HashSet<TrackedRace>();
        for (Leaderboard leaderboard : getLeaderboards()) {
            Util.addAll(leaderboard.getTrackedRaces(), result);
        }
        return result;
    }

    @Override
    public ScoringScheme getScoringScheme() {
        return scoringScheme;
    }

    @Override
    public String getName() {
        return name;
    }

}
