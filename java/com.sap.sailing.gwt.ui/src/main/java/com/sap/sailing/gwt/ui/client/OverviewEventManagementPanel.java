package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;

/**
 * 
 * @author Lennart Hensler (D054527)
 *
 */
public class OverviewEventManagementPanel extends AbstractEventManagementPanel {
    
    private ListDataProvider<LeaderboardDAO> listLeaderboards;
    private CellList<LeaderboardDAO> cellListLeaderboards;
    private ListDataProvider<EventDAO> listEvents;
    private CellTable<EventDAO> cellTableEvents;

    public OverviewEventManagementPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, StringConstants stringConstants) {
        super(sailingService, eventRefresher, errorReporter, stringConstants);
        
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        
        HorizontalPanel listsSplitPanel = new HorizontalPanel();
        mainPanel.add(listsSplitPanel);
        listsSplitPanel.setWidth("100%");
        
        //Create leaderboards list
        CaptionPanel leaderboardsCaptionPanel = new CaptionPanel("Leaderboards");
        listsSplitPanel.add(leaderboardsCaptionPanel);
        leaderboardsCaptionPanel.setWidth("50%");
        leaderboardsCaptionPanel.setStyleName("bold");
        
        VerticalPanel leaderboardsPanel = new VerticalPanel();
        leaderboardsCaptionPanel.setContentWidget(leaderboardsPanel);
        leaderboardsPanel.setWidth("100%");
        
        AbstractCell<LeaderboardDAO> cellLeaderboards = new AbstractCell<LeaderboardDAO>() {
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context, LeaderboardDAO value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.appendEscaped(value.name);
                }
            }
        };
        
        cellListLeaderboards = new CellList<LeaderboardDAO>(cellLeaderboards);
        leaderboardsPanel.add(cellListLeaderboards);
        cellListLeaderboards.setTitle("Test");

        listLeaderboards = new ListDataProvider<LeaderboardDAO>();
        listLeaderboards.addDataDisplay(cellListLeaderboards);
        
        //Fill with test data
        sailingService.getLeaderboards(new AsyncCallback<List<LeaderboardDAO>>() {
            @Override
            public void onSuccess(List<LeaderboardDAO> leaderboards) {
                listLeaderboards.getList().clear();
                listLeaderboards.getList().addAll(leaderboards);
            }

            @Override
            public void onFailure(Throwable t) {
                OverviewEventManagementPanel.super.errorReporter
                        .reportError("Error trying to obtain list of leaderboards: " + t.getMessage());
            }
        });
        //
        
        //Create events list
        CaptionPanel eventsCaptionPanel = new CaptionPanel("Event list");
        listsSplitPanel.add(eventsCaptionPanel);
        eventsCaptionPanel.setWidth("50%");
        eventsCaptionPanel.setStyleName("bold");
        
        VerticalPanel eventsPanel = new VerticalPanel();
        eventsCaptionPanel.setContentWidget(eventsPanel);
        eventsPanel.setWidth("100%");
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        // TODO Auto-generated method stub

    }

}
