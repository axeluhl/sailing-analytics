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

    private ListDataProvider<EventDAO> listEvents;
    private CellList<EventDAO> cellListEvents;
    
    private CaptionPanel captionPanelLeaderboards;
    private ListDataProvider<LeaderboardDAO> listLeaderboards;
    private CellList<LeaderboardDAO> cellListLeaderboards;
    
    private EventDAO selectedEventBuffer = null;
    private LeaderboardDAO selectedLeaderboardBuffer = null;

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
        
        Button btnRefreshEvents = new Button(stringConstants.refresh());
        btnRefreshEvents.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                EventDAO selectedEvent = getSelectedEvent();
                loadEvents(true);
            }
        });
        functionPanelEvents.add(btnRefreshEvents);
        
        Button btnShowLeaderboards = new Button(">");
        btnShowLeaderboards.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent c) {
                captionPanelLeaderboards.setVisible(true);
            }
        });
        functionPanelEvents.add(btnShowLeaderboards);
        
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
                EventIdentifier identifier = null;
                if (selectedEvent != null) {
                    identifier = new EventName(selectedEvent.name);
                    loadLeaderboards(identifier);
                    if (!captionPanelLeaderboards.isVisible()) {
                        captionPanelLeaderboards.setVisible(true);
                    }
                }
            }
        });
        cellListEvents.setSelectionModel(selectionModelEvents);

        listEvents = new ListDataProvider<EventDAO>();
        listEvents.addDataDisplay(cellListEvents);
        
        //Build leaderboards GUI
        captionPanelLeaderboards = new CaptionPanel(stringConstants.leaderboards());
        captionPanelLeaderboards.setVisible(false);
        captionPanelLeaderboards.setWidth("50%");
        captionPanelLeaderboards.setStyleName("bold");
        listsSplitPanel.add(captionPanelLeaderboards);
        
        VerticalPanel leaderboardsPanel = new VerticalPanel();
        captionPanelLeaderboards.setContentWidget(leaderboardsPanel);
        leaderboardsPanel.setWidth("100%");
        
        //Create leaderboard functional elements
        HorizontalPanel functionPanelLeaderboards = new HorizontalPanel();
        leaderboardsPanel.add(functionPanelLeaderboards);
        
        Button btnHideLeaderboards = new Button("<");
        btnHideLeaderboards.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent arg0) {
                captionPanelLeaderboards.setVisible(false);
            }
        });
        functionPanelLeaderboards.add(btnHideLeaderboards);
        
        Button btnRefreshLeaderboards = new Button(stringConstants.refresh());
        btnRefreshLeaderboards.addClickHandler(new ClickHandler() {
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
        functionPanelLeaderboards.add(btnRefreshLeaderboards);
        
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
        cellListLeaderboards.setSelectionModel(selectionModelLeaderboards);

        listLeaderboards = new ListDataProvider<LeaderboardDAO>();
        listLeaderboards.addDataDisplay(cellListLeaderboards);
        
        
        //Fill lists
        loadEvents();
    }
    
    private void loadLeaderboards(EventIdentifier eventIdentifier) {
//        sailingService.getLeaderboardsByEvent(eventIdentifier, new AsyncCallback<List<LeaderboardDAO>>() {
//            @Override
//            public void onSuccess(List<LeaderboardDAO> leaderboards) {
//                listLeaderboards.getList().clear();
//                if (leaderboards != null) {
//                    listLeaderboards.getList().addAll(leaderboards);
//                }
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                OverviewEventManagementPanel.super.errorReporter
//                        .reportError("Error trying to obtain list of leaderboards: " + t.getMessage());
//            }
//        });
        sailingService.getLeaderboards(new AsyncCallback<List<LeaderboardDAO>>() {

            @Override
            public void onFailure(Throwable t) {
                OverviewEventManagementPanel.super.errorReporter
                .reportError("Error trying to obtain list of leaderboards: " + t.getMessage());
            }

            @Override
            public void onSuccess(List<LeaderboardDAO> leaderboards) {
                listLeaderboards.getList().clear();
                if (leaderboards != null) {
                    listLeaderboards.getList().addAll(leaderboards);
                }
            }
        });
    }
    
    private void loadEvents(final boolean reselect) {
        sailingService.listEvents(new AsyncCallback<List<EventDAO>>() {

            @Override
            public void onSuccess(List<EventDAO> result) {
                listEvents.getList().clear();
                if (result != null) {
                    listEvents.setList(result);
                }
                if (reselect) {
                    cellListEvents.getSelectionModel().setSelected(selectedEventBuffer, true);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                OverviewEventManagementPanel.super.errorReporter
                .reportError("Error trying to obtain list of leaderboards: " + caught.getMessage());
            }
        });
    }
    
    private void loadEvents() {
        loadEvents(false);
    }
    
    private EventDAO getSelectedEvent() {
        EventDAO result = null;
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
