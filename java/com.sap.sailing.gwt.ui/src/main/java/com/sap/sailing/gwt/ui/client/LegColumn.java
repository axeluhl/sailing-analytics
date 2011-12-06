package com.sap.sailing.gwt.ui.client;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sailing.gwt.ui.client.LegDetailColumn.LegDetailField;
import com.sap.sailing.gwt.ui.shared.DetailType;
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
    private final String headerStyle;
    private final String columnStyle;
    
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
    
    private class ManeuverCountLegDetailsColumn extends FormattedDoubleLegDetailColumn {
        public ManeuverCountLegDetailsColumn(String title, CellTable<LeaderboardRowDAO> leaderboardTable,
                String headerStyle, String columnStyle) {
            super(title, /*unit*/null, /* field */ null, /* decimals */ 0, leaderboardTable, headerStyle, columnStyle);
        }
        
        @Override
        protected String getTitle(LeaderboardRowDAO row) {
            String resultString = null;
            LegEntryDAO entry = getLegEntry(row);
            if (entry != null) {
                StringBuilder result = new StringBuilder();
                if (entry.numberOfTacks != null) {
                    result.append(entry.numberOfTacks);
                    result.append(" ");
                    result.append(stringConstants.tacks());
                }
                if (entry.numberOfJibes != null) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(entry.numberOfJibes);
                    result.append(" ");
                    result.append(stringConstants.jibes());
                }
                if (entry.numberOfPenaltyCircles != null) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(entry.numberOfPenaltyCircles);
                    result.append(" ");
                    result.append(stringConstants.penaltyCircles());
                }
                resultString = result.toString();
            }
            return resultString;
        }

        @Override
        public String getValue(LeaderboardRowDAO row) {
            Double fieldValue = getFieldValue(row);
            StringBuilder result = new StringBuilder();
            if (fieldValue != null) {
                result.append(getFormatter().format(fieldValue));
            }
            LegEntryDAO entry = getLegEntry(row);
            if (entry != null && entry.numberOfPenaltyCircles != null && (int) entry.numberOfPenaltyCircles != 0) {
                result.append(" (");
                result.append(entry.numberOfPenaltyCircles);
                result.append("P)");
            }
            return result.toString();
        }

        @Override
        protected Double getFieldValue(LeaderboardRowDAO row) {
            LegEntryDAO entry = getLegEntry(row);
            Double result = null;
            if (entry != null) {
                if (entry.numberOfTacks != null) {
                    result = (double) entry.numberOfTacks;
                }
                if (entry.numberOfJibes != null) {
                    if (result == null) {
                        result = (double) entry.numberOfJibes;
                    } else {
                        result += (double) entry.numberOfJibes;
                    }
                }
                if (entry.numberOfPenaltyCircles != null) {
                    if (result == null) {
                        result = (double) entry.numberOfPenaltyCircles;
                    } else {
                        result += (double) entry.numberOfPenaltyCircles;
                    }
                }
            }
            return result;
        }

    }
    public LegColumn(LeaderboardPanel leaderboardPanel, String raceName, int legIndex, StringConstants stringConstants,
            List<DetailType> legDetailSelection, String headerStyle, String columnStyle,
            String detailHeaderStyle, String detailColumnStyle) {
        super(leaderboardPanel, /* expandable */true /* all legs have details */, new TextCell(), stringConstants,
                detailHeaderStyle, detailColumnStyle, legDetailSelection);
        setHorizontalAlignment(ALIGN_CENTER);
        this.raceName = raceName;
        this.legIndex = legIndex;
        this.stringConstants = stringConstants;
        this.headerStyle = headerStyle;
        this.columnStyle = columnStyle;
    }
    
    public static DetailType[] getAvailableLegDetailColumnTypes() {
        return new DetailType[] { DetailType.AVERAGE_SPEED_OVER_GROUND_IN_KNOTS,
                DetailType.DISTANCE_TRAVELED, DetailType.RANK_GAIN,
                DetailType.CURRENT_SPEED_OVER_GROUND_IN_KNOTS,
                DetailType.ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS,
                DetailType.VELOCITY_MADE_GOOD_IN_KNOTS, DetailType.GAP_TO_LEADER_IN_SECONDS,
                DetailType.WINDWARD_DISTANCE_TO_GO_IN_METERS,
                DetailType.NUMBER_OF_MANEUVERS };
    }

    @Override
    protected Map<DetailType, SortableColumn<LeaderboardRowDAO, ?>> getDetailColumnMap(
            LeaderboardPanel leaderboardPanel, StringConstants stringConstants, String detailHeaderStyle,
            String detailColumnStyle) {
        Map<DetailType, SortableColumn<LeaderboardRowDAO, ?>> result = new HashMap<DetailType, SortableColumn<LeaderboardRowDAO, ?>>();
        result.put(DetailType.DISTANCE_TRAVELED,
                new FormattedDoubleLegDetailColumn(stringConstants.distanceInMeters(), stringConstants.distanceInMetersUnit(),
                        new DistanceTraveledInMeters(), 0, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.AVERAGE_SPEED_OVER_GROUND_IN_KNOTS, new FormattedDoubleLegDetailColumn(
                stringConstants.averageSpeedInKnots(), stringConstants.averageSpeedInKnotsUnit(), new AverageSpeedOverGroundInKnots(), 2, leaderboardPanel
                                .getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.CURRENT_SPEED_OVER_GROUND_IN_KNOTS, new FormattedDoubleLegDetailColumn(
                stringConstants.currentSpeedOverGroundInKnots(), stringConstants.currentSpeedOverGroundInKnotsUnit(), new CurrentSpeedOverGroundInKnots(),
                2, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.ESTIMATED_TIME_TO_NEXT_WAYPOINT_IN_SECONDS,
                new FormattedDoubleLegDetailColumn(stringConstants.estimatedTimeToNextWaypointInSeconds(),
                        stringConstants.estimatedTimeToNextWaypointInSecondsUnit(),
                        new EstimatedTimeToNextWaypointInSeconds(), 1, getLeaderboardPanel().getLeaderboardTable(),
                        detailHeaderStyle, detailColumnStyle));
        result.put(
                DetailType.GAP_TO_LEADER_IN_SECONDS,
                new FormattedDoubleLegDetailColumn(stringConstants.gapToLeaderInSeconds(), stringConstants.gapToLeaderInSecondsUnit(),
                        new GapToLeaderInSeconds(), 0, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.VELOCITY_MADE_GOOD_IN_KNOTS,
                new FormattedDoubleLegDetailColumn(stringConstants.velocityMadeGoodInKnots(),
                        stringConstants.velocityMadeGoodInKnotsUnit(), new VelocityMadeGoodInKnots(), 2,
                        getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.WINDWARD_DISTANCE_TO_GO_IN_METERS, new FormattedDoubleLegDetailColumn(
                stringConstants.windwardDistanceToGoInMeters(), stringConstants.windwardDistanceToGoInMetersUnit(), new WindwardDistanceToGoInMeters(),
                0, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.RANK_GAIN, new RankGainColumn(stringConstants.rankGain(), new RankGain(),
                getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.NUMBER_OF_MANEUVERS, new ManeuverCountLegDetailsColumn(
                stringConstants.numberOfManeuvers(), getLeaderboardPanel()
                                .getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
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
    
}

