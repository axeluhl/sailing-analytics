package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources;
import com.sap.sailing.gwt.ui.client.shared.controls.SortableColumn;
import com.sap.sse.common.Util;

public abstract class SortableRaceListColumn<T, C> extends SortableColumn<T, C> {
    
    private final Header<?> header;
    private final InvertibleComparator<T> comparator;
    
    private boolean showDetails = true;

    protected SortableRaceListColumn(String headerText, Cell<C> cell, InvertibleComparator<T> comparator) {
        this(new WrappedTextHeader(headerText), cell, comparator);
    }
    
    protected SortableRaceListColumn(String headerText, Cell<C> cell, InvertibleComparator<T> comparator, SortingOrder preferredSortingOrder) {
        this(new WrappedTextHeader(headerText), cell, comparator, preferredSortingOrder);
    }
    
    protected SortableRaceListColumn(Header<?> header, Cell<C> cell, InvertibleComparator<T> comparator) {
        this(header, cell, comparator, SortingOrder.ASCENDING);
    }
    
    protected SortableRaceListColumn(Header<?> header, Cell<C> cell, InvertibleComparator<T> comparator, SortingOrder preferredSortingOrder) {
        super(cell, preferredSortingOrder);
        this.header = header;
        this.comparator = comparator;
    }

    @Override
    public final InvertibleComparator<T> getComparator() {
        return comparator;
    }

    @Override
    public final Header<?> getHeader() {
        return header;
    }
    
    public void setShowDetails(boolean showDetails) {
        this.showDetails = showDetails;
    }
    
    public boolean isShowDetails() {
        return showDetails;
    }
    
    protected final String getStyleNamesString(String... styleNames) {
        return Util.join(" ", styleNames);
    }
    
    private static final String COLUMN_HIDDEN_STYLE = RacesListLiveResources.INSTANCE.css().racesListHideColumn();
    
    protected final String getCurrentHeaderStyle() {
        return getShowOrHiddenStyle(getHeaderStyle());
    }
    
    protected final String getCurrentColumnStyle() {
        return getShowOrHiddenStyle(getColumnStyle());
    }
    
    private String getShowOrHiddenStyle(String showStyle) {
        showStyle = showStyle == null ? "" : showStyle;
        return showDetails ? showStyle : getStyleNamesString(showStyle, COLUMN_HIDDEN_STYLE);
    }
    
    private static class WrappedTextHeader extends SafeHtmlHeader {
        public WrappedTextHeader(String headerText) {
            super(SafeHtmlUtils.fromTrustedString("<div>" + headerText + "</div>"));
        }
    }
}
