package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
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

    private TextBox textBoxLocation;
    private TextBox textBoxName;
    private TextBox textBoxFrom;
    private TextBox textBoxUntil;
    private CheckBox checkBoxLive;
    
    private CaptionPanel eventsCaptionPanel;
    private ListDataProvider<EventDAO> listEvents;
    private CellList<EventDAO> cellListEvents;
    private Button btnShowLeaderboards;

    private CaptionPanel captionPanelLeaderboards;
    private ListDataProvider<LeaderboardDAO> listLeaderboards;
    private CellList<LeaderboardDAO> cellListLeaderboards;

    public OverviewEventManagementPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, final StringConstants stringConstants) {
        super(sailingService, eventRefresher, errorReporter, stringConstants);

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");

        // Build search GUI
        CaptionPanel captionPanelSearch = new CaptionPanel(stringConstants.searchEvents());
        mainPanel.add(captionPanelSearch);
        captionPanelSearch.setWidth("100%");
        
        HorizontalPanel panelSearch = new HorizontalPanel();
        panelSearch.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
        captionPanelSearch.add(panelSearch);
        panelSearch.setWidth("100%");
        
        Label lblLocation = new Label(stringConstants.location() + ":");
        panelSearch.add(lblLocation);
        textBoxLocation = new TextBox();
        textBoxLocation.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent arg0) {
                // TODO Auto-generated method stub
                
            }
        });
        panelSearch.add(textBoxLocation);
        
        Label lblName = new Label(stringConstants.name() + ":");
        panelSearch.add(lblName);
        textBoxName = new TextBox();
        textBoxName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent arg0) {
                // TODO Auto-generated method stub
                
            }
        });
        panelSearch.add(textBoxName);
        
        checkBoxLive = new CheckBox(stringConstants.onlyLiveEvents());
        checkBoxLive.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> arg0) {
                // TODO Auto-generated method stub
                
            }
        });
        checkBoxLive.setEnabled(false);
        panelSearch.add(checkBoxLive);
        
        Label lblFromDate = new Label(stringConstants.from() + ":");
        panelSearch.add(lblFromDate);
        textBoxFrom = new TextBox();
        textBoxFrom.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent arg0) {
                // TODO Auto-generated method stub
                
            }
        });
        panelSearch.add(textBoxFrom);
        
        Label lblToDate = new Label(stringConstants.until() + ":");
        panelSearch.add(lblToDate);
        textBoxUntil = new TextBox();
        textBoxUntil.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent arg0) {
                // TODO Auto-generated method stub
                
            }
        });

        // Build events GUI
        HorizontalPanel listsSplitPanel = new HorizontalPanel();
        mainPanel.add(listsSplitPanel);
        listsSplitPanel.setWidth("100%");
        
        eventsCaptionPanel = new CaptionPanel(stringConstants.events());
        listsSplitPanel.add(eventsCaptionPanel);
        eventsCaptionPanel.setWidth("50%");
        eventsCaptionPanel.setStyleName("bold");

        VerticalPanel eventsPanel = new VerticalPanel();
        eventsCaptionPanel.setContentWidget(eventsPanel);
        eventsPanel.setWidth("100%");

        // Create event functional elements
        HorizontalPanel functionPanelEvents = new HorizontalPanel();
        functionPanelEvents.setSpacing(5);
        eventsPanel.add(functionPanelEvents);

        Button btnRefreshEvents = new Button(stringConstants.refresh());
        btnRefreshEvents.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final EventDAO selectedEvent = getSelectedEvent();
                if (selectedEvent != null) {
                    Runnable reselectEvent = new Runnable() {
                        @Override
                        public void run() {
                            // loadEvents fills the list with new instances so you have to loop over them and compare
                            // each with the last selected one
                            for (EventDAO event : listEvents.getList()) {
                                EventName eventName = new EventName(event.name);
                                EventName selectedEventName = new EventName(selectedEvent.name);
                                if (eventName.equals(selectedEventName)) {
                                    cellListEvents.getSelectionModel().setSelected(event, true);
                                }
                            }
                        }
                    };
                    loadEvents(reselectEvent);
                } else {
                    loadEvents();
                }
            }
        });
        functionPanelEvents.add(btnRefreshEvents);

        btnShowLeaderboards = new Button(">");
        btnShowLeaderboards.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent c) {
                if (getSelectedEvent() != null) {
                    captionPanelLeaderboards.setVisible(true);
                }
            }
        });
        btnShowLeaderboards.setEnabled(false);
        functionPanelEvents.add(btnShowLeaderboards);

        // Create event list
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
                    btnShowLeaderboards.setEnabled(true);
                } else {
                    btnShowLeaderboards.setEnabled(false);
                    captionPanelLeaderboards.setVisible(false);
                }
            }
        });
        cellListEvents.setSelectionModel(selectionModelEvents);

        listEvents = new ListDataProvider<EventDAO>();
        listEvents.addDataDisplay(cellListEvents);

        // Build leaderboards GUI
        captionPanelLeaderboards = new CaptionPanel(stringConstants.leaderboards());
        captionPanelLeaderboards.setVisible(false);
        captionPanelLeaderboards.setWidth("50%");
        captionPanelLeaderboards.setStyleName("bold");
        listsSplitPanel.add(captionPanelLeaderboards);

        VerticalPanel leaderboardsPanel = new VerticalPanel();
        captionPanelLeaderboards.setContentWidget(leaderboardsPanel);
        leaderboardsPanel.setWidth("100%");

        // Create leaderboard functional elements
        HorizontalPanel functionPanelLeaderboards = new HorizontalPanel();
        functionPanelLeaderboards.setSpacing(5);
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
                if (selectedEvent != null) {
                    EventIdentifier identifier = new EventName(selectedEvent.name);
                    final LeaderboardDAO selectedLeaderboard = getSelectedLeaderboard();
                    
                    if (selectedLeaderboard != null) {
                        Runnable reselectLeaderboard = new Runnable() {
                            @Override
                            public void run() {
                                // loadLeaderboards fills the list with new instances so you have to loop over them and
                                // compare each with the last selected one
                                for (LeaderboardDAO leaderboard : listLeaderboards.getList()) {
                                    // LeaderboardDAO has no equals method, so they are just compared by there names
                                    if (leaderboard.name.equals(selectedLeaderboard.name)) {
                                        cellListLeaderboards.getSelectionModel().setSelected(leaderboard, true);
                                    }
                                }
                            }
                        };
                        loadLeaderboards(identifier, reselectLeaderboard);
                    } else {
                        loadLeaderboards(identifier);
                    }
                } else {
                    Window.alert(stringConstants.noEventSelected());
                }
            }
        });
        functionPanelLeaderboards.add(btnRefreshLeaderboards);

        // Create leaderboard list
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

        // Fill lists
        loadEvents();
        
        //Set checkbox as true, because we can't search for old events right now
        //TODO Remove after searching for old events is possible
        checkBoxLive.setValue(true, true);
    }

    private void loadLeaderboards(EventIdentifier eventIdentifier, final Runnable r) {
        // sailingService.getLeaderboardsByEvent(eventIdentifier, new AsyncCallback<List<LeaderboardDAO>>() {
        // @Override
        // public void onSuccess(List<LeaderboardDAO> leaderboards) {
        // listLeaderboards.getList().clear();
        // if (leaderboards != null) {
        // listLeaderboards.getList().addAll(leaderboards);
        // }
        // }
        //
        // @Override
        // public void onFailure(Throwable t) {
        // OverviewEventManagementPanel.super.errorReporter
        // .reportError("Error trying to obtain list of leaderboards: " + t.getMessage());
        // }
        // });
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
                    if (r != null) {
                        r.run();
                    }
                }
            }
        });
    }
    private void loadLeaderboards(EventIdentifier eventIdentifier) {
        loadLeaderboards(eventIdentifier, null);
    }

    private void loadEvents(final Runnable r) {
        sailingService.listEvents(new AsyncCallback<List<EventDAO>>() {

            @Override
            public void onSuccess(List<EventDAO> result) {
                listEvents.getList().clear();
                if (result != null) {
                    listEvents.setList(result);
                }
                if (r != null) {
                    r.run();
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
        loadEvents(null);
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
    
    private LeaderboardDAO getSelectedLeaderboard() {
        LeaderboardDAO result = null;
        for (LeaderboardDAO leaderboard : listLeaderboards.getList()) {
            if (cellListLeaderboards.getSelectionModel().isSelected(leaderboard)) {
                result = leaderboard;
            }
        }
        return result;
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        trackedEventsComposite.fillEvents(result);
    }

}
