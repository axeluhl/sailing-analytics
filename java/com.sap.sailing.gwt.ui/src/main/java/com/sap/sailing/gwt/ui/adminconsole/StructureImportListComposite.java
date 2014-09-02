package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattaSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public class StructureImportListComposite extends Composite implements RegattasDisplayer {

    private final SelectionModel<RegattaDTO> regattaSelectionModel;
    private final CellTable<RegattaDTO> regattaTable;
    private ListDataProvider<RegattaDTO> regattaListDataProvider;
    private List<RegattaDTO> allRegattas;
    private final SimplePanel mainPanel;
    private final VerticalPanel panel;
    private final SelectionCheckboxColumn<RegattaDTO> selectionCheckboxColumn;

    private final Label noRegattasLabel;

    private final SailingServiceAsync sailingService;
    private final RegattaSelectionProvider regattaSelectionProvider;
    private final ErrorReporter errorReporter;
    private final RegattaRefresher regattaRefresher;
    private final StringMessages stringMessages;

    private final LabeledAbstractFilterablePanel<RegattaDTO> filterablePanelRegattas;

    private static AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

    public static class AnchorCell extends AbstractCell<SafeHtml> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml safeHtml, SafeHtmlBuilder sb) {
            sb.append(safeHtml);
        }
    }

    public StructureImportListComposite(final SailingServiceAsync sailingService,
            final RegattaSelectionProvider regattaSelectionProvider, RegattaRefresher regattaRefresher,
            final ErrorReporter errorReporter, final StringMessages stringMessages, String s) {
        this.sailingService = sailingService; /* Zuweisung vllt raus? */
        this.regattaSelectionProvider = regattaSelectionProvider;
        this.regattaRefresher = regattaRefresher;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);
        this.selectionCheckboxColumn = new SelectionCheckboxColumn<RegattaDTO>(tableResources.cellTableStyle()
                .cellTableCheckboxSelected(), tableResources.cellTableStyle().cellTableCheckboxDeselected(),
                tableResources.cellTableStyle().cellTableCheckboxColumnCell()) {
            @Override
            protected ListDataProvider<RegattaDTO> getListDataProvider() {
                return regattaListDataProvider;
            }

            @Override
            public Boolean getValue(RegattaDTO row) {
                return regattaTable.getSelectionModel().isSelected(row);
            }
        };
        mainPanel = new SimplePanel();
        panel = new VerticalPanel();
        mainPanel.setWidget(panel);
        Label filterRegattasLabel = new Label(stringMessages.filterRegattasByName() + ":");
        filterRegattasLabel.setWordWrap(false);
        noRegattasLabel = new Label(stringMessages.noRegattasYet());
        noRegattasLabel.ensureDebugId("NoRegattasLabel");
        noRegattasLabel.setWordWrap(false);
        panel.add(noRegattasLabel);

        regattaListDataProvider = new ListDataProvider<RegattaDTO>();
        regattaTable = createRegattaImportTable();
        regattaTable.ensureDebugId("RegattasCellTable");
        regattaTable.setVisible(false);
        filterablePanelRegattas = new LabeledAbstractFilterablePanel<RegattaDTO>(filterRegattasLabel, allRegattas,
                regattaTable, regattaListDataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(RegattaDTO t) {
                List<String> string = new ArrayList<String>();
                string.add(t.getName());
                return string;
            }
        };
        filterablePanelRegattas.getTextBox().ensureDebugId("ReggatasFilterTextBox");
        panel.add(filterablePanelRegattas);

        regattaSelectionModel = this.selectionCheckboxColumn.getSelectionModel();
        regattaTable.setSelectionModel(regattaSelectionModel, this.selectionCheckboxColumn.getSelectionManager());
        regattaSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                List<RegattaDTO> selectedRegattas = getSelectedRegattas();
                List<RegattaIdentifier> selectedRaceIdentifiers = new ArrayList<RegattaIdentifier>();
                for (RegattaDTO selectedRegatta : selectedRegattas) {
                    selectedRaceIdentifiers.add(selectedRegatta.getRegattaIdentifier());
                }
                StructureImportListComposite.this.regattaSelectionProvider.setSelection(selectedRaceIdentifiers);
            }
        });
        panel.add(regattaTable);

        initWidget(mainPanel);
    }


    private List<RegattaDTO> getSelectedRegattas() {
        List<RegattaDTO> result = new ArrayList<RegattaDTO>();
        if (regattaListDataProvider != null) {
            for (RegattaDTO regatta : regattaListDataProvider.getList()) {
                if (regattaSelectionModel.isSelected(regatta)) {
                    result.add(regatta);
                }
            }
        }
        return result;
    }

    @Override
    public void fillRegattas(List<RegattaDTO> regattas) {
        if (regattas.isEmpty()) {
            regattaTable.setVisible(false);
            noRegattasLabel.setVisible(true);
        } else {
            regattaTable.setVisible(true);
            noRegattasLabel.setVisible(false);
        }
        List<RegattaDTO> newAllRegattas = new ArrayList<RegattaDTO>(regattas);
        List<RegattaIdentifier> newAllRegattaIdentifiers = new ArrayList<RegattaIdentifier>();
        for (RegattaDTO regatta : regattas) {
            newAllRegattaIdentifiers.add(regatta.getRegattaIdentifier());
        }
        allRegattas = newAllRegattas;
        filterablePanelRegattas.updateAll(allRegattas);
        regattaSelectionProvider.setAllRegattas(newAllRegattaIdentifiers);
    }

    public List<RegattaDTO> getAllRegattas() {
        return allRegattas;
    }

    // create Regatta Table in StructureImportURLManagementPanel

    private CellTable<RegattaDTO> createRegattaImportTable() {
        CellTable<RegattaDTO> table = new CellTable<RegattaDTO>(/* pageSize */10000, tableRes);
        regattaListDataProvider.addDataDisplay(table);
        table.setWidth("100%");

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

        columnSortHandler.setComparator(selectionCheckboxColumn, selectionCheckboxColumn.getComparator());
        table.addColumn(selectionCheckboxColumn, selectionCheckboxColumn.getHeader());
        table.addColumn(regattaNameColumn, stringMessages.regattaName());

        return table;
    }

}
