package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattaSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class StructureImportListComposite extends RegattaListComposite implements RegattasDisplayer {

    private SelectionCheckboxColumn<RegattaDTO> selectionCheckboxColumn;
    private final RegattaStructureProvider regattaStructureProvider;

    public static interface RegattaStructureProvider {
        RegattaStructure getRegattaStructure(RegattaDTO regatta);
    }

    public StructureImportListComposite(final SailingServiceAsync sailingService,
            final RegattaSelectionProvider regattaSelectionProvider, RegattaRefresher regattaRefresher,
            RegattaStructureProvider regattaStructureProvider, final ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        super(sailingService, regattaSelectionProvider, regattaRefresher, errorReporter, stringMessages);
        this.regattaStructureProvider = regattaStructureProvider;
    }

    // create Regatta Table in StructureImportManagementPanel
    @Override
    protected CellTable<RegattaDTO> createRegattaTable() {
        CellTable<RegattaDTO> table = new CellTable<RegattaDTO>(/* pageSize */10000, tableRes);
        regattaListDataProvider.addDataDisplay(table);
        table.setWidth("100%");
        
        this.selectionCheckboxColumn = new SelectionCheckboxColumn<RegattaDTO>(tableRes.cellTableStyle()
                .cellTableCheckboxSelected(), tableRes.cellTableStyle().cellTableCheckboxDeselected(),
                tableRes.cellTableStyle().cellTableCheckboxColumnCell()) {
            @Override
            protected ListDataProvider<RegattaDTO> getListDataProvider() {
                return regattaListDataProvider;
            }

            @Override
            public Boolean getValue(RegattaDTO row) {
                return regattaTable.getSelectionModel().isSelected(row);
            }
        };

        ListHandler<RegattaDTO> columnSortHandler = new ListHandler<RegattaDTO>(regattaListDataProvider.getList());
        table.addColumnSortHandler(columnSortHandler);

        TextColumn<RegattaDTO> regattaNameColumn = new TextColumn<RegattaDTO>() {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.getName();
            }
        };
        regattaNameColumn.setSortable(true);
        columnSortHandler.setComparator(regattaNameColumn, new Comparator<RegattaDTO>() {
            @Override
            public int compare(RegattaDTO r1, RegattaDTO r2) {
                return new NaturalComparator().compare(r1.getName(), r2.getName());
            }
        });
        TextColumn<RegattaDTO> regattaStructureColumn = new TextColumn<RegattaDTO>() {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regattaStructureProvider.getRegattaStructure(regatta).toString();
            }
        };
        regattaStructureColumn.setSortable(true);
        columnSortHandler.setComparator(regattaStructureColumn, new Comparator<RegattaDTO>() {
            @Override
            public int compare(RegattaDTO r1, RegattaDTO r2) {
                return new NaturalComparator().compare(regattaStructureProvider.getRegattaStructure(r1).toString(),
                        regattaStructureProvider.getRegattaStructure(r2).toString());
            }
        });

        columnSortHandler.setComparator(selectionCheckboxColumn, selectionCheckboxColumn.getComparator());
        table.addColumn(selectionCheckboxColumn, selectionCheckboxColumn.getHeader());
        table.addColumn(regattaNameColumn, stringMessages.regattaName());
        table.addColumn(regattaStructureColumn, stringMessages.series());
        table.setSelectionModel(selectionCheckboxColumn.getSelectionModel(), selectionCheckboxColumn.getSelectionManager());

        return table;
    }

}
