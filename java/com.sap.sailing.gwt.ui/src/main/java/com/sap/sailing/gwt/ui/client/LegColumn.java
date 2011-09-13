package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sailing.gwt.ui.client.LegDetailColumn.LegDetailField;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;
import com.sap.sailing.gwt.ui.shared.LegEntryDAO;

/**
 * Displays competitor's rank in leg and makes the column sortable by rank. The leg is
 * identified as an index into the {@link LeaderboardEntryDAO#legDetails} list.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public class LegColumn extends ExpandableSortableColumn<String> {
    private final String raceName;
    private final int legIndex;
    private final StringConstants stringConstants;
    
    private abstract class AbstractLegDetailField<T> implements LegDetailField<T> {
        public T get(LeaderboardRowDAO row) {
            LegEntryDAO entry = getLegEntry(row);
            if (entry == null) {
                return null;
            } else {
                return getFromNonNullEntry(entry);
            }
        }

        protected abstract T getFromNonNullEntry(LegEntryDAO entry);
    }
    
    private class DistanceTraveledInMeters extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDAO entry) {
            return entry.distanceTraveledInMeters;
        }
    }
    
    private class AverageSpeedOverGroundInKnots extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDAO entry) {
            return entry.averageSpeedOverGroundInKnots;
        }
    }
    
    private class RankGain implements LegDetailField<Integer> {
        @Override
        public Integer get(LeaderboardRowDAO row) {
            LegEntryDAO legEntry = getLegEntry(row);
            if (legEntry == null || getLegIndex() == 0) {
                // no gain/loss for first leg
                return null;
            } else {
                LegEntryDAO previousEntry = getLegEntry(row, getLegIndex()-1);
                return previousEntry == null ? null : legEntry.rank - previousEntry.rank;
            }
        }
    }
    
    public LegColumn(LeaderboardPanel leaderboardPanel, String raceName, int legIndex, StringConstants stringConstants) {
        super(leaderboardPanel, /* expandable */ true /* all legs have details */, new TextCell());
        setHorizontalAlignment(ALIGN_RIGHT);
        this.raceName = raceName;
        this.legIndex = legIndex;
        this.stringConstants = stringConstants;
    }
    
    private int getLegIndex() {
        return legIndex;
    }
    
    private String getRaceName() {
        return raceName;
    }

    private LegEntryDAO getLegEntry(LeaderboardRowDAO row) {
        int theLegIndex = getLegIndex();
        return getLegEntry(row, theLegIndex);
    }

    private LegEntryDAO getLegEntry(LeaderboardRowDAO row, int theLegIndex) {
        LegEntryDAO legEntry = null;
        LeaderboardEntryDAO entry = row.fieldsByRaceName.get(getRaceName());
        if (entry != null && entry.legDetails != null) {
            legEntry = entry.legDetails.get(theLegIndex);
        }
        return legEntry;
    }
    
    @Override
    public Comparator<LeaderboardRowDAO> getComparator() {
        return new Comparator<LeaderboardRowDAO>() {
            @Override
            public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                return safeGetLegRank(o1) - safeGetLegRank(o2);
            }
        };
    }

    private int safeGetLegRank(LeaderboardRowDAO row) {
        int result = 0;
        LegEntryDAO legEntry = getLegEntry(row);
        if (legEntry != null) {
            result = legEntry.rank;
        }
        return result;
    }

    @Override
    public Header<SafeHtml> getHeader() {
        return new SortableExpandableColumnHeader(/* title */ stringConstants.leg()+" "+(legIndex+1),
                /* iconURL */ null, getLeaderboardPanel(), this, stringConstants);
    }
    
    @Override
    public String getValue(LeaderboardRowDAO row) {
        return ""+safeGetLegRank(row);
    }

    @Override
    protected List<SortableColumn<LeaderboardRowDAO, ?>> createExpansionColumns() {
        List<SortableColumn<LeaderboardRowDAO, ?>> result = new ArrayList<SortableColumn<LeaderboardRowDAO,?>>();
        try {
            result.add(new FormattedDoubleLegDetailColumn(stringConstants.distanceInMeters(), new DistanceTraveledInMeters(), 1));
            result.add(new FormattedDoubleLegDetailColumn(stringConstants.averageSpeedInKnots(), new AverageSpeedOverGroundInKnots(), 2));
            result.add(new RankGainColumn(stringConstants.rankGain(), new RankGain()));
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

