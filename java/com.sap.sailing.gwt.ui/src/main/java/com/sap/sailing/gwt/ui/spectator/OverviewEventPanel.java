package com.sap.sailing.gwt.ui.spectator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
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
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.client.AbstractEventPanel;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettingsFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

/**
 * 
 * @author Lennart Hensler (D054527)
 * 
 */
public class OverviewEventPanel extends AbstractEventPanel {
    
    private TextBox locationTextBox;
    private TextBox nameTextBox;
    private TextBox fromTextBox;
    private TextBox untilTextBox;
    private CheckBox onlyLiveCheckBox;
    
    private CaptionPanel eventsCaptionPanel;
    private Button showLeaderboardsBtn;
    private CellTable<EventDTO> eventsTable;
    private ListDataProvider<EventDTO> eventsTableProvider;
    private SingleSelectionModel<EventDTO> eventsTableSelectionModel;

    private CaptionPanel leaderboardsCaptionPanel;
    private Button showLeaderboardBtn;
    private CellList<LeaderboardDTO> leaderboardsList;
    private ListDataProvider<LeaderboardDTO> leaderboardsListProvider;
    private SingleSelectionModel<LeaderboardDTO> leaderboardsListSelectionModel;
    
    private CaptionPanel leaderboardCaptionPanel;
    private VerticalPanel leaderboardPanel;
    private LeaderboardDTO currentLeaderboard;
    private LeaderboardPanel displayedLeaderboardPanel;
    
    private List<EventDTO> availableEvents;
    private List<LeaderboardDTO> availableLeaderboards;

