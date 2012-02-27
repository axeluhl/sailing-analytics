package com.sap.sailing.gwt.ui.spectator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorCell;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLFactory;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.PlacemarkOrderDTO;
import com.sap.sailing.gwt.ui.shared.components.CollapsablePanel;

/**
 * 
 * @author Lennart Hensler (D054527)
 * 
 */
public class LeaderboardGroupOverviewPanel extends FormPanel {

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml cell(String url, String displayName);
    }

    public static final String STYLE_NAME_PREFIX = "groupOverviewPanel-";
    private static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    private StringMessages stringMessages;

    private TextBox locationTextBox;
    private TextBox nameTextBox;
    private TextBox fromTextBox;
    private TextBox untilTextBox;
    private CheckBox onlyLiveCheckBox;
    
    private CellTable<LeaderboardGroupDTO> groupsTable;
    private ListDataProvider<LeaderboardGroupDTO> groupsDataProvider;
    private SingleSelectionModel<LeaderboardGroupDTO> groupsSelectionModel;
    
    private List<LeaderboardGroupDTO> availableGroups;

    public LeaderboardGroupOverviewPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter, StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        availableGroups = new ArrayList<LeaderboardGroupDTO>();

        FlowPanel mainPanel = new FlowPanel();
        this.setWidget(mainPanel);
        mainPanel.setSize("100%", "100%");
        
        // Build search GUI
        FlowPanel searchPanel = new FlowPanel();
        searchPanel.setWidth("100%");
        
        CollapsablePanel collapsableSearchPanel = new CollapsablePanel(this.stringMessages.searchEvents(), false);
        collapsableSearchPanel.setContent(searchPanel);
        collapsableSearchPanel.setCollapsingEnabled(false);
        collapsableSearchPanel.setOpen(true);
        collapsableSearchPanel.setWidth("100%");
        mainPanel.add(collapsableSearchPanel);
        
        Label locationLabel = new Label(this.stringMessages.location() + ":");
        locationLabel.setStyleName(STYLE_NAME_PREFIX + "searchLabel");
        locationLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        searchPanel.add(locationLabel);
        locationTextBox = new TextBox();
        locationTextBox.setStyleName(STYLE_NAME_PREFIX + "searchInputField");
        locationTextBox.getElement().getStyle().setFloat(Style.Float.LEFT);
        locationTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchPanel.add(locationTextBox);
        
        Label nameLabel = new Label(this.stringMessages.name() + ":");
        nameLabel.setStyleName(STYLE_NAME_PREFIX + "searchLabel");
        nameLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        searchPanel.add(nameLabel);
        nameTextBox = new TextBox();
        nameTextBox.setStyleName(STYLE_NAME_PREFIX + "searchInputField");
        nameTextBox.getElement().getStyle().setFloat(Style.Float.LEFT);
        nameTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchPanel.add(nameTextBox);
        
        onlyLiveCheckBox = new CheckBox(this.stringMessages.onlyLiveEvents());
        onlyLiveCheckBox.setStyleName(STYLE_NAME_PREFIX + "searchCheckBox");
        onlyLiveCheckBox.getElement().getStyle().setFloat(Style.Float.LEFT);
        onlyLiveCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                onCheckBoxLiveChange();
            }
        });
        searchPanel.add(onlyLiveCheckBox);
        
        Label fromDateLabel = new Label(this.stringMessages.from() + ":");
        fromDateLabel.setStyleName(STYLE_NAME_PREFIX + "searchLabel");
        fromDateLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        searchPanel.add(fromDateLabel);
        fromTextBox = new TextBox();
        fromTextBox.setStyleName(STYLE_NAME_PREFIX + "searchInputField");
        fromTextBox.getElement().getStyle().setFloat(Style.Float.LEFT);
        fromTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchPanel.add(fromTextBox);
        
        Label toDateLabel = new Label(this.stringMessages.until() + ":");
        toDateLabel.setStyleName(STYLE_NAME_PREFIX + "searchLabel");
        toDateLabel.getElement().getStyle().setFloat(Style.Float.LEFT);
        searchPanel.add(toDateLabel);
        untilTextBox = new TextBox();
        untilTextBox.setStyleName(STYLE_NAME_PREFIX + "searchInputField");
        untilTextBox.getElement().setPropertyString("clear", "right");
        untilTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                onSearchCriteriaChange();
            }
        });
        searchPanel.add(untilTextBox);
        
        //Build group GUI
        FlowPanel groupPanel = new FlowPanel();
        groupPanel.setStyleName(STYLE_NAME_PREFIX + "groupPanel");
        
        CollapsablePanel collapsableGroupPanel = new CollapsablePanel(this.stringMessages.leaderboardGroups(), false);
        collapsableGroupPanel.setContent(groupPanel);
        collapsableGroupPanel.setOpen(true);
        collapsableGroupPanel.setWidth("100%");
        mainPanel.add(collapsableGroupPanel);
        
        TextColumn<LeaderboardGroupDTO> groupsLocationColumn = new TextColumn<LeaderboardGroupDTO>() {
            @Override
            public String getValue(LeaderboardGroupDTO group) {
                List<PlacemarkOrderDTO> groupPlaces = group.getGroupPlaces();
                return groupPlaces.isEmpty() ? LeaderboardGroupOverviewPanel.this.stringMessages.notAvailable()
                        : PlacemarkOrderDTO.placemarksOfAllOrderAsSeperatedString(groupPlaces, true);
            }
        };
        groupsLocationColumn.setSortable(true);
        
        AnchorCell groupsNameAnchorCell = new AnchorCell();
        Column<LeaderboardGroupDTO, SafeHtml> groupsNameColumn = new Column<LeaderboardGroupDTO, SafeHtml>(groupsNameAnchorCell) {
            @Override
            public SafeHtml getValue(LeaderboardGroupDTO group) {
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = URLFactory.INSTANCE.encode("/gwt/Spectator.html?leaderboardGroupName=" + group.name + "&root=overview"
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                return ANCHORTEMPLATE.cell(link, group.name);
            }
        };
        groupsNameColumn.setSortable(true);
        
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
                Date startDate = group.getGroupStartDate();
                return startDate == null ? LeaderboardGroupOverviewPanel.this.stringMessages.untracked()
                        : DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(startDate);
            }
        };
        groupsStartDateColumn.setSortable(true);

        groupsTable = new CellTable<LeaderboardGroupDTO>();
        groupsTable.setWidth("100%");
        groupsSelectionModel = new SingleSelectionModel<LeaderboardGroupDTO>();
        groupsTable.setSelectionModel(groupsSelectionModel);
        
        groupsTable.addColumn(groupsLocationColumn, this.stringMessages.location());
        groupsTable.addColumn(groupsNameColumn, this.stringMessages.name());
        groupsTable.addColumn(groupsLeaderboardsColumn, this.stringMessages.leaderboards());
        groupsTable.addColumn(groupsStartDateColumn, this.stringMessages.startDate());
        groupPanel.add(groupsTable);
        
        groupsDataProvider = new ListDataProvider<LeaderboardGroupDTO>();
        groupsDataProvider.addDataDisplay(groupsTable);

        ListHandler<LeaderboardGroupDTO> groupsListHandler = new ListHandler<LeaderboardGroupDTO>(groupsDataProvider.getList());
        groupsListHandler.setComparator(groupsLocationColumn, new Comparator<LeaderboardGroupDTO>() {
            @Override
            public int compare(LeaderboardGroupDTO g1, LeaderboardGroupDTO g2) {
                String places1 = PlacemarkOrderDTO.placemarksOfAllOrderAsSeperatedString(g1.getGroupPlaces(), true);
                String places2 = PlacemarkOrderDTO.placemarksOfAllOrderAsSeperatedString(g2.getGroupPlaces(), true);
                return places1.compareTo(places2);
            }
        });
        groupsListHandler.setComparator(groupsNameColumn, new Comparator<LeaderboardGroupDTO>() {
            @Override
            public int compare(LeaderboardGroupDTO g1, LeaderboardGroupDTO g2) {
                return g1.name.compareTo(g2.name);
            }
        });
        groupsListHandler.setComparator(groupsStartDateColumn, new Comparator<LeaderboardGroupDTO>() {
            @Override
            public int compare(LeaderboardGroupDTO g1, LeaderboardGroupDTO g2) {
                return g1.getGroupStartDate().compareTo(g2.getGroupStartDate());
            }
        });
        
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
        sailingService.getLeaderboardGroups(new AsyncCallback<List<LeaderboardGroupDTO>>() {
            @Override
            public void onSuccess(List<LeaderboardGroupDTO> result) {
                availableGroups = result == null ? new ArrayList<LeaderboardGroupDTO>() : result;
                groupsDataProvider.getList().clear();
                groupsDataProvider.getList().addAll(availableGroups);
            }
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to obtain the data: " + t.getMessage());
            }
        });
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

}
