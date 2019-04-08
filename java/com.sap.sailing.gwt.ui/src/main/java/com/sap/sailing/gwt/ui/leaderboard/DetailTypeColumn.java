package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;

public abstract class DetailTypeColumn<FieldType extends Comparable<?>, RenderingType, T> extends
        LeaderboardSortableColumnWithMinMax<T, RenderingType> {
    
    private static final String HtmlConstantToInlineHeadersWithAndWithoutUnit = "&nbsp;";
    
    private final String title;
    private final DataExtractor<FieldType, T> field;
    private final String headerStyle;
    private final String columnStyle;
    private final String unit;
    private final String tooltip;
    
    public interface DataExtractor<T extends Comparable<?>, Z> {
        T get(Z row);
    }

    protected DetailTypeColumn(DetailType detailType, DataExtractor<FieldType, T> field, Cell<RenderingType> cell,
            String headerStyle, String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(cell, detailType.getDefaultSortingOrder(), displayedLeaderboardRowsProvider);
        setHorizontalAlignment(ALIGN_CENTER);
        this.title = DetailTypeFormatter.format(detailType);
        this.unit = DetailTypeFormatter.getUnit(detailType).isEmpty() ? "" : "[" + DetailTypeFormatter.getUnit(detailType) + "]";
        tooltip = DetailTypeFormatter.getTooltip(detailType);
        this.field = field;
        this.headerStyle = headerStyle;
        this.columnStyle = columnStyle;
    }

    protected String getTitle() {
        return title;
    }

    @Override
    public String getHeaderStyle() {
        return headerStyle;
    }

    protected DataExtractor<FieldType, T> getField() {
        return field;
    }

    @Override
    public String getColumnStyle() {
        return columnStyle;
    }

    @Override
    public InvertibleComparator<T> getComparator() {
        return new InvertibleComparatorAdapter<T>(getPreferredSortingOrder().isAscending()) {
            @Override
            public int compare(T o1, T o2) {
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
        SafeHtmlBuilder titleBuilder = new SafeHtmlBuilder().appendEscaped(title).appendHtmlConstant("<br>");
        if (unit == null || unit.isEmpty()) {
            titleBuilder.appendHtmlConstant(HtmlConstantToInlineHeadersWithAndWithoutUnit);
        } else {
            titleBuilder.appendEscaped(unit);
        }
        return new SafeHtmlHeaderWithTooltip(titleBuilder.toSafeHtml(), tooltip);
    }

    /**
     * Extracts the sortable field value from the row. The resulting value is subject to comparison with the
     * {@link #getComparator() comparator}. This default implementation uses the {@link #getField()} to obtain the logic
     * for extracting a comparable value from the <code>row</code>.
     */
    protected FieldType getFieldValue(T row) {
        return getField().get(row);
    }

}
