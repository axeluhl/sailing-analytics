package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sse.common.InvertibleComparator;
import com.sap.sse.common.impl.InvertibleComparatorAdapter;

public abstract class DetailTypeColumn<FieldType extends Comparable<?>, RenderingType> extends
        LeaderboardSortableColumnWithMinMax<LeaderboardRowDTO, RenderingType> {
    
    private static final String HtmlConstantToInlineHeadersWithAndWithoutUnit = "&nbsp;";
    
    private final String title;
    private final LegDetailField<FieldType> field;
    private final String headerStyle;
    private final String columnStyle;
    private final String unit;
    private final String tooltip;
    
    public interface LegDetailField<T extends Comparable<?>> {
        T get(LeaderboardRowDTO row);
    }

    protected DetailTypeColumn(DetailType detailType, LegDetailField<FieldType> field, Cell<RenderingType> cell,
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
    protected FieldType getFieldValue(LeaderboardRowDTO row) {
        return getField().get(row);
    }

}
