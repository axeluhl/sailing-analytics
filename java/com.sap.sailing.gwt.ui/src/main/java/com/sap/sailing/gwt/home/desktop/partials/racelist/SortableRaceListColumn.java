package com.sap.sailing.gwt.home.desktop.partials.racelist;

import java.util.List;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;
import com.sap.sse.common.InvertibleComparator;
import com.sap.sse.common.SortingOrder;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.celltable.SortableColumn;

public abstract class SortableRaceListColumn<T extends RaceMetadataDTO<?>, C> extends SortableColumn<T, C> {
    
    enum ColumnVisibility {
        ALWAYS {
            @Override
            protected String getAdditionalStyle() {
                return "";
            }
        }, LARGE {
            @Override
            protected String getAdditionalStyle() {
                return SharedResources.INSTANCE.mediaCss().showonlarge();
            }
        }, MEDIUM {
            @Override
            protected String getAdditionalStyle() {
                return SharedResources.INSTANCE.mediaCss().hideonsmall();
            }
        };
        protected abstract String getAdditionalStyle();
    }
    
    private final Header<?> header;
    private final RaceListColumnComparator<T, ?> comparator;
    private boolean showDetails = true;
    private ColumnVisibility columnVisibility = ColumnVisibility.ALWAYS;

    protected SortableRaceListColumn(String headerText, Cell<C> cell, RaceListColumnComparator<T, ?> comparator) {
        this(new WrappedTextHeader(headerText), cell, comparator);
    }
    
    protected SortableRaceListColumn(String headerText, Cell<C> cell, RaceListColumnComparator<T, ?> comparator, SortingOrder preferredSortingOrder) {
        this(new WrappedTextHeader(headerText), cell, comparator, preferredSortingOrder);
    }
    
    private SortableRaceListColumn(Header<?> header, Cell<C> cell, RaceListColumnComparator<T, ?> comparator) {
        this(header, cell, comparator, SortingOrder.ASCENDING);
    }
    
    protected SortableRaceListColumn(Header<?> header, Cell<C> cell, RaceListColumnComparator<T, ?> comparator, SortingOrder preferredSortingOrder) {
        super(cell, preferredSortingOrder);
        this.header = header;
        this.comparator = comparator;
    }
    
    public void setRacesInNaturalOrder(List<T> racesInNaturalOrder) {
        if (this.comparator != null) {
            this.comparator.setRacesInNaturalOrder(racesInNaturalOrder);
        }
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
    
    boolean isShowDetails() {
        return showDetails;
    }

    void setColumnVisibility(ColumnVisibility columnVisibility) {
        this.columnVisibility = columnVisibility;
    }
    
    void updateHeaderAndCellStyles() {
        if (header != null) {
            header.setHeaderStyleNames(getCurrentStyle(getHeaderStyle()));
        }
        setCellStyleNames(getCurrentStyle(getColumnStyle()));
    }
    
    protected final String getStyleNamesString(String... styleNames) {
        return Util.join(" ", styleNames);
    }
    
    private String getCurrentStyle(String baseStyle) {
        return getStyleNamesString(baseStyle, isShowDetails() ? columnVisibility.getAdditionalStyle()
                : RaceListResources.INSTANCE.css().racesListHideColumn());
    }
        
    private static class WrappedTextHeader extends SafeHtmlHeader {
        public WrappedTextHeader(String headerText) {
            super(SafeHtmlUtils.fromTrustedString("<div>" + headerText + "</div>"));
        }
    }
}
