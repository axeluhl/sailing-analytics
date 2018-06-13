package com.sap.sailing.domain.leaderboard.meta;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.MetaLeaderboard;
import com.sap.sailing.domain.leaderboard.ScoreCorrectionListener;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.AbstractSimpleLeaderboardImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

/**
 * A leaderboard whose columns are defined by leaderboards. This can be useful for a regatta series where many regattas
 * shall feed into a single overall scoring board.
 * <p>
 * 
 * For now, the implementation provided by this class is as trivial as possible. No discards, no carried points, no
 * "tracked races," but the possibility to apply score corrections and competitor renamings.
 * <p>
 * 
 * Instances of subclasses need to register a {@link ScoreCorrectionChangeForwarder} as listener on each leaderboard
 * referenced by this meta leaderboard to ensure that score corrections applied to the "inner" leaderboard are signaled
 * to {@link ScoreCorrectionListeners} on this meta leaderboard's score correction. See
 * {@link #registerScoreCorrectionAndRaceColumnChangeForwarder(Leaderboard)} and
 * {@link #unregisterScoreCorrectionChangeForwarder(Leaderboard)}.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public abstract class AbstractMetaLeaderboard extends AbstractSimpleLeaderboardImpl implements MetaLeaderboard {
    private static final long serialVersionUID = 2368754404068836260L;
    private final Fleet metaFleet;
    private final ScoringScheme scoringScheme;
    private final String name;
    
    /**
     * Weak hash maps cannot be serialized. Therefore, this field is serialized as a {@link HashMap} in {@link #writeObject(ObjectOutputStream)}
     * and de-serialized into a new {@link WeakHashMap} in {@link #readObject(ObjectInputStream)} again.
     */
    private transient WeakHashMap<Leaderboard, MetaLeaderboardColumn> columnsForLeaderboards;
    
    /**
     * Weak hash maps cannot be serialized. Therefore, this field is serialized as a {@link HashMap} in {@link #writeObject(ObjectOutputStream)}
     * and de-serialized into a new {@link WeakHashMap} in {@link #readObject(ObjectInputStream)} again.
     */
    private transient WeakHashMap<Leaderboard, ScoreCorrectionListener> scoreCorrectionChangeForwardersByLeaderboard;
    
    private class ScoreCorrectionChangeForwarder implements ScoreCorrectionListener, Serializable {
        private static final long serialVersionUID = 915433462154943441L;

        @Override
        public void correctedScoreChanged(Competitor competitor, RaceColumn raceColumn, Double oldCorrectedScore, Double newCorrectedScore) {
            getScoreCorrection().notifyListeners(competitor, raceColumn, oldCorrectedScore, newCorrectedScore);
        }

        @Override
        public void maxPointsReasonChanged(Competitor competitor, RaceColumn raceColumn,
                MaxPointsReason oldMaxPointsReason, MaxPointsReason newMaxPointsReason) {
            getScoreCorrection().notifyListeners(competitor, raceColumn, oldMaxPointsReason, newMaxPointsReason);
        }

        @Override
        public void carriedPointsChanged(Competitor competitor, Double oldCarriedPoints, Double newCarriedPoints) {
            getScoreCorrection().notifyListenersAboutCarriedPointsChange(competitor, oldCarriedPoints, newCarriedPoints);
        }

        @Override
        public void isSuppressedChanged(Competitor competitor, boolean newIsSuppressed) {
            getScoreCorrection().notifyListenersAboutIsSuppressedChange(competitor, newIsSuppressed);
        }

        @Override
        public void timePointOfLastCorrectionsValidityChanged(TimePoint oldTimePointOfLastCorrectionsValidity,
                TimePoint newTimePointOfLastCorrectionsValidity) {
            getScoreCorrection().notifyListenersAboutLastCorrectionsValidityChanged(oldTimePointOfLastCorrectionsValidity, newTimePointOfLastCorrectionsValidity);
        }

        @Override
        public void commentChanged(String oldComment, String newComment) {
            getScoreCorrection().notifyListenersAboutCommentChanged(oldComment, newComment);
        }
    }
    
    public AbstractMetaLeaderboard(String name, ScoringScheme scoringScheme,
            ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(resultDiscardingRule);
        metaFleet = new FleetImpl("MetaFleet");
        this.scoringScheme = scoringScheme;
        this.name = name;
        columnsForLeaderboards = new WeakHashMap<>();
        scoreCorrectionChangeForwardersByLeaderboard = new WeakHashMap<Leaderboard, ScoreCorrectionListener>();
    }
    
    @Override
    protected SettableScoreCorrection createScoreCorrection() {
        return new MetaLeaderboardScoreCorrection(this);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(new HashMap<Leaderboard, MetaLeaderboardColumn>(columnsForLeaderboards));
        oos.writeObject(new HashMap<Leaderboard, ScoreCorrectionListener>(scoreCorrectionChangeForwardersByLeaderboard));
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        @SuppressWarnings("unchecked")
        Map<? extends Leaderboard, ? extends MetaLeaderboardColumn> columnsForLeaderboardAsStrongMap = (Map<? extends Leaderboard, ? extends MetaLeaderboardColumn>) ois.readObject();
        columnsForLeaderboards = new WeakHashMap<Leaderboard, MetaLeaderboardColumn>(columnsForLeaderboardAsStrongMap);
        @SuppressWarnings("unchecked")
        Map<? extends Leaderboard, ? extends ScoreCorrectionListener> scoreCorrectionChangeForwardersByLeaderboardAsStrongMap = (Map<? extends Leaderboard, ? extends ScoreCorrectionListener>) ois.readObject();
        scoreCorrectionChangeForwardersByLeaderboard = new WeakHashMap<Leaderboard, ScoreCorrectionListener>(scoreCorrectionChangeForwardersByLeaderboardAsStrongMap);
    }

    public abstract Iterable<Leaderboard> getLeaderboards();
    
    @Override
    public MetaLeaderboardScoreCorrection getScoreCorrection() {
        return (MetaLeaderboardScoreCorrection) super.getScoreCorrection();
    }

    @Override
    public Iterable<Boat> getAllBoats() {
        Set<Boat> boats = new HashSet<>();
        for (Leaderboard leaderboard : getLeaderboards()) {
            for (Boat boat: leaderboard.getAllBoats()) {
                boats.add(boat);
            }
        }
        return boats;
    }

    @Override
    public Pair<Iterable<RaceDefinition>, Iterable<Competitor>> getAllCompetitorsWithRaceDefinitionsConsidered() {
        Set<Competitor> competitors = new HashSet<Competitor>();
        Set<RaceDefinition> raceDefinitionsConsidered = new HashSet<>();
        for (Leaderboard leaderboard : getLeaderboards()) {
            final Pair<Iterable<RaceDefinition>, Iterable<Competitor>> allCompetitorsFromLeaderboardWithRaceDefinitionsConsidered = leaderboard.getAllCompetitorsWithRaceDefinitionsConsidered();
            Util.addAll(allCompetitorsFromLeaderboardWithRaceDefinitionsConsidered.getA(), raceDefinitionsConsidered);
            Util.addAll(allCompetitorsFromLeaderboardWithRaceDefinitionsConsidered.getB(), competitors);
        }
        return new Pair<>(raceDefinitionsConsidered, competitors);
    }
 
    @Override
    public Iterable<Competitor> getAllCompetitors(RaceColumn raceColumn, Fleet fleet) {
        final Iterable<Competitor> result;
        if (fleet == metaFleet && Util.contains(getRaceColumns(), raceColumn)) {
            result = ((MetaLeaderboardColumn) raceColumn).getAllCompetitors();
        } else {
            result = Collections.emptySet();
        }
        return result;
    }

    @Override
    public Boat getBoatOfCompetitor(Competitor competitor, RaceColumn raceColumn, Fleet fleet) {
        return null;
    }
    
    @Override
    public Fleet getFleet(String fleetName) {
        return fleetName.equals(metaFleet.getName()) ? metaFleet : null;
    }

    @Override
    public int getTrackedRank(Competitor competitor, RaceColumn race, TimePoint timePoint) {
        final Leaderboard leaderboard = ((MetaLeaderboardColumn) race).getLeaderboard();
        final int result;
        if (leaderboard.hasScores(competitor, timePoint)) {
            final List<Competitor> competitorsFromBestToWorst = leaderboard.getCompetitorsFromBestToWorst(timePoint);
            Util.removeAll(getSuppressedCompetitors(), competitorsFromBestToWorst);
            result = competitorsFromBestToWorst.indexOf(competitor)+1;
        } else {
            result = 0;
        }
        return result;
    }

    @Override
    public Iterable<RaceColumn> getRaceColumns() {
        List<RaceColumn> result = new ArrayList<RaceColumn>(Util.size(getLeaderboards()));
        for (Leaderboard leaderboard : getLeaderboards()) {
            result.add(getColumnForLeaderboard(leaderboard));
        }
        return result;
    }

    protected RaceColumn getColumnForLeaderboard(Leaderboard leaderboard) {
        MetaLeaderboardColumn result = columnsForLeaderboards.get(leaderboard);
        if (result == null) {
            result = new MetaLeaderboardColumn(leaderboard, metaFleet);
            result.addRaceColumnListener(this);
            columnsForLeaderboards.put(leaderboard, result);
        }
        return result;
    }

    protected void registerScoreCorrectionAndRaceColumnChangeForwarder(Leaderboard leaderboard) {
        leaderboard.addRaceColumnListener(this);
        final ScoreCorrectionChangeForwarder listener = new ScoreCorrectionChangeForwarder();
        scoreCorrectionChangeForwardersByLeaderboard.put(leaderboard, listener);
        leaderboard.addScoreCorrectionListener(listener);
    }
    
    protected void unregisterScoreCorrectionChangeForwarder(Leaderboard leaderboard) {
        leaderboard.getScoreCorrection().removeScoreCorrectionListener(scoreCorrectionChangeForwardersByLeaderboard.get(leaderboard));
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
