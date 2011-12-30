package com.sap.sailing.gwt.ui.client;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sailing.gwt.ui.client.LegDetailColumn.LegDetailField;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardEntryDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;
import com.sap.sailing.gwt.ui.shared.LegEntryDAO;
import com.sap.sailing.server.api.DetailType;
import com.sap.sailing.domain.common.Util.Triple;

public class ManeuverCountRaceColumn extends ExpandableSortableColumn<String> implements HasStringAndDoubleValue {

    private final StringConstants stringConstants;
    private final RaceNameProvider raceNameProvider;

    private final String headerStyle;
    private final String columnStyle;

    private final MinMaxRenderer minmaxRenderer;

    private abstract class AbstractManeuverDetailField<T extends Comparable<?>> implements LegDetailField<T> {
        public T get(LeaderboardRowDAO row) {
            LeaderboardEntryDAO fieldsForRace = row.fieldsByRaceName.get(getRaceName());
            if (fieldsForRace == null) {
                return null;
            } else {
                T value = getFromNonNullEntry(fieldsForRace);
                return value;
            }
        }

        protected abstract T getFromNonNullEntry(LeaderboardEntryDAO entry);
    }

    private class NumberOfTacks extends AbstractManeuverDetailField<Double> {

        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDAO entry) {
            return ManeuverCountRaceColumn.this.getTotalNumberOfTacks(entry);
        }
    }

    private class NumberOfJibes extends AbstractManeuverDetailField<Double> {

        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDAO entry) {
            return ManeuverCountRaceColumn.this.getTotalNumberOfJibes(entry);
        }
    }

    private class NumberOfPenaltyCircles extends AbstractManeuverDetailField<Double> {

        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDAO entry) {
            return ManeuverCountRaceColumn.this.getTotalNumberOfPenaltyCircles(entry);
        }
    }

    public ManeuverCountRaceColumn(LeaderboardPanel leaderboardPanel, RaceNameProvider raceNameProvider,
            StringConstants stringConstants, List<DetailType> maneuverDetailSelection, String headerStyle,
            String columnStylee, String detailHeaderStyle, String detailColumnStyle) {
        super(leaderboardPanel, /* expandable */true /* all legs have details */, new TextCell(), stringConstants,
                detailHeaderStyle, detailColumnStyle, maneuverDetailSelection);
        setHorizontalAlignment(ALIGN_CENTER);
        this.stringConstants = stringConstants;
        this.raceNameProvider = raceNameProvider;
        this.headerStyle = headerStyle;
        this.columnStyle = columnStylee;
        this.minmaxRenderer = new MinMaxRenderer(this, getComparator());
    }

    private Double getTotalNumberOfTacks(LeaderboardEntryDAO row) {
        Double totalNumberOfTacks = null;
        if (row != null && row.legDetails != null) {
            for (LegEntryDAO legDetail : row.legDetails) {
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

    private Double getTotalNumberOfJibes(LeaderboardEntryDAO row) {
        Double totalNumberOfJibes = null;
        if (row != null && row.legDetails != null) {
            for (LegEntryDAO legDetail : row.legDetails) {
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

    private Double getTotalNumberOfPenaltyCircles(LeaderboardEntryDAO row) {
        Double totalNumberOfPnaltyCicles = null;
        if (row != null && row.legDetails != null) {
            for (LegEntryDAO legDetail : row.legDetails) {
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

    private Triple<Double, Double, Double> getTotalNumberOfTacksJibesAndPenaltyCircles(LeaderboardRowDAO row) {
        Double totalNumberOfTacks = null;
        Double totalNumberOfJibes = null;
        Double totalNumberOfPenaltyCircles = null;
        LeaderboardEntryDAO fieldsForRace = row.fieldsByRaceName.get(getRaceName());
        if (fieldsForRace != null && fieldsForRace.legDetails != null) {
            for (LegEntryDAO legDetail : fieldsForRace.legDetails) {
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
        return raceNameProvider.getRaceName();
    }

    @Override
    public Header<SafeHtml> getHeader() {
        SortableExpandableColumnHeader result = new SortableExpandableColumnHeader(
        /* title */stringConstants.maneuverTypes(),
        /* iconURL */null, getLeaderboardPanel(), this, stringConstants);
        return result;
    }

    @Override
    public Comparator<LeaderboardRowDAO> getComparator() {
        return new Comparator<LeaderboardRowDAO>() {
            @Override
            public int compare(LeaderboardRowDAO o1, LeaderboardRowDAO o2) {
                boolean ascending = isSortedAscendingForThisColumn(getLeaderboardPanel().getLeaderboardTable());
                Double val1 = getDoubleValue(o1);
                Double val2 = getDoubleValue(o2);
                return val1 == null ? val2 == null ? 0 : ascending ? 1 : -1 : val2 == null ? ascending ? -1 : 1 : val1
                        .compareTo(val2);
            }
        };
    }

    @Override
    public String getValue(LeaderboardRowDAO object) {
        Double result = getDoubleValue(object);
        if (result == null) {
            return "";
        } else {
            Integer intResult = ((int) (double) result);
            return intResult.toString();
        }
    }

    @Override
    public void render(Context context, LeaderboardRowDAO row, SafeHtmlBuilder sb) {
        minmaxRenderer.render(context, row, getTitle(row), sb);
    }

    /**
     * Computes a tool-tip text to add to the table cell's content as rendered by
     * {@link #render(Context, LeaderboardRowDAO, SafeHtmlBuilder)}.
     * 
     * @return This default implementation returns <code>null</code> for no tool tip / title
     */
    protected String getTitle(LeaderboardRowDAO row) {
        return null;
    }

    public Double getDoubleValue(LeaderboardRowDAO row) {
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
    protected void updateMinMax(LeaderboardDAO leaderboard) {
        minmaxRenderer.updateMinMax(leaderboard.rows.values());
    }

    @Override
    protected Map<DetailType, SortableColumn<LeaderboardRowDAO, ?>> getDetailColumnMap(
            LeaderboardPanel leaderboardPanel, StringConstants stringConstants, String detailHeaderStyle,
            String detailColumnStyle) {
        Map<DetailType, SortableColumn<LeaderboardRowDAO, ?>> result = new HashMap<DetailType, SortableColumn<LeaderboardRowDAO, ?>>();
        result.put(DetailType.TACK, new FormattedDoubleLegDetailColumn(stringConstants.tack(), "", new NumberOfTacks(),
                0, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.JIBE, new FormattedDoubleLegDetailColumn(stringConstants.jibe(), "", new NumberOfJibes(),
                0, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle, detailColumnStyle));
        result.put(DetailType.PENALTY_CIRCLE, new FormattedDoubleLegDetailColumn(stringConstants.penaltyCircle(), "",
                new NumberOfPenaltyCircles(), 0, getLeaderboardPanel().getLeaderboardTable(), detailHeaderStyle,
                detailColumnStyle));
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
    public String getStringValueToRender(LeaderboardRowDAO object) {
        String result = getValue(object);
        if (!result.equals("")) {
            return getValue(object);
        } else {
            return null;
        }
    }
}
