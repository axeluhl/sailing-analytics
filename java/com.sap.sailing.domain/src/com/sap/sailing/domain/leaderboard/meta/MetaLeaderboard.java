package com.sap.sailing.domain.leaderboard.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.impl.FleetImpl;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.ThresholdBasedResultDiscardingRule;
import com.sap.sailing.domain.leaderboard.impl.AbstractSimpleLeaderboardImpl;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

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
public class MetaLeaderboard extends AbstractSimpleLeaderboardImpl implements Leaderboard {
    private static final long serialVersionUID = 2368754404068836260L;
    private final List<Leaderboard> leaderboards;
    private final NamedReentrantReadWriteLock leaderboardsLock;
    private final Fleet metaFleet;
    private final ScoringScheme scoringScheme;
    private final String name;
    
    public MetaLeaderboard(String name, ScoringScheme scoringScheme, ThresholdBasedResultDiscardingRule resultDiscardingRule) {
        super(new MetaLeaderboardScoreCorrection(), resultDiscardingRule);
        leaderboards = new ArrayList<Leaderboard>();
        leaderboardsLock = new NamedReentrantReadWriteLock("Leaderboards lock for "+MetaLeaderboard.class.getSimpleName()+" "+name, /* fair */ false);
        metaFleet = new FleetImpl("MetaFleet");
        this.scoringScheme = scoringScheme;
        this.name = name;
    }
    
    public void addLeaderboard(Leaderboard leaderboard) {
        LockUtil.lockForWrite(leaderboardsLock);
        try {
            leaderboards.add(leaderboard);
        } finally {
            LockUtil.unlockAfterWrite(leaderboardsLock);
        }
    }
    
    @Override
    public Iterable<Competitor> getCompetitors() {
        LockUtil.lockForRead(leaderboardsLock);
        try {
            Set<Competitor> result = new HashSet<Competitor>();
            for (Leaderboard leaderboard : leaderboards) {
                Util.addAll(leaderboard.getCompetitors(), result);
            }
            return result;
        } finally {
            LockUtil.unlockAfterRead(leaderboardsLock);
        }
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
        LockUtil.lockForRead(leaderboardsLock);
        try {
            List<RaceColumn> result = new ArrayList<RaceColumn>(leaderboards.size());
            for (Leaderboard leaderboard : leaderboards) {
                result.add(new MetaLeaderboardColumn(leaderboard, metaFleet));
            }
            return result;
        } finally {
            LockUtil.unlockAfterRead(leaderboardsLock);
        }
    }

    @Override
    public Competitor getCompetitorByIdAsString(String idAsString) {
        LockUtil.lockForRead(leaderboardsLock);
        try {
            for (Leaderboard leaderboard : leaderboards) {
                Competitor result = leaderboard.getCompetitorByIdAsString(idAsString);
                if (result != null) {
                    return result;
                }
            }
            return null;
        } finally {
            LockUtil.unlockAfterRead(leaderboardsLock);
        }
    }

    /**
     * Starting from the last leaderboard in {@link #leaderboards}, looks for the first leadaerboard that returns a
     * non-<code>null</code> result for {@link Leaderboard#getDelayToLiveInMillis()} and returns it if found. If no such
     * leaderboard exists in {@link #leaderboards}, <code>null</code> is returned.
     */
    @Override
    public Long getDelayToLiveInMillis() {
        LockUtil.lockForRead(leaderboardsLock);
        try {
            for (int i = leaderboards.size() - 1; i >= 0; i--) {
                Long result = leaderboards.get(i).getDelayToLiveInMillis();
                if (result != null) {
                    return result;
                }
            }
            return null;
        } finally {
            LockUtil.unlockAfterRead(leaderboardsLock);
        }
    }

    @Override
    public Iterable<TrackedRace> getTrackedRaces() {
        LockUtil.lockForRead(leaderboardsLock);
        try {
            Set<TrackedRace> result = new HashSet<TrackedRace>();
            for (Leaderboard leaderboard : leaderboards) {
                Util.addAll(leaderboard.getTrackedRaces(), result);
            }
            return result;
        } finally {
            LockUtil.unlockAfterRead(leaderboardsLock);
        }
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
