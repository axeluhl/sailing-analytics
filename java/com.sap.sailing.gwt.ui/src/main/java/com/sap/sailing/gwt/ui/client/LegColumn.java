package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final List<DetailColumnType> legDetailSelection;
    private final String headerStyle;
    private final String columnStyle;
    private final String detailHeaderStyle;
    private final String detailColumnStyle;
    
    private abstract class AbstractLegDetailField<T extends Comparable<?>> implements LegDetailField<T> {
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
    
    private class CurrentSpeedOverGroundInKnots extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDAO entry) {
            return entry.currentSpeedOverGroundInKnots;
        }
    }
    
    private class EstimatedTimeToNextWaypointInSeconds extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDAO entry) {
            return entry.estimatedTimeToNextWaypointInSeconds;
        }
    }
    
    private class GapToLeaderInSeconds extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDAO entry) {
            return entry.gapToLeaderInSeconds;
        }
    }
    
    private class VelocityMadeGoodInKnots extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDAO entry) {
            return entry.velocityMadeGoodInKnots;
        }
    }
    
    private class WindwardDistanceToGoInMeters extends AbstractLegDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LegEntryDAO entry) {
            return entry.windwardDistanceToGoInMeters;
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
    
    public LegColumn(LeaderboardPanel leaderboardPanel, String raceName, int legIndex, StringConstants stringConstants,
            List<DetailColumnType> legDetailSelection, String headerStyle, String columnStyle,
            String detailHeaderStyle, String detailColumnStyle) {
        super(leaderboardPanel, /* expandable */true /* all legs have details */, new TextCell(), stringConstants,
                detailHeaderStyle, detailColumnStyle, legDetailSelection);
        setHorizontalAlignment(ALIGN_CENTER);
        this.raceName = raceName;
        this.legIndex = legIndex;
        this.stringConstants = stringConstants;
        this.legDetailSelection = legDetailSelection;
        this.headerStyle = headerStyle;
        this.columnStyle = columnStyle;
        this.detailHeaderStyle = detailHeaderStyle;
        this.detailColumnStyle = detailColumnStyle;
    }
    
    public static DetailColumnType[] getAvailableLegDetailColumnTypes() {
        return new DetailColumnType[] { DetailColumnType.DISTANCE_TRAVELED,
                DetailColumnType.AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, DetailColumnType.RANK_GAIN,
                DetailColumnType.CURRENT_SPEED_OVER_GROUND_IN_KNOTS,
                DetailColumnType.ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS,
                DetailColumnType.VELOCITY_MADE_GOOD_IN_KNOTS, DetailColumnType.GAP_TO_LEADER_IN_SECONDS,
                DetailColumnType.WINDWARD_DISTANCE_TO_GO_IN_METERS };
    }

    @Override
    protected Map<DetailColumnType, SortableColumn<LeaderboardRowDAO, ?>> getDetailColumnMap(LeaderboardPanel leaderboardPanel, StringConstants stringConstants, String detailHeaderStyle, String detailColumnStyle) {
        Map<DetailColumnType, SortableColumn<LeaderboardRowDAO, ?>> result = new HashMap<DetailColumnType, SortableColumn<LeaderboardRowDAO, ?>>();
        result.put(DetailColumnType.DISTANCE_TRAVELED,
                new FormattedDoubleLegDetailColumn(stringConstants.distanceInMeters(), new DistanceTraveledInMeters(),
                        0, leaderboardPanel.getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailColumnType.AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, new FormattedDoubleLegDetailColumn(
                stringConstants.averageSpeedInKnots(), new AverageSpeedOverGroundInKnots(), 2, leaderboardPanel
                        .getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailColumnType.CURRENT_SPEED_OVER_GROUND_IN_KNOTS, new FormattedDoubleLegDetailColumn(
                stringConstants.currentSpeedOverGroundInKnots(), new CurrentSpeedOverGroundInKnots(), 1,
                leaderboardPanel.getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailColumnType.ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS, new FormattedDoubleLegDetailColumn(
                stringConstants.estimatedTimeToNextWaypointInSeconds(), new EstimatedTimeToNextWaypointInSeconds(), 1,
                leaderboardPanel.getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailColumnType.GAP_TO_LEADER_IN_SECONDS,
                new FormattedDoubleLegDetailColumn(stringConstants.gapToLeaderInSeconds(), new GapToLeaderInSeconds(),
                        1, leaderboardPanel.getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailColumnType.VELOCITY_MADE_GOOD_IN_KNOTS,
                new FormattedDoubleLegDetailColumn(stringConstants.velocityMadeGoodInKnots(),
                        new VelocityMadeGoodInKnots(), 1, leaderboardPanel.getLeaderboardTable(),
                        detailHeaderStyle, detailColumnStyle));
        result.put(DetailColumnType.WINDWARD_DISTANCE_TO_GO_IN_METERS, new FormattedDoubleLegDetailColumn(
                stringConstants.windwardDistanceToGoInMeters(), new WindwardDistanceToGoInMeters(), 1,
                leaderboardPanel.getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailColumnType.RANK_GAIN, new RankGainColumn(stringConstants.rankGain(), new RankGain(),
                leaderboardPanel.getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        return result;
    }

    private int getLegIndex() {
        return legIndex;
    }
    
    @Override
    public String getColumnStyle() {
        return columnStyle;
    }
    
    @Override
    public String getHeaderStyle() {
        return headerStyle;
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
                boolean ascending = isSortedAscendingForThisColumn(getLeaderboardPanel().getLeaderboardTable());
                LegEntryDAO o1Entry = getLegEntry(o1);
                LegEntryDAO o2Entry = getLegEntry(o2);
                return o1Entry == null ? o2Entry == null ? 0 : ascending?1:-1
                                       : o2Entry == null ? ascending?-1:1 : o1Entry.rank - o2Entry.rank;
            }
        };
    }

    @Override
    public Header<SafeHtml> getHeader() {
        SortableExpandableColumnHeader result = new SortableExpandableColumnHeader(/* title */ stringConstants.leg()+(legIndex+1),
                /* iconURL */ null, getLeaderboardPanel(), this, stringConstants);
        return result;
    }
    
    @Override
    public String getValue(LeaderboardRowDAO row) {
        LegEntryDAO legEntry = getLegEntry(row);
        if (legEntry != null) {
            return ""+legEntry.rank;
        } else {
            return "";
        }
    }
    
    @Override
    protected List<SortableColumn<LeaderboardRowDAO, ?>> createExpansionColumns() {
        List<SortableColumn<LeaderboardRowDAO, ?>> result = new ArrayList<SortableColumn<LeaderboardRowDAO,?>>();
        try {
            for (DetailColumnType type : legDetailSelection) {
                switch (type) {
                case DISTANCE_TRAVELED:
                    result.add(new FormattedDoubleLegDetailColumn(stringConstants.distanceInMeters(), new DistanceTraveledInMeters(), 0, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
                    break;
                case AVERAGE_SPEED_OVER_GROUND_IN_KNOTS:
                    result.add(new FormattedDoubleLegDetailColumn(stringConstants.averageSpeedInKnots(), new AverageSpeedOverGroundInKnots(), 2, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
                    break;
                case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
                    result.add(new FormattedDoubleLegDetailColumn(stringConstants.currentSpeedOverGroundInKnots(), new CurrentSpeedOverGroundInKnots(), 1, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
                    break;
                case ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS:
                    result.add(new FormattedDoubleLegDetailColumn(stringConstants.estimatedTimeToNextWaypointInSeconds(), new EstimatedTimeToNextWaypointInSeconds(), 1, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
                    break;
                case GAP_TO_LEADER_IN_SECONDS:
                    result.add(new FormattedDoubleLegDetailColumn(stringConstants.gapToLeaderInSeconds(), new GapToLeaderInSeconds(), 1, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
                    break;
                case VELOCITY_MADE_GOOD_IN_KNOTS:
                    result.add(new FormattedDoubleLegDetailColumn(stringConstants.velocityMadeGoodInKnots(), new VelocityMadeGoodInKnots(), 1, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
                    break;
                case WINDWARD_DISTANCE_TO_GO_IN_METERS:
                    result.add(new FormattedDoubleLegDetailColumn(stringConstants.windwardDistanceToGoInMeters(), new WindwardDistanceToGoInMeters(), 1, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
                    break;
                case RANK_GAIN:
                    result.add(new RankGainColumn(stringConstants.rankGain(), new RankGain(), getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
                    break;
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

