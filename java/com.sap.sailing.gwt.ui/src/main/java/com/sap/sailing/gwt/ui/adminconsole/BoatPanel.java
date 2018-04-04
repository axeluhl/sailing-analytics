package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;

/**
 * Allows an administrator to view and edit the set of boats currently maintained by the server.
 * 
 * @author Frank Mittag (c5163874)
 * 
 */
public class BoatPanel extends SimplePanel {
    private final BoatTableWrapper<RefreshableMultiSelectionModel<BoatDTO>> boatTable;
    private final RefreshableMultiSelectionModel<BoatDTO> refreshableBoatSelectionModel;

    public BoatPanel(final SailingServiceAsync sailingService, final StringMessages stringMessages, final ErrorReporter errorReporter) {
        super();
        this.boatTable = new BoatTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */ true, 100, true);
        this.refreshableBoatSelectionModel = (RefreshableMultiSelectionModel<BoatDTO>) boatTable.getSelectionModel();
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        HorizontalPanel boatsPanel = new HorizontalPanel();
        boatsPanel.setSpacing(5);
        mainPanel.add(boatsPanel);
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshBoatList();
            }
        });
        refreshButton.ensureDebugId("RefreshButton");
        buttonPanel.add(refreshButton);
        final Button allowReloadButton = new Button(stringMessages.allowReload());
        allowReloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                boatTable.allowUpdate(refreshableBoatSelectionModel.getSelectedSet());
            }
        });
        buttonPanel.add(allowReloadButton);
        Button addBoatButton = new Button(stringMessages.add());
        addBoatButton.ensureDebugId("AddBoatButton");
        addBoatButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openAddBoatDialog();
            }
        });
        buttonPanel.add(addBoatButton);
        
        Button selectAllButton = new Button(stringMessages.selectAll());
        selectAllButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (BoatDTO b : boatTable.getDataProvider().getList()) {
                    refreshableBoatSelectionModel.setSelected(b, true);
                }
            }
        });
        buttonPanel.add(selectAllButton);

        boatsPanel.add(buttonPanel);
        mainPanel.add(boatTable);
        
        refreshableBoatSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                allowReloadButton.setEnabled(!refreshableBoatSelectionModel.getSelectedSet().isEmpty());
            }
        });
        allowReloadButton.setEnabled(!refreshableBoatSelectionModel.getSelectedSet().isEmpty());
    }

    private void openAddBoatDialog() {
        boatTable.openEditBoatDialog(new BoatDTO(), null);
    }
    
    public void refreshBoatList() {
        boatTable.refreshBoatList(/* loadOnlyStandaloneBoats */ false, /* callback */ null);
    }
}
