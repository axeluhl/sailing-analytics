package com.sap.sailing.gwt.ui.spectator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.client.AbstractEventPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.server.api.EventIdentifier;

/**
 * 
 * @author Lennart Hensler (D054527)
 * 
 */
public class OverviewEventPanel extends AbstractEventPanel {

    private TextBox textBoxLocation;
    private TextBox textBoxName;
    private TextBox textBoxFrom;
    private TextBox textBoxUntil;
    private CheckBox checkBoxLive;
    
    private CaptionPanel captionPanelEvents;
    private Button btnShowLeaderboards;
    private CellTable<EventDAO> eventTable;
    private ListDataProvider<EventDAO> eventTableProvider;
    private SingleSelectionModel<EventDAO> eventSelectionModel;

    private CaptionPanel captionPanelLeaderboards;
    
    private List<EventDAO> availableEvents;

    public OverviewEventPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, final StringMessages stringConstants) {
        super(sailingService, eventRefresher, errorReporter, stringConstants);
        availableEvents = new ArrayList<EventDAO>();

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("95%");

        // Build search GUI
        CaptionPanel captionPanelSearch = new CaptionPanel(stringConstants.searchEvents());
        mainPanel.add(captionPanelSearch);
        captionPanelSearch.setWidth("100%");
        
        HorizontalPanel panelSearch = new HorizontalPanel();
        captionPanelSearch.add(panelSearch);
        panelSearch.setWidth("100%");
        
        Label lblLocation = new Label(stringConstants.location() + ":");
        panelSearch.add(lblLocation);
        textBoxLocation = new TextBox();
        textBoxLocation.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange(event.getSource());
            }
        });
        panelSearch.add(textBoxLocation);
        
        Label lblName = new Label(stringConstants.name() + ":");
        panelSearch.add(lblName);
        textBoxName = new TextBox();
        textBoxName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange(event.getSource());
            }
        });
        panelSearch.add(textBoxName);
        
        checkBoxLive = new CheckBox(stringConstants.onlyLiveEvents());
        checkBoxLive.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                onCheckBoxLiveChange();
            }
        });
        panelSearch.add(checkBoxLive);
        
        Label lblFromDate = new Label(stringConstants.from() + ":");
        panelSearch.add(lblFromDate);
        textBoxFrom = new TextBox();
        textBoxFrom.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange(event.getSource());
            }
        });
        panelSearch.add(textBoxFrom);
        
        Label lblToDate = new Label(stringConstants.until() + ":");
        panelSearch.add(lblToDate);
        textBoxUntil = new TextBox();
        textBoxUntil.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange(event.getSource());
            }
        });
        panelSearch.add(textBoxUntil);

        // Build events GUI
        HorizontalPanel listsSplitPanel = new HorizontalPanel();
        mainPanel.add(listsSplitPanel);
        listsSplitPanel.setWidth("100%");
        
        captionPanelEvents = new CaptionPanel(stringConstants.events());
        captionPanelEvents.setWidth("100%");
        listsSplitPanel.add(captionPanelEvents);

        VerticalPanel eventsPanel = new VerticalPanel();
        captionPanelEvents.setContentWidget(eventsPanel);
        eventsPanel.setWidth("100%");

        // Create event functional elements
        HorizontalPanel functionPanelEvents = new HorizontalPanel();
        functionPanelEvents.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        functionPanelEvents.setSpacing(5);
        eventsPanel.add(functionPanelEvents);

        btnShowLeaderboards = new Button(">");
        btnShowLeaderboards.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent c) {
                if (eventSelectionModel.getSelectedObject() != null) {
                    captionPanelLeaderboards.setVisible(true);
                } else {
                    Window.alert(stringConstants.noEventSelected());
                }
            }
        });
        btnShowLeaderboards.setEnabled(false);
        functionPanelEvents.add(btnShowLeaderboards);

        // Create event table
        // TODO
        {
            eventTable = new CellTable<EventDAO>();
            eventTable.setWidth("100%");
            
            //Creating location column
            TextColumn<EventDAO> locationColumn = new TextColumn<EventDAO>() {
                @Override
                public String getValue(EventDAO eventDAO) {
                    String locations = eventDAO.getLocationAsString();
                    return locations != null ? locations : stringConstants.locationNotAvailable();
                }
            };
            //Creating event name column
            TextColumn<EventDAO> nameColumn = new TextColumn<EventDAO>() {
                @Override
                public String getValue(EventDAO eventDAO) {
                    return eventDAO.name;
                }
            };
            //Creating start date column
            TextColumn<EventDAO> startDateColumn = new TextColumn<EventDAO>() {
                @Override
                public String getValue(EventDAO eventDAO) {
                    Date start = eventDAO.regattas.get(0).races.get(0).startOfRace;
                    return start != null ? start.toString() : stringConstants.startDateNotAvailable();
                }
            };
            
            eventTable.addColumn(locationColumn, stringConstants.location());
            eventTable.addColumn(nameColumn, stringConstants.eventName());
            eventTable.addColumn(startDateColumn, stringConstants.startDate());
            
            //Adding the data provider and creating the sort handler
            eventTableProvider = new ListDataProvider<EventDAO>();
            eventTableProvider.addDataDisplay(eventTable);
            Handler eventSortHandler = getEventSortHandler(eventTableProvider.getList(), locationColumn, nameColumn, startDateColumn);
            eventTable.addColumnSortHandler(eventSortHandler);
            
            //Adding the selection model
            eventSelectionModel = new SingleSelectionModel<EventDAO>();
            eventTable.setSelectionModel(eventSelectionModel);
            eventSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    eventSelectionChanged(event);
                }
            });
            
            eventsPanel.add(eventTable);
        }

        // Build leaderboards GUI
        captionPanelLeaderboards = new CaptionPanel(stringConstants.leaderboards());
        captionPanelLeaderboards.setVisible(false);
        captionPanelLeaderboards.setWidth("95%");
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
            public void onClick(ClickEvent event) {
                captionPanelLeaderboards.setVisible(false);
            }
        });
        functionPanelLeaderboards.add(btnHideLeaderboards);

        // Create leaderboard list
        //  TODO
        
        loadEvents();
        
        //Set checkbox as true, because we can't search for old events right now
        //TODO Remove after searching for old events is possible
        checkBoxLive.setValue(true, true);
        checkBoxLive.setEnabled(false);
        //Until here
    }

    private ListHandler<EventDAO> getEventSortHandler(List<EventDAO> list, TextColumn<EventDAO> locationColumn,
            TextColumn<EventDAO> nameColumn, TextColumn<EventDAO> startDateColumn) {
        ListHandler<EventDAO> sortHandler = new ListHandler<EventDAO>(list);
        sortHandler.setComparator(locationColumn, new Comparator<EventDAO>() {
            @Override
            public int compare(EventDAO e1, EventDAO e2) {
                return e1.getLocationAsString().compareTo(e2.getLocationAsString());
            }
        });
        sortHandler.setComparator(nameColumn, new Comparator<EventDAO>() {
            @Override
            public int compare(EventDAO e1, EventDAO e2) {
                return e1.name.compareTo(e2.name);
            }
        });
        sortHandler.setComparator(startDateColumn, new Comparator<EventDAO>() {
            @Override
            public int compare(EventDAO e1, EventDAO e2) {
                return e1.getStartDate().compareTo(e2.getStartDate());
            }
        });
        return sortHandler;
    }

    private void loadEvents(final Runnable actionAfterLoading) {
        sailingService.listEvents(true, new AsyncCallback<List<EventDAO>>() {

            @Override
            public void onSuccess(List<EventDAO> result) {
                if (result != null) {
                    availableEvents = new ArrayList<EventDAO>(result);
                    eventTableProvider.getList().clear();
                    eventTableProvider.setList(availableEvents);
                } else {
                    availableEvents.clear();
                }
                if (actionAfterLoading != null) {
                    actionAfterLoading.run();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to obtain list of leaderboards: " + caught.getMessage());
            }
        });
    }
    private void loadEvents() {
        loadEvents(null);
    }
    
    private void fillEventsList(final Runnable r) {
        // TODO
    }
    private void fillEventsList() {
        fillEventsList(null);
    }

    private void fillLeaderboardsList(EventIdentifier eventIdentifier, final Runnable r) {
        // TODO
    }
    private void loadLeaderboards(EventIdentifier eventIdentifier) {
        fillLeaderboardsList(eventIdentifier, null);
    }

    protected void eventSelectionChanged(SelectionChangeEvent event) {
        // TODO Actions when the event selection changed
    }
    
    private LeaderboardDAO getSelectedLeaderboard() {
        LeaderboardDAO result = null;
        //TODO Get seleceted Leaderboard
        return result;
    }

    private void onCheckBoxLiveChange() {
        if (checkBoxLive.getValue()) {
            //Disable Date-Input-Fields and fill them with the current Date
            //Event list will be refreshed by the listeners of the Date-Input-Fields
            String actualDate = DateTimeFormat.getFormat("dd.MM.yyyy").format(new Date());
            
            textBoxFrom.setValue(actualDate, true);
            textBoxFrom.setEnabled(false);

            textBoxUntil.setValue(actualDate, true);
            textBoxUntil.setEnabled(false);
        } else {
            textBoxFrom.setEnabled(true);
            textBoxUntil.setEnabled(true);
        }
    }
    
    private void onSearchCriteriaChange(Object source) {
        //TODO Refresh event list
        if (source.equals(textBoxLocation)) {
            
        } else if (source.equals(textBoxName)) {
            
        } else if (source.equals(textBoxFrom)) {
            
        } else if (source.equals(textBoxUntil)) {
            
        }
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        
    }

}
