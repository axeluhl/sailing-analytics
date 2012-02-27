package com.sap.sailing.gwt.ui.spectator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.tools.ant.taskdefs.Sleep;

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
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
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
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.components.CollapsablePanel;

/**
 * 
 * @author Lennart Hensler (D054527)
 * 
 */
public class LeaderboardGroupOverviewPanel extends FormPanel {

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml anchor(String url, String displayName);
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
    
    private Label noGroupSelectedLabel;
    
    private FlowPanel groupDetailsPanel;
    private HTML groupDescription;
    private CellTable<LeaderboardDTO> leaderboardsTable;
    private ListDataProvider<LeaderboardDTO> leaderboardsDataProvider;
    private SingleSelectionModel<LeaderboardDTO> leaderboardsSelectionModel;
    
    private CellTable<RaceInLeaderboardDTO> racesTable;
    private ListDataProvider<RaceInLeaderboardDTO> racesDataProvider;
    private SingleSelectionModel<RaceInLeaderboardDTO> racesSelectionModel;
    
    
    
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
                return ANCHORTEMPLATE.anchor(link, group.name);
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
                Date d1 = g1.getGroupStartDate();
                Date d2 = g2.getGroupStartDate();
                if (d1 == null || d2 == null) {
                    return d1 == null && d2 == null ? 0 : d1 == null ? -1 : 1;
                } else {
                    return g1.getGroupStartDate().compareTo(g2.getGroupStartDate());
                }
            }
        });
        groupsTable.addColumnSortHandler(groupsListHandler);
        
        //Build details GUI
        FlowPanel detailsPanel = new FlowPanel();
        detailsPanel.setStyleName(STYLE_NAME_PREFIX + "detailsPanel");
        
        CollapsablePanel collapsableDetailsPanel = new CollapsablePanel(this.stringMessages.details(), false);
        collapsableDetailsPanel.setContent(detailsPanel);
        collapsableDetailsPanel.setWidth("100%");
        mainPanel.add(collapsableDetailsPanel);
        
        noGroupSelectedLabel = new Label(this.stringMessages.noGroupSelected());
        detailsPanel.add(noGroupSelectedLabel);
        
        //Build group details GUI
        groupDetailsPanel = new FlowPanel();
        groupDetailsPanel.setStyleName(STYLE_NAME_PREFIX + "groupDetailsPanel");
        groupDetailsPanel.getElement().getStyle().setFloat(Style.Float.LEFT);
        detailsPanel.add(groupDetailsPanel);
        
        groupDescription = new HTML();
        groupDescription.setStyleName(STYLE_NAME_PREFIX + "groupDescription");
        groupDescription.setVisible(false);
        groupDetailsPanel.add(groupDescription);
        
        TextColumn<LeaderboardDTO> leaderboardsLocationColumn = new TextColumn<LeaderboardDTO>() {
            @Override
            public String getValue(LeaderboardDTO leaderboard) {
                PlacemarkOrderDTO leaderboardPlaces = leaderboard.getPlaces();
                return leaderboardPlaces == null ? LeaderboardGroupOverviewPanel.this.stringMessages
                        .locationNotAvailable() : leaderboardPlaces.placemarksAsString();
            }
        };
        
        AnchorCell leaderboardsNameAnchorCell = new AnchorCell();
        Column<LeaderboardDTO, SafeHtml> leaderboardsNameColumn = new Column<LeaderboardDTO, SafeHtml>(leaderboardsNameAnchorCell) {
            @Override
            public SafeHtml getValue(LeaderboardDTO leaderboard) {
                LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject();
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = URLFactory.INSTANCE.encode("/gwt/Leaderboard.html?name=" + leaderboard.name
                        + "&leaderboardGroupName=" + selectedGroup.name + "&root=overview"
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                return ANCHORTEMPLATE.anchor(link, leaderboard.name);
            }
        };
        
        TextColumn<LeaderboardDTO> leaderboardsRacesColumn = new TextColumn<LeaderboardDTO>() {
            @Override
            public String getValue(LeaderboardDTO leaderboard) {
                // TODO Auto-generated method stub
                return null;
            }
        };
        
        TextColumn<LeaderboardDTO> leaderboardsStartDateColumn = new TextColumn<LeaderboardDTO>() {
            @Override
            public String getValue(LeaderboardDTO leaderboard) {
                Date leaderboardStart = leaderboard.getStartDate();
                return leaderboardStart == null ? LeaderboardGroupOverviewPanel.this.stringMessages.untracked()
                        : DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT).format(leaderboardStart);
            }
        };
        
        leaderboardsTable = new CellTable<LeaderboardDTO>();
        leaderboardsTable.setWidth("100%");
        leaderboardsTable.setVisible(false);
        leaderboardsSelectionModel = new SingleSelectionModel<LeaderboardDTO>();
        leaderboardsTable.setSelectionModel(leaderboardsSelectionModel);
        
        leaderboardsTable.addColumn(leaderboardsLocationColumn, this.stringMessages.location());
        leaderboardsTable.addColumn(leaderboardsNameColumn, this.stringMessages.name());
        leaderboardsTable.addColumn(leaderboardsRacesColumn, this.stringMessages.races());
        leaderboardsTable.addColumn(leaderboardsStartDateColumn, this.stringMessages.startDate());
        groupDetailsPanel.add(leaderboardsTable);
        
        leaderboardsDataProvider = new ListDataProvider<LeaderboardDTO>();
        leaderboardsDataProvider.addDataDisplay(leaderboardsTable);

        //Build leaderboard details GUI TODO
        
        //Loading the data
        loadData();
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

        groupsDataProvider.getList().clear();
        for (LeaderboardGroupDTO group : availableGroups) {
            if (checkSearchCriteria(group, location, name, onlyLive, from, until)) {
                groupsDataProvider.getList().add(group);
            }
        }
        //Now sort again according to selected criterion
        ColumnSortEvent.fire(groupsTable, groupsTable.getColumnSortList());
    }
    
    private boolean checkSearchCriteria(LeaderboardGroupDTO forGroup, String location, String name, boolean onlyLive, Date from, Date until) {
        boolean result = true;
        if (result && location != null && !location.equals("")) {
            result = textContainsStringsToCheck(
                    PlacemarkOrderDTO.placemarksOfAllOrderAsSeperatedString(forGroup.getGroupPlaces(), true),
                    location.split("\\s"));
        }
        if (result && name != null && !name.equals("")) {
            result = textContainsStringsToCheck(forGroup.name, name.split("\\s"));
        }
        if (result) {
            Date startDate = forGroup.getGroupStartDate();
            if (startDate != null) {
                if (from != null && until != null) {
                    result = from.before(startDate) && until.after(startDate);
                } else if (from != null) {
                    result = from.before(startDate);
                } else if (until != null) {
                    result = until.after(startDate);
                }
            }
        }
        
        return result;
    }
    
    private boolean textContainsStringsToCheck(String text, String[] stringsToCheck) {
        boolean contains = false;
        for (String stringToCheck : stringsToCheck) {
            contains = text.contains(stringToCheck);
            if (contains) {
                break;
            }
        }
        return contains;
    }

}
