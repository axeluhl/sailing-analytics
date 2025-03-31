package com.sap.sailing.gwt.home.desktop.partials.racelist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.common.theme.component.celltable.CleanCellTableResources;
import com.sap.sailing.gwt.common.theme.component.celltable.StyledHeaderOrFooterBuilder;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorDTO;
import com.sap.sailing.gwt.home.communication.race.RaceMetadataDTO;
import com.sap.sailing.gwt.home.communication.race.SimpleRaceMetadataDTO;
import com.sap.sailing.gwt.home.communication.race.wind.AbstractWindDTO;
import com.sap.sailing.gwt.home.desktop.partials.racelist.RaceListColumnFactory.SortableRaceListStartTimeColumn;
import com.sap.sailing.gwt.home.desktop.partials.racelist.RaceListResources.LocalCss;
import com.sap.sailing.gwt.home.desktop.places.event.EventView;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterValueChangeHandler;
import com.sap.sailing.gwt.home.shared.partials.filter.FilterValueProvider;
import com.sap.sse.common.InvertibleComparator;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.gwt.client.celltable.SortedCellTable;

public abstract class AbstractRaceList<T extends RaceMetadataDTO<? extends AbstractWindDTO>> extends Composite
        implements FilterValueProvider<SimpleCompetitorDTO>, FilterValueChangeHandler<SimpleRaceMetadataDTO> {

    private static final LocalCss CSS = RaceListResources.INSTANCE.css();

    private final SimplePanel cellTableContainer = new SimplePanel();
    
    protected final SortableRaceListColumn<T, ?> fleetCornerColumn = RaceListColumnFactory.getFleetCornerColumn();
    protected final SortableRaceListColumn<T, ?> raceNameColumn = RaceListColumnFactory.getRaceNameColumn();
    protected final SortableRaceListColumn<T, ?> fleetNameColumn = RaceListColumnFactory.getFleetNameColumn(); 
    protected final SortableRaceListStartTimeColumn<T> startTimeColumn = RaceListColumnFactory.getStartTimeColumn();
    protected final SortableRaceListColumn<T, ?> windDirectionColumn = RaceListColumnFactory.getWindDirectionColumn();
    protected final SortableRaceListColumn<T, ?> raceViewerButtonColumn;
    protected final RaceListColumnSet columnSet;
    private final SortedCellTable<T> cellTable = new SortedCellTable<T>(0, CleanCellTableResources.INSTANCE);
    private boolean tableColumnsInitialized = false;
    
    protected AbstractRaceList(EventView.Presenter presenter, RaceListColumnSet columnSet, boolean showNotTracked) {
        CSS.ensureInjected();
        this.raceViewerButtonColumn = RaceListColumnFactory.getRaceViewerButtonColumn(presenter, showNotTracked);
        this.columnSet = columnSet;
        this.cellTableContainer.setWidget(this.cellTable);
        this.initTableStyle();
        this.initWidget(cellTableContainer);
    }

    protected void setTableData(Collection<T> data) {
        this.ensureInitTableColumns();
        this.columnSet.updateColumnVisibilities();
        List<T> dataList = new ArrayList<>(data);
        for (int i = 0; i < this.cellTable.getColumnCount(); i++) {
            SortableRaceListColumn<T, ?> column = (SortableRaceListColumn<T, ?>) this.cellTable.getColumn(i);
            column.updateHeaderAndCellStyles();
            column.setRacesInNaturalOrder(dataList);
        }
        this.cellTable.setPageSize(data.size());
        this.cellTable.setList(data);
        this.restoreColumnSortInfos();
    }

    private void initTableStyle() {
        this.cellTable.addStyleName(CSS.raceslist());
        this.cellTable.setHeaderBuilder(new StyledHeaderOrFooterBuilder<T>(cellTable, false, CSS.raceslist_head()));
        this.cellTable.setRowStyles(new RowStyles<T>() {
            @Override
            public String getStyleNames(T row, int rowIndex) {
                return CSS.race();
            }
        });
    }
    
    private void ensureInitTableColumns() {
        if (tableColumnsInitialized) return;
        tableColumnsInitialized = true;
        this.initTableColumns();
    }
    
    protected abstract void initTableColumns();
    
    @SuppressWarnings("unchecked")
    private void restoreColumnSortInfos() {
        ColumnSortList sortList = this.cellTable.getColumnSortList();
        List<ColumnSortInfo> oldSortInfos;
        if (sortList.size() == 0) {
            boolean ascending = getDefaultSortColumn().getPreferredSortingOrder().isAscending();
            oldSortInfos = Collections.singletonList(new ColumnSortInfo(getDefaultSortColumn(), ascending));
        } else {
            oldSortInfos = new ArrayList<ColumnSortList.ColumnSortInfo>(sortList.size());
            for (int i = sortList.size() - 1; i >= 0; i--) {
                oldSortInfos.add(sortList.get(i));
            }
        }
        for (ColumnSortInfo sortInfo : oldSortInfos) {
            Column<T, ?> column = (Column<T, ?>) sortInfo.getColumn();
            this.cellTable.sortColumn(column);
        }
    }
    
    protected SortableRaceListColumn<T, ?> getDefaultSortColumn() {
        return raceNameColumn;
    }
    
    protected void add(SortableRaceListColumn<T, ?> column) {
        Header<?> header = column.getHeader();
        boolean ascending = column.getPreferredSortingOrder().isAscending();
        InvertibleComparator<T> comperator = column.getComparator();
        if (comperator != null) {
            comperator.setAscending(ascending);
        }
        this.cellTable.addColumn(column, header, comperator, ascending);
    }
    
    @Override
    public void onFilterValueChanged(final Filter<SimpleRaceMetadataDTO> filter) {
        this.cellTable.setRowStyles(new RowStyles<T>() {
            @Override
            public String getStyleNames(T row, int rowIndex) {
                return filter.matches(row) ? null : CSS.racesListHideColumn();
            }
        });
        this.cellTable.redraw();
    }
    
    @Override
    public Collection<SimpleCompetitorDTO> getFilterableValues() {
        Set<SimpleCompetitorDTO> filterableValues = new HashSet<>();
        for (T entry : cellTable.getDataProvider().getList()) {
            filterableValues.addAll(entry.getCompetitors());
        }
        return filterableValues;
    }
    
    public abstract boolean hasWind();

    public abstract boolean hasVideos();

    public abstract boolean hasAudios();
}
