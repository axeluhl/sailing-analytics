package com.sap.sailing.gwt.ui.client;

import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.Header;
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
    
    public LegColumn(LeaderboardPanel leaderboardPanel, String raceName, int legIndex) {
        super(leaderboardPanel, /* expandable */ true /* all legs have details */, new TextCell());
        this.raceName = raceName;
        this.legIndex = legIndex;
    }
    
    private int getLegIndex() {
        return legIndex;
    }
    
    private String getRaceName() {
        return raceName;
    }

    private LegEntryDAO getLegEntry(LeaderboardRowDAO row) {
        LegEntryDAO legEntry = null;
        LeaderboardEntryDAO entry = row.fieldsByRaceName.get(getRaceName());
        if (entry != null && entry.legDetails != null) {
            legEntry = entry.legDetails.get(getLegIndex());
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
        return new SortableExpandableColumnHeader(/* title */ "Leg "+(legIndex+1),
                /* iconURL */ null, getLeaderboardPanel(), this);
    }
    
    @Override
    public String getValue(LeaderboardRowDAO row) {
        return ""+safeGetLegRank(row);
    }

    @Override
    protected List<SortableColumn<LeaderboardRowDAO, ?>> createExpansionColumns() {
        // TODO this is where dynamic column configuration will go
        return super.createExpansionColumns();
    }
}

