package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;

public abstract class LegDetailColumn<FieldType extends Comparable<?>, RenderingType> extends
        SortableColumn<LeaderboardRowDTO, RenderingType> {
    private final String title;
    private final LegDetailField<FieldType> field;
    private final String headerStyle;
    private final String columnStyle;
    private final String unit;
    
    private SafeHtmlHeader header;

    public interface LegDetailField<T extends Comparable<?>> {
        T get(LeaderboardRowDTO row);
    }

    protected LegDetailColumn(String title, String unit, LegDetailField<FieldType> field, Cell<RenderingType> cell,
            SortingOrder preferredSortingOrder, String headerStyle, String columnStyle) {
        super(cell, preferredSortingOrder);
        setHorizontalAlignment(ALIGN_CENTER);
        this.title = title;
        this.unit = unit;
        this.field = field;
        this.headerStyle = headerStyle;
        this.columnStyle = columnStyle;
        InitializeHeader();
    }

    private void InitializeHeader() {
    	//Code to add a tooltip to the title. Do NOT delete. This is saved to use it, after some refactorings are done.
//      SafeHtmlBuilder builder = new SafeHtmlBuilder().appendHtmlConstant("<div title=\"Tooltip text.\">");
//      builder.appendEscaped(title).appendHtmlConstant("<br>");
    	
    	SafeHtmlBuilder builder = new SafeHtmlBuilder().appendEscaped(title).appendHtmlConstant("<br>");
        if (unit == null) {
            builder.appendHtmlConstant("&nbsp;");
        } else {
            builder.appendEscaped(unit);
        }
        
        header = new SafeHtmlHeader(builder.toSafeHtml());
	}

	protected String getTitle() {
        return title;
    }

    @Override
    public String getHeaderStyle() {
        return headerStyle;
    }

    protected LegDetailField<FieldType> getField() {
        return field;
    }

    @Override
    public String getColumnStyle() {
        return columnStyle;
    }

    @Override
    public InvertibleComparator<LeaderboardRowDTO> getComparator() {
        return new InvertibleComparatorAdapter<LeaderboardRowDTO>(getPreferredSortingOrder().isAscending()) {
            @Override
            public int compare(LeaderboardRowDTO o1, LeaderboardRowDTO o2) {
                try {
                    @SuppressWarnings("unchecked")
                    Comparable<FieldType> value1 = (Comparable<FieldType>) getFieldValue(o1);
                    FieldType value2 = getFieldValue(o2);
                    return value1 == null ? value2 == null ? 0 : isAscending() ? 1 : -1 : value2 == null ? isAscending() ? -1
                            : 1 : value1.compareTo(value2);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public Header<?> getHeader() {
        return header;
    }

    /**
     * Extracts the sortable field value from the row. The resulting value is subject to comparison with the
     * {@link #getComparator() comparator}. This default implementation uses the {@link #getField()} to obtain the logic
     * for extracting a comparable value from the <code>row</code>.
     */
    protected FieldType getFieldValue(LeaderboardRowDTO row) {
        return getField().get(row);
    }

}
