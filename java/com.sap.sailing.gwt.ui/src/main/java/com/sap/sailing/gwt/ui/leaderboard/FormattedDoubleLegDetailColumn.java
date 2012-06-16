package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;

public class FormattedDoubleLegDetailColumn extends LegDetailColumn<Double, String> implements HasStringAndDoubleValue {
    private final NumberFormat formatter;
    private final MinMaxRenderer minMaxRenderer;

    public FormattedDoubleLegDetailColumn(String title, String unit,
            com.sap.sailing.gwt.ui.leaderboard.LegDetailColumn.LegDetailField<Double> field, int decimals,
            CellTable<LeaderboardRowDTO> leaderboardTable, String headerStyle, String columnStyle) {
        super(title, unit, field, new TextCell(), leaderboardTable, headerStyle, columnStyle);
        formatter = NumberFormatterFactory.getDecimalFormat(decimals);
        this.minMaxRenderer = new MinMaxRenderer(this, getComparator());
    }

    protected MinMaxRenderer getMinMaxRenderer() {
        return minMaxRenderer;
    }

    @Override
    protected void updateMinMax(LeaderboardDTO leaderboard) {
        getMinMaxRenderer().updateMinMax(leaderboard.rows.values());
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
        if (!value.isEmpty() & value != null) {
            return getValue(object);
        }
        return null;
    }

}
