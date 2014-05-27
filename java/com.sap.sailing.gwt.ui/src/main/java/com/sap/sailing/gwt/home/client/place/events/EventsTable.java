package com.sap.sailing.gwt.home.client.place.events;

import java.util.Comparator;
import java.util.UUID;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.ui.shared.ClickableSafeHtmlCell;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventsTable extends Composite {
    private static EventsTableUiBinder uiBinder = GWT.create(EventsTableUiBinder.class);

    interface EventsTableUiBinder extends UiBinder<Widget, EventsTable> {
    }

    private ListDataProvider<EventDTO> dataProvider = new ListDataProvider<EventDTO>();

    @UiField(provided = true)
    CellTable<EventDTO> cellTable;

    /**
     * The pager used to change the range of data.
     */
    @UiField(provided = true)
    SimplePager pager;

    private final EventsActivity activity;

    /**
     * Constructor.
     * 
     * @param constants
     *            the constants
     */
    public EventsTable(EventsActivity activity) {
        this.activity = activity;
        initTable();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public static final ProvidesKey<EventDTO> KEY_PROVIDER = new ProvidesKey<EventDTO>() {
        public UUID getKey(EventDTO item) {
            return item == null ? null : item.id;
        }
    };

    public void initTable() {
        cellTable = new CellTable<EventDTO>(KEY_PROVIDER);
        cellTable.setWidth("100%", true);

        ListHandler<EventDTO> sortHandler = new ListHandler<EventDTO>(dataProvider.getList());
        cellTable.addColumnSortHandler(sortHandler);

        // Create a Pager to control the table.
        SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
        pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
        pager.setDisplay(cellTable);

        // Add a selection model so we can select cells.
        final SelectionModel<EventDTO> selectionModel = new SingleSelectionModel<EventDTO>(KEY_PROVIDER);
        cellTable.setSelectionModel(selectionModel, DefaultSelectionEventManager.<EventDTO> createCheckboxManager());

        // Initialize the columns.
        initTableColumns(selectionModel, sortHandler);

        // Add the CellList to the adapter in the database.
        dataProvider.addDataDisplay(cellTable);
    }

    public void setEvents(Iterable<EventDTO> events) {
        dataProvider.getList().clear();
        Util.addAll(events, dataProvider.getList());
    }

    /**
     * Add the columns to the table.
     */
    private void initTableColumns(final SelectionModel<EventDTO> selectionModel, ListHandler<EventDTO> sortHandler) {
        // Checkbox column. This table will uses a checkbox column for selection.
        // Alternatively, you can call cellTable.setSelectionEnabled(true) to enable mouse selection.
        Column<EventDTO, Boolean> checkColumn = new Column<EventDTO, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(EventDTO object) {
                // Get the value from the selection model.
                return selectionModel.isSelected(object);
            }
        };
        cellTable.addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
        cellTable.setColumnWidth(checkColumn, 40, Unit.PX);

        // name.
        Column<EventDTO, SafeHtml> nameColumn = new Column<EventDTO, SafeHtml>(new ClickableSafeHtmlCell()) {
            @Override
            public SafeHtml getValue(EventDTO event) {
                final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
                safeHtmlBuilder.appendHtmlConstant("<a>");
                safeHtmlBuilder.appendEscaped(event.getName());
                safeHtmlBuilder.appendHtmlConstant("</a>");
                return safeHtmlBuilder.toSafeHtml();
            }
        };
        nameColumn.setFieldUpdater(new FieldUpdater<EventDTO, SafeHtml>() {
            @Override
            public void update(int index, EventDTO object, SafeHtml value) {
                activity.goTo(new EventPlace(object.id.toString()));
            }
        });
        nameColumn.setSortable(true);
        sortHandler.setComparator(nameColumn, new Comparator<EventDTO>() {
            public int compare(EventDTO o1, EventDTO o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        cellTable.addColumn(nameColumn, "name");
        cellTable.setColumnWidth(nameColumn, 20, Unit.PCT);
    }
}
