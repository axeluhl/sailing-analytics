package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattaSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RegattaListComposite extends Composite implements RegattaDisplayer {

    private final SelectionModel<RegattaDTO> regattaSelectionModel;
    private final CellTable<RegattaDTO> regattaTable;
    private ListDataProvider<RegattaDTO> regattaListDataProvider;
    private List<RegattaDTO> allRegattas;
    private final SimplePanel mainPanel;
    private final VerticalPanel panel;

    private final Label noRegattasLabel;

    private final SailingServiceAsync sailingService;
    private final RegattaSelectionProvider regattaSelectionProvider;
    private final ErrorReporter errorReporter;
    private final RegattaRefresher regattaRefresher;
    private final StringMessages stringMessages;

    private final TextBox filterRegattasTextbox;

    private static AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

    public static class AnchorCell extends AbstractCell<SafeHtml> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml safeHtml, SafeHtmlBuilder sb) {
            sb.append(safeHtml);
        }
    }

    public RegattaListComposite(final SailingServiceAsync sailingService, final RegattaSelectionProvider regattaSelectionProvider,  
            RegattaRefresher regattaRefresher, final ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.regattaSelectionProvider = regattaSelectionProvider;
        this.regattaRefresher = regattaRefresher;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        
        mainPanel = new SimplePanel();
        panel = new VerticalPanel();
        mainPanel.setWidget(panel);

        HorizontalPanel filterPanel = new HorizontalPanel();
        panel.add(filterPanel);
        Label filterRegattasLabel = new Label(stringMessages.filterRegattasByName() + ":");
        filterRegattasLabel.setWordWrap(false);
        filterPanel.setSpacing(5);
        filterPanel.add(filterRegattasLabel);
        filterPanel.setCellVerticalAlignment(filterRegattasLabel, HasVerticalAlignment.ALIGN_MIDDLE);
        filterRegattasTextbox = new TextBox();
        filterRegattasTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                updateFilteredRegattasList();
            }
        });
        filterPanel.add(filterRegattasTextbox);
        
        noRegattasLabel = new Label(stringMessages.noRegattasYet());
        noRegattasLabel.setWordWrap(false);
        panel.add(noRegattasLabel);

        regattaListDataProvider = new ListDataProvider<RegattaDTO>();
        regattaTable = createRegattaTable();
        regattaTable.setVisible(false);
        
        regattaSelectionModel = new SingleSelectionModel<RegattaDTO>();
        regattaSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                List<RegattaDTO> selectedRegattas = getSelectedRegattas();
                List<RegattaIdentifier> selectedRaceIdentifiers = new ArrayList<RegattaIdentifier>();
                for (RegattaDTO selectedRegatta : selectedRegattas) {
                    selectedRaceIdentifiers.add(selectedRegatta.getRegattaIdentifier());
                }
                RegattaListComposite.this.regattaSelectionProvider.setSelection(selectedRaceIdentifiers);
            }
        });
        regattaTable.setSelectionModel(regattaSelectionModel);
        
        panel.add(regattaTable);
        
        initWidget(mainPanel);
    }

    private CellTable<RegattaDTO> createRegattaTable() {
        CellTable<RegattaDTO> table = new CellTable<RegattaDTO>(/* pageSize */10000, tableRes);
        regattaListDataProvider.addDataDisplay(table);
        table.setWidth("100%");

        ListHandler<RegattaDTO> columnSortHandler = new ListHandler<RegattaDTO>(regattaListDataProvider.getList());
        table.addColumnSortHandler(columnSortHandler);

        TextColumn<RegattaDTO> regattaNameColumn = new TextColumn<RegattaDTO>() {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.name;
            }
        };
        regattaNameColumn.setSortable(true);
        columnSortHandler.setComparator(regattaNameColumn, new Comparator<RegattaDTO>() {
            @Override
            public int compare(RegattaDTO r1, RegattaDTO r2) {
                return r1.name.compareTo(r2.name);
            }
        });

        TextColumn<RegattaDTO> regattaBoatClassColumn = new TextColumn<RegattaDTO>() {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.boatClass != null ? regatta.boatClass.name : "";
            }
        };
        regattaBoatClassColumn.setSortable(true);
        columnSortHandler.setComparator(regattaBoatClassColumn, new Comparator<RegattaDTO>() {
            @Override
            public int compare(RegattaDTO r1, RegattaDTO r2) {
                return r1.boatClass.name.compareTo(r2.boatClass.name);
            }
        });

        ImagesBarColumn<RegattaDTO, RegattaConfigImagesBarCell> regattaActionColumn = new ImagesBarColumn<RegattaDTO, RegattaConfigImagesBarCell>(
                new RegattaConfigImagesBarCell(stringMessages));
        regattaActionColumn.setFieldUpdater(new FieldUpdater<RegattaDTO, String>() {
            @Override
            public void update(int index, RegattaDTO regatta, String value) {
                if (RegattaConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveRegatta(regatta.name))) {
                        removeRegatta(regatta);
                    }
                }
            }
        });
        
        table.addColumn(regattaNameColumn, stringMessages.regattaName());
        table.addColumn(regattaBoatClassColumn, stringMessages.boatClass());
        table.addColumn(regattaActionColumn, stringMessages.actions());
        
        return table;
    }
    
    private void updateFilteredRegattasList() {
        String text = filterRegattasTextbox.getText();
        List<String> wordsToFilter = Arrays.asList(text.split(" "));
        regattaListDataProvider.getList().clear();
        if (text != null && !text.isEmpty()) {
            for (RegattaDTO regattaDTO : allRegattas) {
                boolean failed = false;
                for (String word : wordsToFilter) {
                    String textAsUppercase = word.toUpperCase().trim();
                    if (!regattaDTO.name.toUpperCase().contains(textAsUppercase)
                            && !regattaDTO.boatClass.name.toUpperCase().contains(textAsUppercase)
                            && !regattaDTO.name.toUpperCase().contains(textAsUppercase)) {
                        failed = true;
                        break;
                    }
                }
                if (!failed) {
                    regattaListDataProvider.getList().add(regattaDTO);
                }
            }
        } else {
            for (RegattaDTO regatta : allRegattas) {
                regattaListDataProvider.getList().add(regatta);
            }
        }
        // now sort again according to selected criterion
        ColumnSortEvent.fire(regattaTable, regattaTable.getColumnSortList());
    }

    private void removeRegatta(final RegattaDTO regatta) {
        final RegattaIdentifier regattaIdentifier = new RegattaName(regatta.name);
        sailingService.removeRegatta(regattaIdentifier, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to remove regatta " + regatta.name + ": " + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                regattaRefresher.fillRegattas();
            }
        });
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
        updateFilteredRegattasList();
        regattaSelectionProvider.setAllRegattas(newAllRegattaIdentifiers); 
    }

    public List<RegattaDTO> getAllRegattas() {
        return allRegattas;
    }
}
