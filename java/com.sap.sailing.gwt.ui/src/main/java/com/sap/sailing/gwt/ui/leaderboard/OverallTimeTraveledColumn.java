package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.AbstractSortableColumnWithMinMax;
import com.sap.sailing.gwt.ui.leaderboard.DetailTypeColumn.DataExtractor;

public class OverallTimeTraveledColumn extends ExpandableSortableColumn<String> implements HasStringAndDoubleValue<LeaderboardRowDTO> {
    
    private static final DetailType DETAIL_TYPE = DetailType.OVERALL_TOTAL_TIME_SAILED_IN_SECONDS;

    private StringMessages stringMessages;

    private String columnStyle;
    private String headerStyle;
    private MinMaxRenderer<LeaderboardRowDTO> minmaxRenderer;

    public OverallTimeTraveledColumn(LeaderboardPanel<?> leaderboardPanel, StringMessages stringMessages, String headerStyle, String columnStyle, String detailHeaderStyle,
            String detailColumnStyle) {
        super(leaderboardPanel, /* expandable */true, new TextCell(), DETAIL_TYPE.getDefaultSortingOrder(), 
                stringMessages, detailHeaderStyle, detailColumnStyle,
                Arrays.asList(DetailType.TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS, DetailType.TOTAL_TIME_SAILED_UPWIND_IN_SECONDS, DetailType.TOTAL_TIME_SAILED_REACHING_IN_SECONDS), leaderboardPanel);
        setHorizontalAlignment(ALIGN_CENTER);
        this.stringMessages = stringMessages;
        this.columnStyle = columnStyle;
        this.headerStyle = headerStyle;
        this.minmaxRenderer = new MinMaxRenderer<LeaderboardRowDTO>(this, getComparator());
    }

    @Override
    public Double getDoubleValue(LeaderboardRowDTO row) {
        return row.totalTimeSailedInSeconds;
    }
    
    @Override
    protected Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> getDetailColumnMap(
            LeaderboardPanel<?> leaderboardPanel, StringMessages stringConstants, String detailHeaderStyle,
            String detailColumnStyle) {
        Map<DetailType, AbstractSortableColumnWithMinMax<LeaderboardRowDTO, ?>> result = new HashMap<>();

        result.put(DetailType.TOTAL_TIME_SAILED_UPWIND_IN_SECONDS,
                new TotalTimeColumn(DetailType.TOTAL_TIME_SAILED_UPWIND_IN_SECONDS, new TotalTimeSailedUpwindInSeconds(),
                        detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS,
                new TotalTimeColumn(DetailType.TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS, new TotalTimeSailedDownwindInSeconds(),
                        detailHeaderStyle, detailColumnStyle, leaderboardPanel));
        result.put(DetailType.TOTAL_TIME_SAILED_REACHING_IN_SECONDS,
                new TotalTimeColumn(DetailType.TOTAL_TIME_SAILED_REACHING_IN_SECONDS, new TotalTimeSailedReachingInSeconds(),
                        detailHeaderStyle, detailColumnStyle, leaderboardPanel));

        return result;
    }
    
    private static class TotalTimeSailedDownwindInSeconds implements DataExtractor<Double, LeaderboardRowDTO> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            return row.totalTimeSailedDownwindInSeconds;
        }
    }
    
    private static class TotalTimeSailedReachingInSeconds implements DataExtractor<Double,LeaderboardRowDTO> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            return row.totalTimeSailedReachingInSeconds;
        }
    }

    private static class TotalTimeSailedUpwindInSeconds implements DataExtractor<Double,LeaderboardRowDTO> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            return row.totalTimeSailedUpwindInSeconds;
        }
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
    public SortableExpandableColumnHeader getHeader() {
        SortableExpandableColumnHeader result = new SortableExpandableColumnHeader(
        /* title */DetailTypeFormatter.format(DETAIL_TYPE), /* tooltip */ DetailTypeFormatter.getTooltip(DETAIL_TYPE),
        DetailTypeFormatter.getUnit(DETAIL_TYPE), /* iconURL */null, getLeaderboardPanel(), this, stringMessages);
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
    public String getStringValueToRender(LeaderboardRowDTO row) {
        Double timeInSeconds = getDoubleValue(row);
        String result;
        if (timeInSeconds == null) {
            result = null;
        } else {
            int hh = (int) (timeInSeconds / 3600);
            int mm = (int) ((timeInSeconds - 3600 * hh) / 60);
            int ss = (int) (timeInSeconds - 3600 * hh - 60 * mm);
            NumberFormat numberFormat = NumberFormatterFactory.getDecimalFormat(2, 0);
            result = "" + numberFormat.format(hh) + ":" + numberFormat.format(mm) + ":" + numberFormat.format(ss);
        }
        return result;
    }

    @Override
    public void render(Context context, LeaderboardRowDTO row, SafeHtmlBuilder sb) {
        minmaxRenderer.render(context, row, getTitle(row), sb);
    }

    @Override
    public void updateMinMax() {
        minmaxRenderer.updateMinMax(getDisplayedLeaderboardRowsProvider().getRowsToDisplay().values());
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

    @Override
    public String getColumnStyle() {
        return columnStyle;
    }

    @Override
    public String getHeaderStyle() {
        return headerStyle;
    }

}
