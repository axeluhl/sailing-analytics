package com.sap.sailing.domain.leaderboard.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.NoWindError;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.NamedImpl;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.SettableScoreCorrection;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
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
public class MetaLeaderboard extends NamedImpl implements Leaderboard {
    private static final long serialVersionUID = 2368754404068836260L;
    private final List<Leaderboard> leaderboards;
    private final MetaLeaderboardScoreCorrection scoreCorrection;
    private final Fleet metaFleet;
    private final ScoringScheme scoringScheme;
    
    public MetaLeaderboard(String name, ScoringScheme scoringScheme) {
        super(name);
        leaderboards = new ArrayList<Leaderboard>();
        scoreCorrection = new MetaLeaderboardScoreCorrection();
        metaFleet = new FleetImpl("MetaFleet");
        this.scoringScheme = scoringScheme;
    }
    
    @Override
    public Iterable<Competitor> getCompetitors() {
        Set<Competitor> result = new HashSet<Competitor>();
        for (Leaderboard leaderboard : leaderboards) {
            Util.addAll(leaderboard.getCompetitors(), result);
        }
        return result;
    }

    @Override
    public Fleet getFleet(String fleetName) {
        return fleetName.equals(metaFleet.getName()) ? metaFleet : null;
    }

    @Override
    public Entry getEntry(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getCarriedPoints(Competitor competitor) {
        return 0;
    }

    @Override
    public int getTrackedRank(Competitor competitor, RaceColumn race, TimePoint timePoint) throws NoWindException {
        return ((MetaLeaderboardColumn) race).getLeaderboard().getCompetitorsFromBestToWorst(timePoint).indexOf(competitor)+1;
    }

    @Override
    public Double getNetPoints(final Competitor competitor, final RaceColumn raceColumn, final TimePoint timePoint)
            throws NoWindException {
        return getScoreCorrection().getCorrectedScore(
                new Callable<Integer>() {
                    public Integer call() throws NoWindException {
                        return getTrackedRank(competitor, raceColumn, timePoint);
                    }
                }, competitor,
                raceColumn, timePoint, Util.size(getCompetitors()), getScoringScheme()).getCorrectedScore();
    }

    @Override
    public MaxPointsReason getMaxPointsReason(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return getScoreCorrection().getMaxPointsReason(competitor, raceColumn);
    }

    /**
     * For a meta leaderboard, net points equal total points as long as there are no discards and disqualifications
     */
    @Override
    public Double getTotalPoints(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) throws NoWindException {
        return getNetPoints(competitor, raceColumn, timePoint);
    }

    @Override
    public boolean isDiscarded(Competitor competitor, RaceColumn raceColumn, TimePoint timePoint) {
        return false;
    }

    @Override
    public Double getTotalPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        double result = getCarriedPoints(competitor);
        for (RaceColumn r : getRaceColumns()) {
            final Double totalPoints = getTotalPoints(competitor, r, timePoint);
            if (totalPoints != null) {
                result += totalPoints;
            }
        }
        return result;
    }

    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(final RaceColumn raceColumn, final TimePoint timePoint)
            throws NoWindException {
        final Comparator<Double> comparator = getScoringScheme().getScoreComparator(/* nullScoresAreBetter */ false);
        List<Competitor> sortedCompetitors = new ArrayList<Competitor>();
        Util.addAll(getCompetitors(), sortedCompetitors);
        Collections.sort(sortedCompetitors, new Comparator<Competitor>() {
            @Override
            public int compare(Competitor o1, Competitor o2) {
                try {
                    return comparator.compare(getTotalPoints(o1, raceColumn, timePoint), getTotalPoints(o2, raceColumn, timePoint));
                } catch (NoWindException e) {
                    throw new NoWindError(e);
                }
            }
        });
        return sortedCompetitors;
    }

    @Override
    public List<Competitor> getCompetitorsFromBestToWorst(final TimePoint timePoint) throws NoWindException {
        final Comparator<Double> comparator = getScoringScheme().getScoreComparator(/* nullScoresAreBetter */ false);
        List<Competitor> sortedCompetitors = new ArrayList<Competitor>();
        Util.addAll(getCompetitors(), sortedCompetitors);
        Collections.sort(sortedCompetitors, new Comparator<Competitor>() {
            @Override
            public int compare(Competitor o1, Competitor o2) {
                try {
                    return comparator.compare(getTotalPoints(o1, timePoint), getTotalPoints(o2, timePoint));
                } catch (NoWindException e) {
                    throw new NoWindError(e);
                }
            }
        });
        return sortedCompetitors;
    }

    @Override
    public Map<Pair<Competitor, RaceColumn>, Entry> getContent(TimePoint timePoint) throws NoWindException {
        // TODO Auto-generated method stub
        // TODO this finally seems the spark in the powder keg: extract common base class from AbstractLeaderboardImpl!
        return null;
    }

    @Override
    public Iterable<RaceColumn> getRaceColumns() {
        List<RaceColumn> result = new ArrayList<RaceColumn>(leaderboards.size());
        for (Leaderboard leaderboard : leaderboards) {
            result.add(new MetaLeaderboardColumn(leaderboard, metaFleet));
        }
        return result;
    }

    @Override
    public RaceColumn getRaceColumnByName(String name) {
        for (RaceColumn raceColumn : getRaceColumns()) {
            if (name.equals(raceColumn)) {
                return raceColumn;
            }
        }
        return null;
    }

    @Override
    public void setCarriedPoints(Competitor competitor, double carriedPoints) {
        // TODO Auto-generated method stub

    }

    @Override
    public void unsetCarriedPoints(Competitor competitor) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasCarriedPoints() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasCarriedPoints(Competitor competitor) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public SettableScoreCorrection getScoreCorrection() {
        return scoreCorrection;
    }

    @Override
    public ThresholdBasedResultDiscardingRule getResultDiscardingRule() {
        return new ThresholdBasedResultDiscardingRule() {
            private static final long serialVersionUID = 5961387567698376786L;
            @Override
            public Set<RaceColumn> getDiscardedRaceColumns(Competitor competitor, Leaderboard leaderboard, TimePoint timePoint) {
                return Collections.emptySet();
            }
            @Override
            public int[] getDiscardIndexResultsStartingWithHowManyRaces() {
                return new int[0];
            }
        };
    }

    @Override
    public Competitor getCompetitorByName(String competitorName) {
        for (Leaderboard leaderboard : leaderboards) {
            Competitor result = leaderboard.getCompetitorByName(competitorName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public void setDisplayName(Competitor competitor, String displayName) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getDisplayName(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean considerForDiscarding(RaceColumn raceColumn, TimePoint timePoint) {
        return true; // all columns are valid and hence "considered" for discarding, although none will finally be discarded
    }

    @Override
    public void setResultDiscardingRule(ThresholdBasedResultDiscardingRule discardingRule) {
        // no-op because this type of leaderboard doesn't support discards
    }

    @Override
    public Competitor getCompetitorByIdAsString(String idAsString) {
        for (Leaderboard leaderboard : leaderboards) {
            Competitor result = leaderboard.getCompetitorByIdAsString(idAsString);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public void addRaceColumnListener(RaceColumnListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public Long getDelayToLiveInMillis() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterable<TrackedRace> getTrackedRaces() {
        Set<TrackedRace> result = new HashSet<TrackedRace>();
        for (Leaderboard leaderboard : leaderboards) {
            Util.addAll(leaderboard.getTrackedRaces(), result);
        }
        return result;
    }

    @Override
    public ScoringScheme getScoringScheme() {
        return scoringScheme;
    }

}
