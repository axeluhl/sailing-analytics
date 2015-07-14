package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import java.util.Collection;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources.LocalCss;
import com.sap.sailing.gwt.home.client.place.event.partials.racelist.RaceListColumnFactory.SortableRaceListStartTimeColumn;
import com.sap.sailing.gwt.ui.leaderboard.SortedCellTable;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.wind.AbstractWindDTO;
import com.sap.sse.gwt.theme.client.component.celltable.CleanCellTableResources;
import com.sap.sse.gwt.theme.client.component.celltable.StyledHeaderOrFooterBuilder;

public abstract class AbstractRaceList<T extends RaceMetadataDTO<? extends AbstractWindDTO>> extends Composite {

    private static final LocalCss CSS = RacesListLiveResources.INSTANCE.css();

    private final SimplePanel cellTableContainer = new SimplePanel();
    
    protected final SortableRaceListColumn<T, ?> fleetCornerColumn = RaceListColumnFactory.getFleetCornerColumn();
    protected final SortableRaceListColumn<T, ?> raceNameColumn = RaceListColumnFactory.getRaceNameColumn();
    protected final SortableRaceListColumn<T, ?> fleetNameColumn = RaceListColumnFactory.getFleetNameColumn(); 
    protected final SortableRaceListStartTimeColumn<T> startTimeColumn = RaceListColumnFactory.getStartTimeColumn();
    protected final SortableRaceListColumn<T, ?> windDirectionColumn = RaceListColumnFactory.getWindDirectionColumn();
    protected final SortableRaceListColumn<T, ?> raceViewerButtonColumn;
    
    private SortedCellTable<T> cellTable;
    
    protected AbstractRaceList(EventView.Presenter presenter) {
        CSS.ensureInjected();
        this.raceViewerButtonColumn = RaceListColumnFactory.getRaceViewerButtonColumn(presenter);
        this.initWidget(cellTableContainer);
    }

    protected void setTableData(Collection<T> data) {
        Column<T, ?> sortColumn = (this.cellTable == null ? null : this.cellTable.getCurrentlySortedColumn());
        this.cellTable = new SortedCellTable<T>(data.size(), CleanCellTableResources.INSTANCE);
        this.cellTableContainer.setWidget(this.cellTable);
        this.initTableStyle();
        this.initTableColumns();
        this.cellTable.setList(data);
        if (sortColumn != null && this.cellTable.getColumnIndex(sortColumn) >= 0) {
            this.cellTable.sortColumn(sortColumn);
        }
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
    
    protected abstract void initTableColumns();

    protected void add(SortableRaceListColumn<T, ?> column) {
        if (column.isShowDetails()) {
            if (column.getColumnStyle() != null) {
                column.setCellStyleNames(column.getColumnStyle());
            }
            Header<?> header = column.getHeader();
            if (header != null && column.getHeaderStyle() != null) {
                header.setHeaderStyleNames(column.getHeaderStyle());
            }
            boolean ascending = column.getPreferredSortingOrder().isAscending();
            this.cellTable.addColumn(column, header, column.getComparator(), ascending);
        }
    }
    
}