    public OverviewEventPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, final StringMessages stringConstants) {
        super(sailingService, eventRefresher, errorReporter, stringConstants);
        availableEvents = new ArrayList<EventDTO>();
        availableLeaderboards = new ArrayList<LeaderboardDTO>();

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("95%");

        // Build search GUI
        CaptionPanel searchCaptionPanel = new CaptionPanel(stringConstants.searchEvents());
        searchCaptionPanel.setWidth("100%");
        mainPanel.add(searchCaptionPanel);
        
        HorizontalPanel searchFunctionalPanel = new HorizontalPanel();
        searchCaptionPanel.add(searchFunctionalPanel);
        searchFunctionalPanel.setWidth("100%");
        
        Label locationLbl = new Label(stringConstants.location() + ":");
        searchFunctionalPanel.add(locationLbl);
        locationTextBox = new TextBox();
        locationTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchFunctionalPanel.add(locationTextBox);
        
        Label nameLbl = new Label(stringConstants.name() + ":");
        searchFunctionalPanel.add(nameLbl);
        nameTextBox = new TextBox();
        nameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchFunctionalPanel.add(nameTextBox);
        
        onlyLiveCheckBox = new CheckBox(stringConstants.onlyLiveEvents());
        onlyLiveCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                onCheckBoxLiveChange();
            }
        });
        searchFunctionalPanel.add(onlyLiveCheckBox);
        
        Label fromDateLbl = new Label(stringConstants.from() + ":");
        searchFunctionalPanel.add(fromDateLbl);
        fromTextBox = new TextBox();
        fromTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchFunctionalPanel.add(fromTextBox);
        
        Label toDateLbl = new Label(stringConstants.until() + ":");
        searchFunctionalPanel.add(toDateLbl);
        untilTextBox = new TextBox();
        untilTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchFunctionalPanel.add(untilTextBox);

        HorizontalPanel listsSplitPanel = new HorizontalPanel();
        mainPanel.add(listsSplitPanel);
        listsSplitPanel.setWidth("100%");
        
        // Build events GUI
        eventsCaptionPanel = new CaptionPanel(stringConstants.events());
        eventsCaptionPanel.setWidth("100%");
        listsSplitPanel.add(eventsCaptionPanel);

        VerticalPanel eventsPanel = new VerticalPanel();
        eventsCaptionPanel.setContentWidget(eventsPanel);
        eventsPanel.setWidth("100%");

        // Create event functional elements
        HorizontalPanel eventsFunctionPanel = new HorizontalPanel();
        eventsFunctionPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        eventsFunctionPanel.setSpacing(5);
        eventsPanel.add(eventsFunctionPanel);
        
        Button refreshEventListBtn = new Button(stringConstants.refresh());
        refreshEventListBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshEventsTable();
            }
        });
        eventsFunctionPanel.add(refreshEventListBtn);

        showLeaderboardsBtn = new Button(stringConstants.showDetails() + " >");
        showLeaderboardsBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent c) {
                if (eventsTableSelectionModel.getSelectedObject() != null) {
                    setLeaderboardsPanelVisible(true);
                } else {
                    Window.alert(stringConstants.noEventSelected());
                }
            }
        });
        showLeaderboardsBtn.setEnabled(false);
        eventsFunctionPanel.add(showLeaderboardsBtn);

        // Create event table
        eventsTable = new CellTable<EventDTO>();
        eventsTable.setWidth("100%");

        // Creating location column
        TextColumn<EventDTO> locationColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO eventDTO) {
                String locations = eventDTO.locations;
                return locations != null ? locations : stringConstants.locationNotAvailable();
            }
        };
        locationColumn.setSortable(true);
        // Creating event name column
        TextColumn<EventDTO> nameColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO eventDTO) {
                return eventDTO.name;
            }
        };
        nameColumn.setSortable(true);
        // Creating start date column
        TextColumn<EventDTO> startDateColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO eventDTO) {
                Date start = eventDTO.regattas.get(0).races.get(0).startOfRace;
                return start != null ? dateFormatter.render(start) : stringConstants.startDateNotAvailable();
            }
        };
        startDateColumn.setSortable(true);

        eventsTable.addColumn(locationColumn, stringConstants.location());
        eventsTable.addColumn(nameColumn, stringConstants.eventName());
        eventsTable.addColumn(startDateColumn, stringConstants.startDate());

        // Adding the data provider and creating the sort handler
        eventsTableProvider = new ListDataProvider<EventDTO>();
        eventsTableProvider.addDataDisplay(eventsTable);
        Handler eventSortHandler = getEventSortHandler(eventsTableProvider.getList(), locationColumn, nameColumn,
                startDateColumn);
        eventsTable.addColumnSortHandler(eventSortHandler);

        // Adding the selection model
        eventsTableSelectionModel = new SingleSelectionModel<EventDTO>();
        eventsTable.setSelectionModel(eventsTableSelectionModel);
        eventsTableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                eventSelectionChanged();
            }
        });

        eventsPanel.add(eventsTable);

        // Build leaderboards GUI
        leaderboardsCaptionPanel = new CaptionPanel(stringConstants.leaderboards());
        leaderboardsCaptionPanel.setVisible(false);
        leaderboardsCaptionPanel.setWidth("100%");
        listsSplitPanel.add(leaderboardsCaptionPanel);

        VerticalPanel leaderboardsPanel = new VerticalPanel();
        leaderboardsCaptionPanel.setContentWidget(leaderboardsPanel);
        leaderboardsPanel.setWidth("100%");

        // Create leaderboard functional elements
        HorizontalPanel leaderboardsFunctionPanel = new HorizontalPanel();
        leaderboardsFunctionPanel.setSpacing(5);
        leaderboardsPanel.add(leaderboardsFunctionPanel);

        Button hideLeaderboardsBtn = new Button("< " + stringConstants.hideLeaderboards());
        hideLeaderboardsBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setLeaderboardsPanelVisible(false);
            }
        });
        leaderboardsFunctionPanel.add(hideLeaderboardsBtn);
        
        showLeaderboardBtn = new Button("v " + stringConstants.showLeaderboard());
        showLeaderboardBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (leaderboardsListSelectionModel.getSelectedObject() != null) {
                    setLeaderboardPanelVisible(true);
                } else {
                    Window.alert(stringConstants.noLeaderboardSelected());
                }
            }
        });
        showLeaderboardBtn.setEnabled(false);
        leaderboardsFunctionPanel.add(showLeaderboardBtn);

        // Create leaderboard list
        Cell<LeaderboardDTO> leaderboardCell = new AbstractCell<LeaderboardDTO>() {
            @Override
            public void render(Context context, LeaderboardDTO leaderboard, SafeHtmlBuilder safeHtmlBuilder) {
                safeHtmlBuilder.append(SafeHtmlUtils.fromString(leaderboard.name));
            }
        };
        leaderboardsList = new CellList<LeaderboardDTO>(leaderboardCell);
        
        leaderboardsListProvider = new ListDataProvider<LeaderboardDTO>();
        leaderboardsListProvider.addDataDisplay(leaderboardsList);
        
        leaderboardsListSelectionModel = new SingleSelectionModel<LeaderboardDTO>();
        leaderboardsListSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                leaderboardSelectionChanged();
            }
        });
        leaderboardsList.setSelectionModel(leaderboardsListSelectionModel);
        
        leaderboardsPanel.add(leaderboardsList);
        
        //Build leaderboard GUI
        leaderboardCaptionPanel = new CaptionPanel();
        leaderboardCaptionPanel.setVisible(false);
        leaderboardCaptionPanel.setWidth("100%");
        mainPanel.add(leaderboardCaptionPanel);
        
        leaderboardPanel = new VerticalPanel();
        leaderboardPanel.setWidth("100%");
        leaderboardCaptionPanel.add(leaderboardPanel);
        
        HorizontalPanel leaderboardFunctionPanel = new HorizontalPanel();
        leaderboardFunctionPanel.setWidth("100%");
        leaderboardPanel.add(leaderboardFunctionPanel);
        
        Button hideLeaderboardBtn = new Button("^ " + stringConstants.hideLeaderboard());
        hideLeaderboardBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                setLeaderboardPanelVisible(false);
            }
        });
        leaderboardFunctionPanel.add(hideLeaderboardBtn);
        
        displayedLeaderboardPanel = null;
        
        //Loading the data
        Runnable displayEvents = new Runnable() {
            @Override
            public void run() {
                eventsTableProvider.getList().clear();
                eventsTableProvider.getList().addAll(availableEvents);
            }
        };
        loadEvents(displayEvents);
        
        //Set checkbox as true, because we can't search for old events right now
        //TODO Remove after searching for old events is possible
        onlyLiveCheckBox.setValue(true, true);
        onlyLiveCheckBox.setEnabled(false);
        //Until here
    }

    private void loadEvents(final Runnable actionAfterLoading) {
        sailingService.listEvents(true, new AsyncCallback<List<EventDTO>>() {

            @Override
            public void onSuccess(List<EventDTO> result) {
                if (result != null) {
                    availableEvents = new ArrayList<EventDTO>(result);
                } else {
                    availableEvents.clear();
                }
                if (actionAfterLoading != null) {
                    actionAfterLoading.run();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to obtain list of events: " + caught.getMessage());
            }
        });
    }
    
    private void refreshEventsTable() {
        final EventDTO selectedEvent = eventsTableSelectionModel.getSelectedObject();
        
        //Clear Search criteria
        locationTextBox.setText("");
        nameTextBox.setText("");
        fromTextBox.setText("");
        untilTextBox.setText("");
        //TODO Switch the value of checkBoxLive to false, as soon as searching for old events is possible
        onlyLiveCheckBox.setValue(true);
        
        //Load all available events and reselect the event
        Runnable displayAndReselect = new Runnable() {
            @Override
            public void run() {
                eventsTableProvider.getList().clear();
                eventsTableProvider.getList().addAll(availableEvents);
                //Now sort again according to selected criterion
                ColumnSortEvent.fire(eventsTable, eventsTable.getColumnSortList());
                //Reselect the event
                if (selectedEvent != null) {
                    for (EventDTO event : eventsTableProvider.getList()) {
                        if (event.equals(selectedEvent)) {
                            eventsTableSelectionModel.setSelected(selectedEvent, true);
                            break;
                        }
                    }
                }
            }
        };
        loadEvents(displayAndReselect);
        //Now sort again according to selected criterion
        ColumnSortEvent.fire(eventsTable, eventsTable.getColumnSortList());
    }
    
    private void setLeaderboardsPanelVisible(boolean visible) {
        leaderboardsCaptionPanel.setVisible(visible);
        eventsCaptionPanel.setWidth(visible ? "95%" : "100%");
        showLeaderboardsBtn.setEnabled(!visible);
    }
    
    private void eventSelectionChanged() {
        EventDTO selectedEvent = eventsTableSelectionModel.getSelectedObject();
        if (selectedEvent != null) {
            setLeaderboardsPanelVisible(true);

            Runnable displayLeaderboards = new Runnable() {
                @Override
                public void run() {
                    leaderboardsListProvider.getList().clear();
                    leaderboardsListProvider.getList().addAll(availableLeaderboards);
                }
            };
            loadLeaderboards(selectedEvent, displayLeaderboards);
        } else {
            setLeaderboardsPanelVisible(false);
            showLeaderboardsBtn.setEnabled(false);
            setLeaderboardPanelVisible(false);
        }
    }
    
    private void loadLeaderboards(EventDTO forEvent, final Runnable actionAfterLoading) {
        sailingService.getLeaderboardsByEvent(forEvent, new AsyncCallback<List<LeaderboardDTO>>() {

            @Override
            public void onSuccess(List<LeaderboardDTO> leaderboards) {
                if (leaderboards != null) {
                    availableLeaderboards = leaderboards;
                } else {
                    availableLeaderboards.clear();
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
    
    private void leaderboardSelectionChanged() {
        LeaderboardDTO selectedLeaderboard = leaderboardsListSelectionModel.getSelectedObject();
        if (selectedLeaderboard != null) {
            setDisplayedLeaderboard(selectedLeaderboard);
            setLeaderboardPanelVisible(true);
        } else {
            setLeaderboardPanelVisible(false);
            showLeaderboardBtn.setEnabled(false);
        }
    }
    
    private void setLeaderboardPanelVisible(boolean visible) {
        leaderboardCaptionPanel.setVisible(visible);
        showLeaderboardBtn.setEnabled(!visible);
    }
    
    private void setDisplayedLeaderboard(LeaderboardDTO boardToDisplay) {
        //If the currentLeaderboard equals the boardToDisplay, there is no need to create a new LeaderboardPanel
        if (!boardToDisplay.equals(currentLeaderboard)) {
            if (displayedLeaderboardPanel != null) {
                leaderboardPanel.remove(displayedLeaderboardPanel);
            }
            currentLeaderboard = boardToDisplay;
            CompetitorSelectionModel competitorSelectionModel = new CompetitorSelectionModel(true);
            displayedLeaderboardPanel = new LeaderboardPanel(sailingService, LeaderboardSettingsFactory.getInstance()
                    .createNewDefaultSettings(/* autoExpandFirstRace */false), competitorSelectionModel,
                    currentLeaderboard.name, null, errorReporter, stringConstants, null);
            leaderboardPanel.add(displayedLeaderboardPanel);
        }
    }
    
    private void onCheckBoxLiveChange() {
        if (onlyLiveCheckBox.getValue()) {
            String today = DateTimeFormat.getFormat("dd.MM.yyyy").format(new Date()).toString();
            
            fromTextBox.setText(today);
            fromTextBox.setEnabled(false);
            untilTextBox.setText(today);
            untilTextBox.setEnabled(false);
        } else {
            fromTextBox.setText("");
            fromTextBox.setEnabled(true);
            untilTextBox.setText("");
            untilTextBox.setEnabled(true);
        }
        onSearchCriteriaChange();
    }
    
    private void onSearchCriteriaChange() {
        //Get search criteria
        String location = locationTextBox.getText();
        String name = nameTextBox.getText();
        boolean onlyLive = onlyLiveCheckBox.getValue();
        Date from = null;
        Date until = null;
        try {
            from = DateTimeFormat.getFormat("dd.MM.yyyy").parse(fromTextBox.getText());
            until = DateTimeFormat.getFormat("dd.MM.yyyy").parse(untilTextBox.getText());
            //Adding 24 hours to until, so that it doesn't result in an empty table if 'from' and 'until' are equal.
            //Instead you'll have a 24 hour range
            long time = until.getTime() + 24 * 60 * 60 * 1000;
            until = new Date(time);
        } catch (IllegalArgumentException e) {}
        //Filter list by criteria
        eventsTableProvider.getList().clear();
        for (EventDTO event : availableEvents) {
            if (checkSearchCriteria(event, location, name, onlyLive, from, until)) {
                eventsTableProvider.getList().add(event);
            }
        }
        //Now sort again according to selected criterion
        ColumnSortEvent.fire(eventsTable, eventsTable.getColumnSortList());
    }
    
    private boolean checkSearchCriteria(EventDTO forEvent, String location, String name, boolean onlyLive, Date from, Date until) {
        boolean result = true;
        
        if (result && !location.equals("")) {
            result = !textContainingStringsToCheck(Arrays.asList(location.split("\\s")), forEvent.locations);
        }
        if (result && !name.equals("")) {
            result = !textContainingStringsToCheck(Arrays.asList(name.split("\\s")), forEvent.name);
        }
        //If only live events are allowed the check of the dates isn't needed
        if (result && onlyLive) {
            result = forEvent.currentlyTracked();
        } else if (result) {
            Date startDate = forEvent.getStartDate();
            if (from != null && until != null) {
                result = from.before(startDate) && until.after(startDate); 
            } else if (from != null) {
                result = from.before(startDate);
            } else if (until != null) {
                result = until.after(startDate); 
            }
        }
        
        return result;
    }

    private ListHandler<EventDTO> getEventSortHandler(List<EventDTO> list, TextColumn<EventDTO> locationColumn,
            TextColumn<EventDTO> nameColumn, TextColumn<EventDTO> startDateColumn) {
        
        ListHandler<EventDTO> sortHandler = new ListHandler<EventDTO>(list);
        sortHandler.setComparator(locationColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return e1.locations == null ? e2.locations == null ? 0 : -1 : e2.locations == null ? 1 : e1.locations
                        .compareTo(e2.locations);
            }
        });
        sortHandler.setComparator(nameColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return e1.name.compareTo(e2.name);
            }
        });
        sortHandler.setComparator(startDateColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return e1.getStartDate().compareTo(e2.getStartDate());
            }
        });
        return sortHandler;
    }

    @Override
    public void fillEvents(List<EventDTO> result) {
    }

}
