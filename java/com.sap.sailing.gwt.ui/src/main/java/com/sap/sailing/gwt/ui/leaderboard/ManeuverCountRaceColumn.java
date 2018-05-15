package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.LegEntryDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.DetailTypeColumn.DataExtractor;
import com.sap.sse.common.InvertibleComparator;
import com.sap.sse.common.impl.InvertibleComparatorAdapter;
import com.sap.sse.gwt.client.celltable.AbstractSortableColumnWithMinMax;

public class ManeuverCountRaceColumn extends ExpandableSortableColumn<String> implements HasStringAndDoubleValue<LeaderboardRowDTO> {
    private final StringMessages stringMessages;
    private final RaceNameProvider raceNameProvider;

    private final String headerStyle;
    private final String columnStyle;

    private final MinMaxRenderer<LeaderboardRowDTO> minmaxRenderer;

    private abstract class AbstractManeuverDetailField<T extends Comparable<?>> implements DataExtractor<T, LeaderboardRowDTO> {
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
            return ManeuverCountRaceColumn.this.getTotalNumberOfManeuvers(entry, ManeuverType.TACK);
        }
    }

    private class AverageTackLossInMeters extends AbstractManeuverDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDTO entry) {
            return ManeuverCountRaceColumn.this.getAverageManeuverLossInMeters(entry, ManeuverType.TACK);
        }
    }

    private class NumberOfJibes extends AbstractManeuverDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDTO entry) {
            return ManeuverCountRaceColumn.this.getTotalNumberOfManeuvers(entry, ManeuverType.JIBE);
        }
    }

    private class AverageJibeLossInMeters extends AbstractManeuverDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDTO entry) {
            return ManeuverCountRaceColumn.this.getAverageManeuverLossInMeters(entry, ManeuverType.JIBE);
        }
    }

    private class NumberOfPenaltyCircles extends AbstractManeuverDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDTO entry) {
            return ManeuverCountRaceColumn.this.getTotalNumberOfManeuvers(entry, ManeuverType.PENALTY_CIRCLE);
        }
    }

    private class AverageManeuverLossInMeters extends AbstractManeuverDetailField<Double> {
        @Override
        protected Double getFromNonNullEntry(LeaderboardEntryDTO entry) {
            return ManeuverCountRaceColumn.this.getAverageManeuverLossInMeters(entry, ManeuverType.TACK, ManeuverType.JIBE);
        }
    }

    public ManeuverCountRaceColumn(LeaderboardPanel<?> leaderboardPanel, RaceNameProvider raceNameProvider,
            StringMessages stringConstants, List<DetailType> maneuverDetailSelection, String headerStyle,
            String columnStylee, String detailHeaderStyle, String detailColumnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(leaderboardPanel, /* expandable */true /* all legs have details */, new TextCell(), DetailType.NUMBER_OF_MANEUVERS.getDefaultSortingOrder(), 
                stringConstants, detailHeaderStyle, detailColumnStyle, maneuverDetailSelection, displayedLeaderboardRowsProvider);
        setHorizontalAlignment(ALIGN_CENTER);
        this.stringMessages = stringConstants;
        this.raceNameProvider = raceNameProvider;
        this.headerStyle = headerStyle;
        this.columnStyle = columnStylee;
        this.minmaxRenderer = new MinMaxRenderer<LeaderboardRowDTO>(this, getComparator());
    }

    private Double getAverageManeuverLossInMeters(LeaderboardEntryDTO row, ManeuverType... maneuverTypes) {
        int count = 0;
        double totalLossInMeters = 0.0;
        if (row != null && row.legDetails != null) {
            for (LegEntryDTO legDetail : row.legDetails) {
                if (legDetail != null) {
                    if (legDetail.averageManeuverLossInMeters != null) {
                        for (ManeuverType maneuverType : maneuverTypes) {
                            final Integer maneuverCount = legDetail.numberOfManeuvers==null?null:legDetail.numberOfManeuvers.get(maneuverType);
                            if (maneuverCount != null && maneuverCount != 0) {
                                totalLossInMeters += legDetail.averageManeuverLossInMeters.get(maneuverType) * maneuverCount;
                                count += maneuverCount;
                            }
                        }
                    }
                }
            }
        }
        return count == 0 ? null : totalLossInMeters / count;
    }

    private Double getTotalNumberOfManeuvers(LeaderboardEntryDTO row, ManeuverType maneuverType) {
        Double totalNumberOfManeuvers = null;
        if (row != null && row.legDetails != null) {
            for (LegEntryDTO legDetail : row.legDetails) {
                if (legDetail != null) {
                    if (legDetail.numberOfManeuvers != null && legDetail.numberOfManeuvers.get(maneuverType) != null) {
                        if (totalNumberOfManeuvers == null) {
                            totalNumberOfManeuvers = (double) legDetail.numberOfManeuvers.get(maneuverType);
                        } else {
                            totalNumberOfManeuvers += (double) legDetail.numberOfManeuvers.get(maneuverType);
                        }
                    }
                }
            }
        }
        return totalNumberOfManeuvers;
    }

    private Map<ManeuverType, Double> getTotalNumberOfTacksJibesAndPenaltyCircles(LeaderboardRowDTO row) {
        LeaderboardEntryDTO fieldsForRace = row.fieldsByRaceColumnName.get(getRaceName());
        return getTotalNumberOfTacksJibesAndPenaltyCircles(fieldsForRace);
    }

    private Map<ManeuverType, Double> getTotalNumberOfTacksJibesAndPenaltyCircles(LeaderboardEntryDTO fieldsForRace) {
        Map<ManeuverType, Double> totalNumberOfManeuvers = new HashMap<ManeuverType, Double>();
        for (ManeuverType maneuverType : new ManeuverType[] { ManeuverType.TACK, ManeuverType.JIBE, ManeuverType.PENALTY_CIRCLE }) {
            totalNumberOfManeuvers.put(maneuverType, 0.0);
        }
        if (fieldsForRace != null && fieldsForRace.legDetails != null) {
            for (LegEntryDTO legDetail : fieldsForRace.legDetails) {
                if (legDetail != null) {
                    for (ManeuverType maneuverType : new ManeuverType[] { ManeuverType.TACK, ManeuverType.JIBE, ManeuverType.PENALTY_CIRCLE }) {
                        if (legDetail.numberOfManeuvers != null && legDetail.numberOfManeuvers.get(maneuverType) != null) {
                            totalNumberOfManeuvers.put(maneuverType,
                                    totalNumberOfManeuvers.get(maneuverType) + (double) legDetail.numberOfManeuvers.get(maneuverType));
                        }
                    }
                }
            }
        }
        return totalNumberOfManeuvers;
    }

    private String getRaceName() {
        return raceNameProvider.getRaceColumnName();
    }

    @Override
    public SortableExpandableColumnHeader getHeader() {
        SortableExpandableColumnHeader result = new SortableExpandableColumnHeader(
        /* title */stringMessages.maneuverTypes(), /* tooltip */ stringMessages.numberOfManeuversInRaceTooltip(),
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
        Map<ManeuverType, Double> tacksJibesAndPenalties = getTotalNumberOfTacksJibesAndPenaltyCircles(row);
        for (Double maneuverCount : tacksJibesAndPenalties.values()) {
            if (maneuverCount != null) {
                if (result == null) {
                    result = maneuverCount;
                } else {
                    result += maneuverCount;
                }
            }
        }
        return result;
    }

    @Override
    public void updateMinMax() {
        minmaxRenderer.updateMinMax(getDisplayedLeaderboardRowsProvider().getRowsToDisplay().values());
    }

    @Override
    protected Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> getDetailColumnMap(
            LeaderboardPanel<?> leaderboardPanel, StringMessages stringMessages, String detailHeaderStyle,
            String detailColumnStyle) {
        Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> result = new HashMap<>();
        result.put(DetailType.TACK, new FormattedDoubleLeaderboardRowDTODetailTypeColumn(DetailType.TACK, new NumberOfTacks(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        
        result.put(DetailType.JIBE, new FormattedDoubleLeaderboardRowDTODetailTypeColumn(DetailType.JIBE, new NumberOfJibes(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        
        result.put(DetailType.PENALTY_CIRCLE,
                new FormattedDoubleLeaderboardRowDTODetailTypeColumn(DetailType.PENALTY_CIRCLE, new NumberOfPenaltyCircles(), detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        
        result.put(DetailType.AVERAGE_TACK_LOSS_IN_METERS,
                new FormattedDoubleLeaderboardRowDTODetailTypeColumn(DetailType.AVERAGE_TACK_LOSS_IN_METERS, new AverageTackLossInMeters(),
                        detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        
        result.put(DetailType.AVERAGE_JIBE_LOSS_IN_METERS,
                new FormattedDoubleLeaderboardRowDTODetailTypeColumn(DetailType.AVERAGE_JIBE_LOSS_IN_METERS, new AverageJibeLossInMeters(),
                        detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        
        result.put(DetailType.AVERAGE_MANEUVER_LOSS_IN_METERS,
                new FormattedDoubleLeaderboardRowDTODetailTypeColumn(DetailType.AVERAGE_MANEUVER_LOSS_IN_METERS, new AverageManeuverLossInMeters(),
                        detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        
        return result;
    }

    public static List<DetailType> getAvailableManeuverDetailColumnTypes() {
        return Arrays.asList(new DetailType[] { DetailType.TACK, DetailType.AVERAGE_TACK_LOSS_IN_METERS,
                DetailType.JIBE, DetailType.AVERAGE_JIBE_LOSS_IN_METERS, DetailType.PENALTY_CIRCLE,
                DetailType.AVERAGE_MANEUVER_LOSS_IN_METERS });
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
