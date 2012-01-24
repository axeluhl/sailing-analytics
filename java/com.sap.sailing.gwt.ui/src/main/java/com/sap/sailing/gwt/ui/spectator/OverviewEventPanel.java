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
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

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
    
    private CaptionPanel eventsCaptionPanel;
    private Button btnShowLeaderboards;
    private CellTable<EventDTO> eventsTable;
    private ListDataProvider<EventDTO> eventsTableProvider;
    private SingleSelectionModel<EventDTO> eventsTableSelectionModel;

    private CaptionPanel leaderboardsCaptionPanel;
    private CellList<LeaderboardDTO> leaderboardsList;
    private ListDataProvider<LeaderboardDTO> leaderboardsListProvider;
    private SingleSelectionModel<LeaderboardDTO> leaderboardsListSelectionModel;
    
    private CaptionPanel leaderboardCaptionPanel;
    
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
                onSearchCriteriaChange();
            }
        });
        panelSearch.add(textBoxLocation);
        
        Label lblName = new Label(stringConstants.name() + ":");
        panelSearch.add(lblName);
        textBoxName = new TextBox();
        textBoxName.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
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
                onSearchCriteriaChange();
            }
        });
        panelSearch.add(textBoxFrom);
        
        Label lblToDate = new Label(stringConstants.until() + ":");
        panelSearch.add(lblToDate);
        textBoxUntil = new TextBox();
        textBoxUntil.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        panelSearch.add(textBoxUntil);

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
        HorizontalPanel functionPanelEvents = new HorizontalPanel();
        functionPanelEvents.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        functionPanelEvents.setSpacing(5);
        eventsPanel.add(functionPanelEvents);
        
        Button btnRefreshEventList = new Button(stringConstants.refresh());
        btnRefreshEventList.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshEventsTable();
            }
        });
        functionPanelEvents.add(btnRefreshEventList);

        btnShowLeaderboards = new Button(">");
        btnShowLeaderboards.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent c) {
                if (eventsTableSelectionModel.getSelectedObject() != null) {
                    setLeaderboardsPanelVisible(true);
                } else {
                    Window.alert(stringConstants.noEventSelected());
                }
            }
        });
        btnShowLeaderboards.setEnabled(false);
        functionPanelEvents.add(btnShowLeaderboards);

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
        HorizontalPanel functionPanelLeaderboards = new HorizontalPanel();
        functionPanelLeaderboards.setSpacing(5);
        leaderboardsPanel.add(functionPanelLeaderboards);

        Button btnHideLeaderboards = new Button("<");
        btnHideLeaderboards.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                setLeaderboardsPanelVisible(false);
            }
        });
        functionPanelLeaderboards.add(btnHideLeaderboards);

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
        
        //Create leaderboard container
        //TODO
        
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
        checkBoxLive.setValue(true, true);
        checkBoxLive.setEnabled(false);
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
        textBoxLocation.setText("");
        textBoxName.setText("");
        textBoxFrom.setText("");
        textBoxUntil.setText("");
        //TODO Switch the value of checkBoxLive to false, as soon as searching for old events is possible
        checkBoxLive.setValue(true);
        
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
        btnShowLeaderboards.setEnabled(!visible);
    }
    
    private void eventSelectionChanged() {
        // TODO Actions when the event selection changed
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
            btnShowLeaderboards.setEnabled(false);
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
        //TODO Actions when the leaderboard selection changes
    }
    
    private void onCheckBoxLiveChange() {
        if (checkBoxLive.getValue()) {
            String today = DateTimeFormat.getFormat("dd.MM.yyyy").format(new Date()).toString();
            
            textBoxFrom.setText(today);
            textBoxFrom.setEnabled(false);
            textBoxUntil.setText(today);
            textBoxUntil.setEnabled(false);
        } else {
            textBoxFrom.setText("");
            textBoxFrom.setEnabled(true);
            textBoxUntil.setText("");
            textBoxUntil.setEnabled(true);
        }
        onSearchCriteriaChange();
    }
    
    private void onSearchCriteriaChange() {
        //Get search criteria
        String location = textBoxLocation.getText();
        String name = textBoxName.getText();
        boolean onlyLive = checkBoxLive.getValue();
        Date from = null;
        Date until = null;
        try {
            from = DateTimeFormat.getFormat("dd.MM.yyyy").parse(textBoxFrom.getText());
            until = DateTimeFormat.getFormat("dd.MM.yyyy").parse(textBoxUntil.getText());
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
                return e1.locations.compareTo(e2.locations);
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
