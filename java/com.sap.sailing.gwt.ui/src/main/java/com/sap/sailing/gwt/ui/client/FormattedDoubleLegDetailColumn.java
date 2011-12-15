package com.sap.sailing.gwt.ui.client;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public class FormattedDoubleLegDetailColumn extends LegDetailColumn<Double, String> implements HasStringAndDoubleValue {
    private final NumberFormat formatter;
    private final MinMaxRenderer minMaxRenderer;

    public FormattedDoubleLegDetailColumn(String title, String unit,
            com.sap.sailing.gwt.ui.client.LegDetailColumn.LegDetailField<Double> field, int decimals,
            CellTable<LeaderboardRowDAO> leaderboardTable, String headerStyle, String columnStyle) {
        super(title, unit, field, new TextCell(), leaderboardTable, headerStyle, columnStyle);
        StringBuilder patternBuilder = new StringBuilder("0");
        if (decimals > 0) {
            patternBuilder.append('.');
        }
        for (int i = 0; i < decimals; i++) {
            patternBuilder.append('0');
        }
        formatter = NumberFormat.getFormat(patternBuilder.toString());
        this.minMaxRenderer = new MinMaxRenderer(this, getComparator());
    }

    protected MinMaxRenderer getMinMaxRenderer() {
        return minMaxRenderer;
    }

    @Override
    protected void updateMinMax(LeaderboardDAO leaderboard) {
        getMinMaxRenderer().updateMinMax(leaderboard.rows.values());
    }

    protected NumberFormat getFormatter() {
        return formatter;
    }

    /**
     * Computes the string representation of the value to be displayed in the table. Note that it's not the resulting
     * string used for comparisons with the {@link #getComparator() comparator} but the sortable value extracted using
     * {@link #getFieldValue(LeaderboardRowDAO)}.
     */
    @Override
    public String getValue(LeaderboardRowDAO row) {
        Double fieldValue = getFieldValue(row);
        String result = "";
        if (fieldValue != null) {
            result = getFormatter().format(fieldValue);
        }
        return result;
    }
    
    @Override
    public Double getDoubleValue(LeaderboardRowDAO row) {
        return getFieldValue(row);
    }

    @Override
    public void render(Context context, LeaderboardRowDAO row, SafeHtmlBuilder sb) {
        getMinMaxRenderer().render(context, row, getTitle(row), sb);
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

    @Override
    public String getStringValueToRender(LeaderboardRowDAO object) {
        String value = getValue(object);
        if (!value.isEmpty() & value != null) {
            return getValue(object);
        }
        return null;
    }

}
