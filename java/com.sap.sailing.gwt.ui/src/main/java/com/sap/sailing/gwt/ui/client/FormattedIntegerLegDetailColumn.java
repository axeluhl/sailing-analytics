package com.sap.sailing.gwt.ui.client;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public class FormattedIntegerLegDetailColumn extends LegDetailColumn<Integer, String> {
    private final NumberFormat formatter;
    
    public FormattedIntegerLegDetailColumn(String title, String unit,
            com.sap.sailing.gwt.ui.client.LegDetailColumn.LegDetailField<Integer> field, int decimals,
            CellTable<LeaderboardRowDAO> leaderboardTable, String headerStyle, String columnStyle) {
        super(title, unit, field, new TextCell(), leaderboardTable, headerStyle, columnStyle);
        StringBuilder patternBuilder = new StringBuilder("0");
        if (decimals > 0) {
            patternBuilder.append('.');
        }
        for (int i=0; i<decimals; i++) {
            patternBuilder.append('0');
        }
        formatter = NumberFormat.getFormat(patternBuilder.toString());
    }
    
    protected NumberFormat getFormatter() {
        return formatter;
    }

    /**
     * Computes the string representation of the value to be displayed in the table. Note that it's not the
     * resulting string used for comparisons with the {@link #getComparator() comparator} but the sortable
     * value extracted using {@link #getFieldValue(LeaderboardRowDAO)}.
     */
    @Override
    public String getValue(LeaderboardRowDAO row) {
        Integer fieldValue = getFieldValue(row);
        String result = "";
        if (fieldValue != null) {
            result = getFormatter().format(fieldValue);
        }
        return result;
    }

    @Override
    public void render(Context context, LeaderboardRowDAO row, SafeHtmlBuilder sb) {
        int percent = getPercentage(row);
        String title = getTitle(row);
        sb.appendHtmlConstant("<div "+(title==null?"":"title=\""+title+"\" ")+"style=\"left: 0px; background-image: url(/images/greyBar.png); "+
        " background-position: left; background-repeat: no-repeat; background-size: "+
                percent+"% 25px; \">").
        appendEscaped(getValue(row)).appendHtmlConstant("</div>");
    }

    /**
     * Computes a tool-tip text to add to the table cell's content as rendered by {@link #render(Context, LeaderboardRowDAO, SafeHtmlBuilder)}.
     * 
     * @return This default implementation returns <code>null</code> for no tool tip / title
     */
    protected String getTitle(LeaderboardRowDAO row) {
        return null;
    }

    private int getPercentage(LeaderboardRowDAO row) {
        Integer value = getFieldValue(row);
        int percentage = 0;
        if (value != null && getMinimum() != null && getMaximum() != null) {
            int minBarLength = Math.abs(getMinimum()) < 0.01 ? 0 : 10;
            percentage = (int) (minBarLength+(100.-minBarLength)*(value-getMinimum())/(getMaximum()-getMinimum()));
        }
        return percentage;
    }

}
