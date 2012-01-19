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
    private CellTable<EventDTO> eventTable;
    private ListDataProvider<EventDTO> eventTableProvider;
    private SingleSelectionModel<EventDTO> eventSelectionModel;

    private CaptionPanel captionPanelLeaderboards;
    
    private List<EventDTO> availableEvents;

    public OverviewEventPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, final StringMessages stringConstants) {
        super(sailingService, eventRefresher, errorReporter, stringConstants);
        availableEvents = new ArrayList<EventDTO>();

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
        {
            eventTable = new CellTable<EventDTO>();
            eventTable.setWidth("100%");
            
            //Creating location column
            TextColumn<EventDTO> locationColumn = new TextColumn<EventDTO>() {
                @Override
                public String getValue(EventDTO eventDAO) {
                    String locations = eventDAO.locations;
                    return locations != null ? locations : stringConstants.locationNotAvailable();
                }
            };
            locationColumn.setSortable(true);
            //Creating event name column
            TextColumn<EventDTO> nameColumn = new TextColumn<EventDTO>() {
                @Override
                public String getValue(EventDTO eventDAO) {
                    return eventDAO.name;
                }
            };
            nameColumn.setSortable(true);
            //Creating start date column
            TextColumn<EventDTO> startDateColumn = new TextColumn<EventDTO>() {
                @Override
                public String getValue(EventDTO eventDAO) {
                    Date start = eventDAO.regattas.get(0).races.get(0).startOfRace;
                    return start != null ? dateFormatter.render(start) : stringConstants.startDateNotAvailable();
                }
            };
            startDateColumn.setSortable(true);
            
            eventTable.addColumn(locationColumn, stringConstants.location());
            eventTable.addColumn(nameColumn, stringConstants.eventName());
            eventTable.addColumn(startDateColumn, stringConstants.startDate());
            
            //Adding the data provider and creating the sort handler
            eventTableProvider = new ListDataProvider<EventDTO>();
            eventTableProvider.addDataDisplay(eventTable);
            Handler eventSortHandler = getEventSortHandler(eventTableProvider.getList(), locationColumn, nameColumn, startDateColumn);
            eventTable.addColumnSortHandler(eventSortHandler);
            
            //Adding the selection model
            eventSelectionModel = new SingleSelectionModel<EventDTO>();
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
        // TODO
        
        loadEvents();
        
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
                eventTableProvider.getList().clear();
                eventTableProvider.setList(availableEvents);
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
    
    protected void eventSelectionChanged(SelectionChangeEvent event) {
        // TODO Actions when the event selection changed
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
        eventTableProvider.getList().clear();
        for (EventDTO event : availableEvents) {
            if (checkSearchCriteria(event)) {
                eventTableProvider.getList().add(event);
            }
        }
        // now sort again according to selected criterion
        ColumnSortEvent.fire(eventTable, eventTable.getColumnSortList());
    }
    
    private boolean checkSearchCriteria(EventDTO forEvent) {
        //TODO
        boolean result = true;
        String location = textBoxLocation.getText();
        String name = textBoxName.getText();
        boolean onlyLive = checkBoxLive.getValue();
        Date from = DateTimeFormat.getFormat("dd.MM.yyyy").parse(textBoxFrom.getText());
        Date until = DateTimeFormat.getFormat("dd.MM.yyyy").parse(textBoxUntil.getText());
        return result;
    }

    private ListHandler<EventDTO> getEventSortHandler(List<EventDTO> list, TextColumn<EventDTO> locationColumn,
            TextColumn<EventDTO> nameColumn, TextColumn<EventDTO> startDateColumn) {
        //TODO Check if comparators are correct
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
