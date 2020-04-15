package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Collection;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;

public class FormattedDoubleLeaderboardRowDTODetailTypeColumn extends DetailTypeColumn<Double, String, LeaderboardRowDTO>
        implements HasStringAndDoubleValue<LeaderboardRowDTO> {
    private final NumberFormat formatter;
    private final MinMaxRenderer<LeaderboardRowDTO> minMaxRenderer;
    
    /**
     * Creates a new column for the given {@link DetailType}. Have a look at
     * {@link DetailTypeFormatter#getUnit(DetailType)}, to see if the given type is supported.
     */
    public FormattedDoubleLeaderboardRowDTODetailTypeColumn(DetailType detailType, DataExtractor<Double,LeaderboardRowDTO> field, String headerStyle,
            String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(detailType, field, new TextCell(), headerStyle, columnStyle, displayedLeaderboardRowsProvider);
        formatter = createNumberFormatter(detailType);
        this.minMaxRenderer = createMinMaxRenderer();
    }
    
    protected MinMaxRenderer<LeaderboardRowDTO> createMinMaxRenderer() {
        return new MinMaxRenderer<LeaderboardRowDTO>(this, getComparator());
    }

    private NumberFormat createNumberFormatter(DetailType detailType) {
        return NumberFormatterFactory.getDecimalFormat(detailType.getPrecision());
    }
    
    protected MinMaxRenderer<LeaderboardRowDTO> getMinMaxRenderer() {
        return minMaxRenderer;
    }

    @Override
    public void updateMinMax() {
        Collection<LeaderboardRowDTO> data = getDisplayedLeaderboardRowsProvider().getRowsToDisplay().values();
        getMinMaxRenderer().updateMinMax(data);
    }

    protected NumberFormat getFormatter() {
        return formatter;
    }

    /**
     * Computes the string representation of the value to be displayed in the table. Note that it's not the resulting
     * string used for comparisons with the {@link #getComparator() comparator} but the sortable value extracted using
     * {@link #getFieldValue(LeaderboardRowDTO)}.
     */
    @Override
    public String getValue(LeaderboardRowDTO row) {
        Double fieldValue = getFieldValue(row);
        String result = "";
        if (fieldValue != null) {
            result = getFormatter().format(fieldValue);
        }
        return result;
    }
    
    @Override
    public Double getDoubleValue(LeaderboardRowDTO row) {
        return getFieldValue(row);
    }

    @Override
    public void render(Context context, LeaderboardRowDTO row, SafeHtmlBuilder sb) {
        getMinMaxRenderer().render(context, row, getTitle(row), sb);
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
    public String getStringValueToRender(LeaderboardRowDTO object) {
        String value = getValue(object);
        if (value != null && !value.isEmpty()) {
            return getValue(object);
        }
        return null;
    }
}