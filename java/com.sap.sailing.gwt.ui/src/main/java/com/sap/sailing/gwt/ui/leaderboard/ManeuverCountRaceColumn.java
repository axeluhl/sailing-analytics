package com.sap.sailing.gwt.ui.leaderboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LegDetailColumn.LegDetailField;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.shared.LegEntryDTO;

public class ManeuverCountRaceColumn extends ExpandableSortableColumn<String> implements HasStringAndDoubleValue {
    private final StringMessages stringMessages;
    private final RaceNameProvider raceNameProvider;

    private final String headerStyle;
    private final String columnStyle;

    private final MinMaxRenderer minmaxRenderer;

    private abstract class AbstractManeuverDetailField<T extends Comparable<?>> implements LegDetailField<T> {
        public T get(LeaderboardRowDTO row) {
            LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceName());
            if (fieldsForRace == null) {
                return null;
            } else {
                T value = getFromNonNullEntry(fieldsForRace);
                return value;
            }
        }

        protected abstract T getFromNonNullEntry(LeaderboardEntryDTO entry);
    }

    private class NumberOfTacks extends AbstractManeuverDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDTO entry) {
            return ManeuverCountRaceColumn.this.getTotalNumberOfTacks(entry);
        }
    }

    private class NumberOfJibes extends AbstractManeuverDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDTO entry) {
            return ManeuverCountRaceColumn.this.getTotalNumberOfJibes(entry);
        }
    }

    private class NumberOfPenaltyCircles extends AbstractManeuverDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDTO entry) {
            return ManeuverCountRaceColumn.this.getTotalNumberOfPenaltyCircles(entry);
        }
    }

    private class AverageManeuverLossInMeters extends AbstractManeuverDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDTO entry) {
            return ManeuverCountRaceColumn.this.getAverageManeuverLossInMeters(entry);
        }
    }

    public ManeuverCountRaceColumn(LeaderboardPanel leaderboardPanel, RaceNameProvider raceNameProvider,
            StringMessages stringConstants, List<DetailType> maneuverDetailSelection, String headerStyle,
            String columnStylee, String detailHeaderStyle, String detailColumnStyle) {
        super(leaderboardPanel, /* expandable */true /* all legs have details */, new TextCell(), DetailType.NUMBER_OF_MANEUVERS.getDefaultSortingOrder(), 
                stringConstants, detailHeaderStyle, detailColumnStyle, maneuverDetailSelection);
        setHorizontalAlignment(ALIGN_CENTER);
        this.stringMessages = stringConstants;
        this.raceNameProvider = raceNameProvider;
        this.headerStyle = headerStyle;
        this.columnStyle = columnStylee;
        this.minmaxRenderer = new MinMaxRenderer(this, getComparator());
    }

    public Double getAverageManeuverLossInMeters(LeaderboardEntryDTO row) {
        Double maneuverLossInMetersSum = null;
        int count = 0;
        if (row != null && row.legDetails != null) {
            for (LegEntryDTO legDetail : row.legDetails) {
                if (legDetail != null) {
                    if (legDetail.averageManeuverLossInMeters != null) {
                        if (maneuverLossInMetersSum == null) {
                            maneuverLossInMetersSum = (double) legDetail.averageManeuverLossInMeters;
                        } else {
                            maneuverLossInMetersSum += (double) legDetail.averageManeuverLossInMeters;
                        }
                        Triple<Double, Double, Double> maneuverCounts = getTotalNumberOfTacksJibesAndPenaltyCircles(row);
                        count += maneuverCounts.getA()+maneuverCounts.getB()+maneuverCounts.getC();
                    }
                }
            }
        }
        return maneuverLossInMetersSum == null ? null : maneuverLossInMetersSum / count;
    }

    private Double getTotalNumberOfTacks(LeaderboardEntryDTO row) {
        Double totalNumberOfTacks = null;
        if (row != null && row.legDetails != null) {
            for (LegEntryDTO legDetail : row.legDetails) {
                if (legDetail != null) {
                    if (legDetail.numberOfTacks != null) {
                        if (totalNumberOfTacks == null) {
                            totalNumberOfTacks = (double) legDetail.numberOfTacks;
                        } else {
                            totalNumberOfTacks += (double) legDetail.numberOfTacks;
                        }
                    }
                }
            }
        }
        return totalNumberOfTacks;
    }

    private Double getTotalNumberOfJibes(LeaderboardEntryDTO row) {
        Double totalNumberOfJibes = null;
        if (row != null && row.legDetails != null) {
            for (LegEntryDTO legDetail : row.legDetails) {
                if (legDetail != null) {
                    if (legDetail.numberOfJibes != null) {
                        if (totalNumberOfJibes == null) {
                            totalNumberOfJibes = (double) legDetail.numberOfJibes;
                        } else {
                            totalNumberOfJibes += (double) legDetail.numberOfJibes;
                        }
                    }
                }
            }
        }
        return totalNumberOfJibes;
    }

    private Double getTotalNumberOfPenaltyCircles(LeaderboardEntryDTO row) {
        Double totalNumberOfPnaltyCicles = null;
        if (row != null && row.legDetails != null) {
            for (LegEntryDTO legDetail : row.legDetails) {
                if (legDetail != null) {
                    if (legDetail.numberOfPenaltyCircles != null) {
                        if (totalNumberOfPnaltyCicles == null) {
                            totalNumberOfPnaltyCicles = (double) legDetail.numberOfPenaltyCircles;
                        } else {
                            totalNumberOfPnaltyCicles += (double) legDetail.numberOfPenaltyCircles;
                        }
                    }
                }
            }
        }
        return totalNumberOfPnaltyCicles;
    }

    private Triple<Double, Double, Double> getTotalNumberOfTacksJibesAndPenaltyCircles(LeaderboardRowDTO row) {
        LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceName());
        return getTotalNumberOfTacksJibesAndPenaltyCircles(fieldsForRace);
    }

    private Triple<Double, Double, Double> getTotalNumberOfTacksJibesAndPenaltyCircles(LeaderboardEntryDTO fieldsForRace) {
        Double totalNumberOfTacks = null;
        Double totalNumberOfJibes = null;
        Double totalNumberOfPenaltyCircles = null;
        if (fieldsForRace != null && fieldsForRace.legDetails != null) {
            for (LegEntryDTO legDetail : fieldsForRace.legDetails) {
                if (legDetail != null) {
                    if (legDetail.numberOfTacks != null) {
                        if (totalNumberOfTacks == null) {
                            totalNumberOfTacks = (double) legDetail.numberOfTacks;
                        } else {
                            totalNumberOfTacks += legDetail.numberOfTacks;
                        }
                    }
                    if (legDetail.numberOfJibes != null) {
                        if (totalNumberOfJibes == null) {
                            totalNumberOfJibes = (double) legDetail.numberOfJibes;
                        } else {
                            totalNumberOfJibes += (double) legDetail.numberOfJibes;
                        }
                    }
                    if (legDetail.numberOfPenaltyCircles != null) {
                        if (totalNumberOfPenaltyCircles == null) {
                            totalNumberOfPenaltyCircles = (double) legDetail.numberOfPenaltyCircles;
                        } else {
                            totalNumberOfPenaltyCircles += (double) legDetail.numberOfPenaltyCircles;
                        }
                    }
                }
            }
        }
        return new Triple<Double, Double, Double>(totalNumberOfTacks, totalNumberOfJibes, totalNumberOfPenaltyCircles);
    }

    private String getRaceName() {
        return raceNameProvider.getRaceColumnName();
    }

    @Override
    public Header<SafeHtml> getHeader() {
        SortableExpandableColumnHeader result = new SortableExpandableColumnHeader(
        /* title */stringMessages.maneuverTypes(),
        /* iconURL */null, getLeaderboardPanel(), this, stringMessages);
        return result;
    }

    @Override
    public InvertibleComparator<LeaderboardRowDTO> getComparator() {
        return new InvertibleComparatorAdapter<LeaderboardRowDTO>(getPreferredSortingOrder().isAscending()) {
            @Override
            public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
                Double val1 = getDoubleValue(o1);
                Double val2 = getDoubleValue(o2);
                return val1 == null ? val2 == null ? 0 : isAscending() ? 1 : -1 : val2 == null ? isAscending() ? -1 : 1 : val1
                        .compareTo(val2);
            }
        };
    }

    @Override
    public String getValue(LeaderboardRowDTO object) {
        Double result = getDoubleValue(object);
        if (result == null) {
            return "";
        } else {
            Integer intResult = ((int) (double) result);
            return intResult.toString();
        }
    }

    @Override
    public void render(Context context, LeaderboardRowDTO row, SafeHtmlBuilder sb) {
        minmaxRenderer.render(context, row, getTitle(row), sb);
    }

    /**
     * Computes a tool-tip text to add to the table cell's content as rendered by
     * {@link #render(Context, LeaderboardRowDTO, SafeHtmlBuilder)}.
     * 
     * @return This default implementation returns <code>null</code> for no tool tip / title
     */
    protected String getTitle(LeaderboardRowDTO row) {
        return null;
    }

    public Double getDoubleValue(LeaderboardRowDTO row) {
        Double result = null;
        Triple<Double, Double, Double> tacksJibesAndPenalties = getTotalNumberOfTacksJibesAndPenaltyCircles(row);
        Double totalNumberOfTacks = tacksJibesAndPenalties.getA();
        Double totalNumberOfJibes = tacksJibesAndPenalties.getB();
        Double totalNumberOfPenaltyCircles = tacksJibesAndPenalties.getC();
        if (totalNumberOfTacks != null) {
            result = (double) totalNumberOfTacks;
        }
        if (totalNumberOfJibes != null) {
            if (result == null) {
                result = (double) totalNumberOfJibes;
            } else {
                result += (double) totalNumberOfJibes;
            }
        }
        if (totalNumberOfPenaltyCircles != null) {
            if (result == null) {
                result = (double) totalNumberOfPenaltyCircles;
            } else {
                result += (double) totalNumberOfPenaltyCircles;
            }
        }
        return result;
    }

    @Override
    protected void updateMinMax(LeaderboardDTO leaderboard) {
        minmaxRenderer.updateMinMax(leaderboard.rows.values());
    }

    @Override
    protected Map<DetailType, SortableColumn<LeaderboardRowDTO, ?>> getDetailColumnMap(
            LeaderboardPanel leaderboardPanel, StringMessages stringMessages, String detailHeaderStyle,
            String detailColumnStyle) {
        Map<DetailType, SortableColumn<LeaderboardRowDTO, ?>> result = new HashMap<DetailType, SortableColumn<LeaderboardRowDTO, ?>>();
        result.put(DetailType.TACK, new FormattedDoubleLegDetailColumn(stringMessages.tack(), "", new NumberOfTacks(),
                DetailType.TACK.getPrecision(), DetailType.TACK.getDefaultSortingOrder(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.JIBE, new FormattedDoubleLegDetailColumn(stringMessages.jibe(), "", new NumberOfJibes(),
                DetailType.JIBE.getPrecision(), DetailType.JIBE.getDefaultSortingOrder(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.PENALTY_CIRCLE,
                new FormattedDoubleLegDetailColumn(stringMessages.penaltyCircle(), "", new NumberOfPenaltyCircles(),
                        DetailType.PENALTY_CIRCLE.getPrecision(), DetailType.PENALTY_CIRCLE.getDefaultSortingOrder(),
                        detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.AVERAGE_MANEUVER_LOSS_IN_METERS,
                new FormattedDoubleLegDetailColumn(stringMessages.averageManeuverLossInMeters(),
                        stringMessages.distanceInMetersUnit(), new AverageManeuverLossInMeters(),
                        DetailType.AVERAGE_MANEUVER_LOSS_IN_METERS.getPrecision(), DetailType.AVERAGE_MANEUVER_LOSS_IN_METERS.getDefaultSortingOrder(),
                        detailHeaderStyle, detailColumnStyle));
        return result;
    }

    public static DetailType[] getAvailableManeuverDetailColumnTypes() {
        return new DetailType[] { DetailType.TACK, DetailType.JIBE, DetailType.PENALTY_CIRCLE };
    }

    @Override
    public String getColumnStyle() {
        return columnStyle;
    }

    @Override
    public String getHeaderStyle() {
        return headerStyle;
    }

    @Override
    public String getStringValueToRender(LeaderboardRowDTO object) {
        String result = getValue(object);
        if (!result.equals("")) {
            return getValue(object);
        } else {
            return null;
        }
    }
}
