package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;

/**
 * Allows an administrator to view and edit the set of competitors currently maintained by the server.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CompetitorPanel extends SimplePanel {
    private final CompetitorTableWrapper<RefreshableMultiSelectionModel<CompetitorDTO>> competitorTable;
    private final RefreshableMultiSelectionModel<CompetitorDTO> refreshableCompetitorSelectionModel;
    private final String leaderboardName;

    public CompetitorPanel(final SailingServiceAsync sailingService, final StringMessages stringMessages,
            final ErrorReporter errorReporter) {
        this(sailingService, null, stringMessages, errorReporter);
    }

    public CompetitorPanel(final SailingServiceAsync sailingService, final String leaderboardName,
            final StringMessages stringMessages, final ErrorReporter errorReporter) {
        super();
        this.leaderboardName = leaderboardName;
        this.competitorTable = new CompetitorTableWrapper<>(sailingService, stringMessages, errorReporter, /* multiSelection */ true, /* enablePager */ false);
        this.refreshableCompetitorSelectionModel = (RefreshableMultiSelectionModel<CompetitorDTO>) competitorTable.getSelectionModel();
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        HorizontalPanel competitorsPanel = new HorizontalPanel();
        competitorsPanel.setSpacing(5);
        mainPanel.add(competitorsPanel);
        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshCompetitorList();
            }
        });
        refreshButton.ensureDebugId("RefreshButton");
        buttonPanel.add(refreshButton);
        final Button allowReloadButton = new Button(stringMessages.allowReload());
        allowReloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                competitorTable.allowUpdate(refreshableCompetitorSelectionModel.getSelectedSet());
            }
        });
        buttonPanel.add(allowReloadButton);
        Button addCompetitorButton = new Button(stringMessages.add());
        addCompetitorButton.ensureDebugId("AddCompetitorButton");
        addCompetitorButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openAddCompetitorDialog();
            }
        });
        buttonPanel.add(addCompetitorButton);
        
        Button selectAllButton = new Button(stringMessages.selectAll());
        selectAllButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                for (CompetitorDTO c : competitorTable.getDataProvider().getList()) {
                    refreshableCompetitorSelectionModel.setSelected(c, true);
                }
            }
        });
        buttonPanel.add(selectAllButton);

        //only if this competitor panel is connected to a leaderboard, we want to enable invitations
        if (leaderboardName != null) {
            final Button inviteCompetitorsButton = new Button(stringMessages.inviteSelectedCompetitors());
            inviteCompetitorsButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Set<CompetitorDTO> competitors = refreshableCompetitorSelectionModel.getSelectedSet();

                    CompetitorInvitationHelper helper = new CompetitorInvitationHelper(sailingService, stringMessages, errorReporter);
                    helper.inviteCompetitors(competitors, leaderboardName);
                }
            });
            buttonPanel.add(inviteCompetitorsButton);
        }

        competitorsPanel.add(buttonPanel);
        mainPanel.add(competitorTable);
        
        refreshableCompetitorSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                allowReloadButton.setEnabled(!refreshableCompetitorSelectionModel.getSelectedSet().isEmpty());
            }
        });
        allowReloadButton.setEnabled(!refreshableCompetitorSelectionModel.getSelectedSet().isEmpty());

        if (leaderboardName != null) {
            refreshCompetitorList();
        }
    }

    private void openAddCompetitorDialog() {
        competitorTable.openEditCompetitorDialog(new CompetitorDTOImpl(), /* boat class to be used from CompetitorDTO */ null);
    }
    
    public void refreshCompetitorList() {
        competitorTable.refreshCompetitorList(leaderboardName);
    }
}
