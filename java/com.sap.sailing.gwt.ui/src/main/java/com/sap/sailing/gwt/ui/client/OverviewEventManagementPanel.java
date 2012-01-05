package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.server.api.EventIdentifier;
import com.sap.sailing.server.api.EventName;

/**
 * 
 * @author Lennart Hensler (D054527)
 *
 */
public class OverviewEventManagementPanel extends AbstractEventManagementPanel {
    
    private ListDataProvider<LeaderboardDAO> listLeaderboards;
    private CellList<LeaderboardDAO> cellListLeaderboards;
    private ListDataProvider<EventDAO> listEvents;
    private CellList<EventDAO> cellListEvents;

    public OverviewEventManagementPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, final StringConstants stringConstants) {
        super(sailingService, eventRefresher, errorReporter, stringConstants);
        
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");
        
        HorizontalPanel listsSplitPanel = new HorizontalPanel();
        mainPanel.add(listsSplitPanel);
        listsSplitPanel.setWidth("100%");
        
        //Build events GUI
        CaptionPanel eventsCaptionPanel = new CaptionPanel(stringConstants.events());
        listsSplitPanel.add(eventsCaptionPanel);
        eventsCaptionPanel.setWidth("50%");
        eventsCaptionPanel.setStyleName("bold");
        
        VerticalPanel eventsPanel = new VerticalPanel();
        eventsCaptionPanel.setContentWidget(eventsPanel);
        eventsPanel.setWidth("100%");
        
        //Create event functional elements
        HorizontalPanel functionPanelEvents = new HorizontalPanel();
        eventsPanel.add(functionPanelEvents);
        
        Button refreshEvents = new Button(stringConstants.refresh());
        refreshEvents.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                loadEvents();
            }
        });
        functionPanelEvents.add(refreshEvents);
        
        //Create event list
        AbstractCell<EventDAO> cellEvents = new AbstractCell<EventDAO>() {
            @Override
            public void render(com.google.gwt.cell.client.Cell.Context context, EventDAO value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.appendEscaped(value.name);
                }
            }
        };
        
        cellListEvents = new CellList<EventDAO>(cellEvents);
        eventsPanel.add(cellListEvents);
        Label emptyLabelEvents = new Label(stringConstants.noEventsFound());
        cellListEvents.setEmptyListWidget(emptyLabelEvents);
        
        SingleSelectionModel<EventDAO> selectionModelEvents = new SingleSelectionModel<EventDAO>();
        selectionModelEvents.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                EventDAO selectedEvent = getSelectedEvent();
                EventIdentifier identifier;
                if (selectedEvent != null) {
                    identifier = new EventName(selectedEvent.name);
                    loadLeaderboards(identifier);
                    if (!cellListLeaderboards.isVisible()) {
                        cellListLeaderboards.setVisible(true);
                    }
                }
            }
        });
        cellListEvents.setSelectionModel(selectionModelEvents);

        listEvents = new ListDataProvider<EventDAO>();
        listEvents.addDataDisplay(cellListEvents);
        
        //Build leaderboards GUI
        CaptionPanel leaderboardsCaptionPanel = new CaptionPanel(stringConstants.leaderboards());
        leaderboardsCaptionPanel.setVisible(false);
        leaderboardsCaptionPanel.setWidth("50%");
        leaderboardsCaptionPanel.setStyleName("bold");
        listsSplitPanel.add(leaderboardsCaptionPanel);
        
        VerticalPanel leaderboardsPanel = new VerticalPanel();
        leaderboardsCaptionPanel.setContentWidget(leaderboardsPanel);
        leaderboardsPanel.setWidth("100%");
        
        //Create leaderboard functional elements
        HorizontalPanel functionPanelLeaderboards = new HorizontalPanel();
        leaderboardsPanel.add(functionPanelLeaderboards);
        
        Button refreshLeaderboards = new Button(stringConstants.refresh());
        refreshLeaderboards.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent click) {
                EventDAO selectedEvent = getSelectedEvent();
                EventIdentifier identifier;
                if (selectedEvent != null) {
                    identifier = new EventName(selectedEvent.name);
                    loadLeaderboards(identifier);
                } else {
                    Window.alert(stringConstants.noEventSelected());
                }
            }
        });
        functionPanelLeaderboards.add(refreshLeaderboards);
        
        //Create leaderboard list
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
        Label emptyLabelLeaderboards = new Label();
        cellListLeaderboards.setEmptyListWidget(emptyLabelLeaderboards);
        
        SingleSelectionModel<LeaderboardDAO> selectionModelLeaderboards = new SingleSelectionModel<LeaderboardDAO>();
        selectionModelLeaderboards.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            
            @Override
            public void onSelectionChange(SelectionChangeEvent arg0) {
                // TODO Auto-generated method stub
                
            }
        });

        listLeaderboards = new ListDataProvider<LeaderboardDAO>();
        listLeaderboards.addDataDisplay(cellListLeaderboards);
        
        
        //Fill lists
        loadEvents();
    }
    
    private void loadLeaderboards(EventIdentifier eventIdentifier) {
        sailingService.getLeaderboardsByEvent(eventIdentifier, new AsyncCallback<List<LeaderboardDAO>>() {
            @Override
            public void onSuccess(List<LeaderboardDAO> leaderboards) {
                listLeaderboards.getList().clear();
                if (leaderboards != null) {
                    listLeaderboards.getList().addAll(leaderboards);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                OverviewEventManagementPanel.super.errorReporter
                        .reportError("Error trying to obtain list of leaderboards: " + t.getMessage());
            }
        });
    }
    
    private void loadEvents() {
        sailingService.listEvents(new AsyncCallback<List<EventDAO>>() {

            @Override
            public void onSuccess(List<EventDAO> result) {
                listEvents.getList().clear();
                if (result != null) {
                    listEvents.setList(result);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                OverviewEventManagementPanel.super.errorReporter
                .reportError("Error trying to obtain list of leaderboards: " + caught.getMessage());
            }
        });
    }
    
    private EventDAO getSelectedEvent() {
        EventDAO result = null;
        SelectionModel<?> t = cellListEvents.getSelectionModel();
        for (EventDAO event : listEvents.getList()) {
            if (cellListEvents.getSelectionModel().isSelected(event)) {
                result = event;
            }
        }
        return result;
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        trackedEventsComposite.fillEvents(result);
    }

}
