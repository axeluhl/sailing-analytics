package com.sap.sailing.gwt.ui.spectator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RacePlaceOrder;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorCell;
import com.sap.sailing.gwt.ui.client.AbstractEventPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.ParallelExecutionCallback;
import com.sap.sailing.gwt.ui.client.ParallelExecutionHolder;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLFactory;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

/**
 * 
 * @author Lennart Hensler (D054527)
 * 
 */
public class LeaderboardGroupOverviewPanel extends AbstractEventPanel {

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml cell(String url, String displayName);
    }

    public static final String STYLE_NAME_PREFIX = "groupOverviewPanel-";
    private static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    
    private TextBox locationTextBox;
    private TextBox nameTextBox;
    private TextBox fromTextBox;
    private TextBox untilTextBox;
    private CheckBox onlyLiveCheckBox;
    
    private CellTable<LeaderboardGroupDTO> groupsTable;
    private ListDataProvider<LeaderboardGroupDTO> groupsDataProvider;
    private SingleSelectionModel<LeaderboardGroupDTO> groupsSelectionModel;
    
    private List<LeaderboardGroupDTO> availableGroups;
    private HashMap<RaceIdentifier, Date> availableRaceStartDates;
    private HashMap<RaceIdentifier, RacePlaceOrder> availabeRaceLocations;

    public LeaderboardGroupOverviewPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, final StringMessages stringConstants) {
        super(sailingService, eventRefresher, errorReporter, stringConstants);
        
        availableGroups = new ArrayList<LeaderboardGroupDTO>();
        availableRaceStartDates = new HashMap<RaceIdentifier, Date>();
        availabeRaceLocations = new HashMap<RaceIdentifier, RacePlaceOrder>();

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
        
        //Build group GUI
        FlowPanel groupPanel = new FlowPanel();
        groupPanel.setStyleName(STYLE_NAME_PREFIX + "groupPanel");
        mainPanel.add(groupPanel);
        
        groupsTable = new CellTable<LeaderboardGroupDTO>();
        groupsTable.setWidth("100%");
        groupPanel.add(groupsTable);
        
        groupsDataProvider = new ListDataProvider<LeaderboardGroupDTO>();
        ListHandler<LeaderboardGroupDTO> groupsListHandler = new ListHandler<LeaderboardGroupDTO>(groupsDataProvider.getList());
        groupsDataProvider.addDataDisplay(groupsTable);
        
        groupsSelectionModel = new SingleSelectionModel<LeaderboardGroupDTO>();
        groupsTable.setSelectionModel(groupsSelectionModel);
        
        TextColumn<LeaderboardGroupDTO> groupsLocationColumn = new TextColumn<LeaderboardGroupDTO>() {
            @Override
            public String getValue(LeaderboardGroupDTO group) {
                // TODO Figure out a better way to save the locations
                return "Not Available";
            }
        };
        groupsLocationColumn.setSortable(false);
//        groupsLocationColumn.setSortable(true);
//        groupsListHandler.setComparator(groupsLocationColumn, new Comparator<LeaderboardGroupDTO>() {
//            @Override
//            public int compare(LeaderboardGroupDTO g1, LeaderboardGroupDTO g2) {
//                // TODO Auto-generated method stub
//                return 0;
//            }
//        });
        
        AnchorCell groupsNameAnchorCell = new AnchorCell();
        Column<LeaderboardGroupDTO, SafeHtml> groupsNameColumn = new Column<LeaderboardGroupDTO, SafeHtml>(groupsNameAnchorCell) {
            @Override
            public SafeHtml getValue(LeaderboardGroupDTO group) {
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = URLFactory.INSTANCE.encode("/gwt/Spectator.html?leaderboardGroupName=" + group.name
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                return ANCHORTEMPLATE.cell(link, group.name);
            }
        };
        groupsNameColumn.setSortable(true);
        groupsListHandler.setComparator(groupsNameColumn, new Comparator<LeaderboardGroupDTO>() {
            @Override
            public int compare(LeaderboardGroupDTO g1, LeaderboardGroupDTO g2) {
                return g1.name.compareTo(g2.name);
            }
        });
        
        TextColumn<LeaderboardGroupDTO> groupsLeaderboardsColumn = new TextColumn<LeaderboardGroupDTO>() {
            @Override
            public String getValue(LeaderboardGroupDTO group) {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (LeaderboardDTO leaderboard : group.leaderboards) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append(leaderboard.name);
                    first = false;
                }
                return sb.toString();
            }
        };
        groupsLeaderboardsColumn.setSortable(false);
        
        TextColumn<LeaderboardGroupDTO> groupsStartDateColumn = new TextColumn<LeaderboardGroupDTO>() {
            @Override
            public String getValue(LeaderboardGroupDTO group) {
                //TODO Figure out a better way to save the start dates
                RaceIdentifier race = getFirstTrackedRaceInGroup(group);
                Date startDate = race == null ? null : availableRaceStartDates.get(race);
                return startDate == null ? stringConstants.untracked() : DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(startDate);
            }
        };
        groupsStartDateColumn.setSortable(true);
        groupsListHandler.setComparator(groupsStartDateColumn, new Comparator<LeaderboardGroupDTO>() {
            @Override
            public int compare(LeaderboardGroupDTO g1, LeaderboardGroupDTO g2) {
                RaceIdentifier race1 = getFirstTrackedRaceInGroup(g1);
                Date startDate1 = race1 == null ? null : availableRaceStartDates.get(race1);

                RaceIdentifier race2 = getFirstTrackedRaceInGroup(g2);
                Date startDate2 = race2 == null ? null : availableRaceStartDates.get(race2);
                return startDate1.compareTo(startDate2);
            }
        });
        
        groupsTable.addColumn(groupsLocationColumn, stringConstants.location());
        groupsTable.addColumn(groupsNameColumn, stringConstants.name());
        groupsTable.addColumn(groupsLeaderboardsColumn, stringConstants.leaderboards());
        groupsTable.addColumn(groupsStartDateColumn, stringConstants.startDate());
        
        //Build group details GUI TODO
        
        //Loading the data
        loadData();
        
        //Set checkbox as true, because we can't search for old events right now
        //TODO Remove after searching for old events is possible
        onlyLiveCheckBox.setValue(true, true);
        onlyLiveCheckBox.setEnabled(false);
        //Until here
    }

    private void loadData() {
        final ParallelExecutionCallback<List<LeaderboardGroupDTO>> getGroupsCallback = new ParallelExecutionCallback<List<LeaderboardGroupDTO>>();
        final ParallelExecutionCallback<List<EventDTO>> getEventsCallback = new ParallelExecutionCallback<List<EventDTO>>();
        new ParallelExecutionHolder(getGroupsCallback, getEventsCallback) {
            @Override
            protected void handleSuccess() {
                //Update the groups
                availableGroups = getGroupsCallback.getData() == null ? new ArrayList<LeaderboardGroupDTO>() : getGroupsCallback.getData();
                groupsDataProvider.getList().clear();
                groupsDataProvider.getList().addAll(availableGroups);
                
                //Add the additional data, like the start time and the location
                List<EventDTO> events = getEventsCallback.getData();
                for (EventDTO event : events) {
                    for (RegattaDTO regatta : event.regattas) {
                        for (RaceDTO race : regatta.races) {
                            for (LeaderboardGroupDTO group : availableGroups) {
                                if (group.containsRace(race.getRaceIdentifier())) {
                                    availableRaceStartDates.put(race.getRaceIdentifier(), race.startOfRace);
                                    availabeRaceLocations.put(race.getRaceIdentifier(), race.racePlaces);
                                }
                            }
                        }
                    }
                }
            }
            @Override
            protected void handleFailure(Throwable t) {
                errorReporter.reportError("Error trying to obtain the data: " + t.getMessage());
            }
        };
        sailingService.getLeaderboardGroups(getGroupsCallback);
        sailingService.listEvents(true, getEventsCallback);
    }
    
    private void groupSelectionChanged() {
        //TODO
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
        //Filter list by criteria TODO
//        eventsTableProvider.getList().clear();
//        for (EventDTO event : availableEvents) {
//            if (checkSearchCriteria(event, location, name, onlyLive, from, until)) {
//                eventsTableProvider.getList().add(event);
//            }
//        }
//        //Now sort again according to selected criterion
//        ColumnSortEvent.fire(eventsTable, eventsTable.getColumnSortList());
    }
    
    private boolean checkSearchCriteria(LeaderboardGroupDTO forGroup, String location, String name, boolean onlyLive, Date from, Date until) {
        boolean result = true;
        //TODO
        if (result && !location.equals("")) {
//            result = !textContainingStringsToCheck(Arrays.asList(location.split("\\s")), forEvent.locations);
        }
        if (result && !name.equals("")) {
//            result = !textContainingStringsToCheck(Arrays.asList(name.split("\\s")), forEvent.name);
        }
        //If only live events are allowed the check of the dates isn't needed
        if (result && onlyLive) {
//            result = forEvent.currentlyTracked();
        } else if (result) {
//            Date startDate = forEvent.getStartDate();
//            if (from != null && until != null) {
//                result = from.before(startDate) && until.after(startDate); 
//            } else if (from != null) {
//                result = from.before(startDate);
//            } else if (until != null) {
//                result = until.after(startDate); 
//            }
        }
        
        return result;
    }
    
    private RaceIdentifier getFirstTrackedRaceInGroup(LeaderboardGroupDTO group) {
        RaceIdentifier firstTrackedRace = null;
        groupLoop:
        for (LeaderboardDTO leaderboard : group.leaderboards) {
            for (RaceInLeaderboardDTO race : leaderboard.getRaceList()) {
                if (availableRaceStartDates.containsKey(race.getRaceIdentifier())) {
                    firstTrackedRace = race.getRaceIdentifier();
                    break groupLoop;
                }
            }
        }
        return firstTrackedRace;
    }
    
    private RaceIdentifier getLastTrackedRaceInGroup(LeaderboardGroupDTO group) {
        RaceIdentifier lastTrackedRace = null;
        for (LeaderboardDTO leaderboard : group.leaderboards) {
            for (RaceInLeaderboardDTO race : leaderboard.getRaceList()) {
                if (availableRaceStartDates.containsKey(race.getRaceIdentifier())) {
                    lastTrackedRace = race.getRaceIdentifier();
                }
            }
        }
        return lastTrackedRace;
    }
    
    @Override
    public void fillEvents(List<EventDTO> result) {
        // TODO Auto-generated method stub
        
    }

}
