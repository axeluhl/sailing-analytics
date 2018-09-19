package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.FlushableCellTable;
import com.sap.sse.gwt.client.celltable.SelectionCheckboxColumn;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.shared.UserDTO;

public class StructureImportListComposite extends RegattaListComposite implements RegattasDisplayer {

    private SelectionCheckboxColumn<RegattaDTO> selectionCheckboxColumn;
    private final RegattaStructureProvider regattaStructureProvider;

    public static interface RegattaStructureProvider {
        RegattaStructure getRegattaStructure(RegattaDTO regatta);
    }

    public StructureImportListComposite(final SailingServiceAsync sailingService, final UserService userService,
            RegattaRefresher regattaRefresher, RegattaStructureProvider regattaStructureProvider,
            final ErrorReporter errorReporter, final StringMessages stringMessages) {
        super(sailingService, userService, regattaRefresher, errorReporter, stringMessages);
        this.regattaStructureProvider = regattaStructureProvider;
    }

    // create Regatta Table in StructureImportManagementPanel
    @Override
    protected CellTable<RegattaDTO> createRegattaTable(final UserDTO user,
            final UserManagementServiceAsync userManagementService) {
        FlushableCellTable<RegattaDTO> table = new FlushableCellTable<RegattaDTO>(/* pageSize */10000, tableRes);
        regattaListDataProvider.addDataDisplay(table);
        table.setWidth("100%");
        
        this.selectionCheckboxColumn = new SelectionCheckboxColumn<RegattaDTO>(
                tableRes.cellTableStyle().cellTableCheckboxSelected(),
                tableRes.cellTableStyle().cellTableCheckboxDeselected(),
                tableRes.cellTableStyle().cellTableCheckboxColumnCell(), new EntityIdentityComparator<RegattaDTO>() {
                    @Override
                    public boolean representSameEntity(RegattaDTO dto1, RegattaDTO dto2) {
                        return dto1.getRegattaIdentifier().equals(dto2.getRegattaIdentifier());
                    }
                    @Override
                    public int hashCode(RegattaDTO t) {
                        return t.getRegattaIdentifier().hashCode();
                    }
                }, regattaListDataProvider, table);

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
