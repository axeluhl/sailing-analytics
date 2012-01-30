package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorCell;
import com.sap.sailing.gwt.ui.client.AbstractEventPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDTO;

public class LeaderboardGroupConfigPanel extends AbstractEventPanel {
    
    private VerticalPanel mainPanel;
    
    private TextBox filterGroupsTextBox;
    private CellTable<LeaderboardGroupDTO> leaderboardGroupsTable;
    private SingleSelectionModel<LeaderboardGroupDTO> leaderboardGroupsSelectionModel;
    private ListDataProvider<LeaderboardGroupDTO> leaderboardGroupsProvider;
    
    private CellTable<LeaderboardDTO> groupDetailsTable;
    private MultiSelectionModel<LeaderboardDTO> groupDetailsSelectionModel;
    private ListDataProvider<LeaderboardDTO> groupDetailsProvider;
    
    private TextBox filterLeaderboardsTextBox;
    private CellTable<LeaderboardDTO> leaderboardsTable;
    private MultiSelectionModel<LeaderboardDTO> leaderboardsSelectionModel;
    private ListDataProvider<LeaderboardDTO> leaderboardsProvider;
    
    private HorizontalPanel splitPanel;

    public LeaderboardGroupConfigPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, StringMessages stringMessages) {
        super(sailingService, eventRefresher, errorReporter, stringMessages);
        
        mainPanel = new VerticalPanel();
        mainPanel.setWidth("95%");
        add(mainPanel);
        
        mainPanel.add(createLeaderboardGroupsGUI());
        
        splitPanel = new HorizontalPanel();
        splitPanel.setVisible(false);
        mainPanel.add(splitPanel);
        
        splitPanel.add(createLeaderboardGroupDetailsGUI());
        splitPanel.add(createSwitchLeaderboardsGUI());
        splitPanel.add(createLeaderboardsGUI());
    }

    private Widget createSwitchLeaderboardsGUI() {
        VerticalPanel switchLeaderboardsPanel = new VerticalPanel();
        switchLeaderboardsPanel.setWidth("5%");
        
        Button moveToLeaderboards = new Button("->");
        moveToLeaderboards.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO Auto-generated method stub
            }
        });
        moveToLeaderboards.setEnabled(false);
        switchLeaderboardsPanel.add(moveToLeaderboards);
        
        Button moveToGroup = new Button("<-");
        moveToGroup.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                // TODO Auto-generated method stub
            }
        });
        moveToGroup.setEnabled(false);
        switchLeaderboardsPanel.add(moveToGroup);
        
        return switchLeaderboardsPanel;
    }

    private Widget createLeaderboardsGUI() {
        CaptionPanel leaderboardsCaptionPanel = new CaptionPanel(stringConstants.leaderboards());
        leaderboardsCaptionPanel.setWidth("47%");
        
        VerticalPanel leaderboardsPanel = new VerticalPanel();
        leaderboardsCaptionPanel.add(leaderboardsPanel);
        
        //Create leaderboards functional elements
        HorizontalPanel leaderboardsFunctionPanel = new HorizontalPanel();
        leaderboardsPanel.add(leaderboardsFunctionPanel);
        
        Label filterLeaderboardsLabel = new Label(stringConstants.filterLeaderboardsByName() + ":");
        leaderboardsFunctionPanel.add(filterLeaderboardsLabel);
        
        filterLeaderboardsTextBox = new TextBox();
        filterGroupsTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent arg0) {
                // TODO Auto-generated method stub
            }
        });
        leaderboardsFunctionPanel.add(filterLeaderboardsTextBox);
        
        //Create leaderboards table
        leaderboardsProvider = new ListDataProvider<LeaderboardDTO>();
        ListHandler<LeaderboardDTO> leaderboardsListHandler = new ListHandler<LeaderboardDTO>(leaderboardsProvider.getList());
        
        TextColumn<LeaderboardDTO> leaderboardsNameColumn = new TextColumn<LeaderboardDTO>() {
            @Override
            public String getValue(LeaderboardDTO leaderboard) {
                return leaderboard.name;
            }
        };
        leaderboardsListHandler.setComparator(leaderboardsNameColumn, new Comparator<LeaderboardDTO>() {
            @Override
            public int compare(LeaderboardDTO l1, LeaderboardDTO l2) {
                return l1.name.compareTo(l2.name);
            }
        });
        
        TextColumn<LeaderboardDTO> leaderboardsRacesColumn = new TextColumn<LeaderboardDTO>() {
            @Override
            public String getValue(LeaderboardDTO leaderboard) {
                String result = "";
                boolean first = true;
                for (RaceInLeaderboardDTO race : leaderboard.getRaceList()) {
                    RaceIdentifier raceId = race.getRaceIdentifier();
                    if (first) {
                        result += raceId != null ? raceId.getRaceName() : race.getRaceColumnName();
                        first = false;
                    } else {
                        result += "; ";
                        result += raceId != null ? raceId.getRaceName() : race.getRaceColumnName();
                    }
                }
                return result;
            }
        };
        
        leaderboardsTable = new CellTable<LeaderboardDTO>();
        leaderboardsTable.addColumnSortHandler(leaderboardsListHandler);
        leaderboardsTable.addColumn(leaderboardsNameColumn, stringConstants.name());
        leaderboardsTable.addColumn(leaderboardsRacesColumn, stringConstants.races());

        leaderboardsSelectionModel = new MultiSelectionModel<LeaderboardDTO>();
        leaderboardsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent arg0) {
                // TODO Auto-generated method stub
            }
        });
        leaderboardsTable.setSelectionModel(leaderboardsSelectionModel);

        leaderboardsProvider.addDataDisplay(leaderboardsTable);
        leaderboardsPanel.add(leaderboardsTable);
        
        return leaderboardsCaptionPanel;
    }

    private Widget createLeaderboardGroupDetailsGUI() {
        CaptionPanel groupDetailsCaptionPanel = new CaptionPanel();
        groupDetailsCaptionPanel.setWidth("48%");
        
        VerticalPanel groupDetailsPanel = new VerticalPanel();
        groupDetailsCaptionPanel.add(groupDetailsPanel);
        
        //Create leaderboard table
        TextColumn<LeaderboardDTO> groupDetailsNameColumn = new TextColumn<LeaderboardDTO>() {
            @Override
            public String getValue(LeaderboardDTO leaderboard) {
                return leaderboard.name;
            }
        };
        
        TextColumn<LeaderboardDTO> groupDetailsRacesColumn = new TextColumn<LeaderboardDTO>() {
            @Override
            public String getValue(LeaderboardDTO leaderboard) {
                String result = "";
                boolean first = true;
                for (RaceInLeaderboardDTO race : leaderboard.getRaceList()) {
                    RaceIdentifier raceId = race.getRaceIdentifier();
                    if (first) {
                        result += raceId != null ? raceId.getRaceName() : race.getRaceColumnName();
                        first = false;
                    } else {
                        result += "; ";
                        result += raceId != null ? raceId.getRaceName() : race.getRaceColumnName();
                    }
                }
                return result;
            }
        };
        
        groupDetailsTable = new CellTable<LeaderboardDTO>();
        groupDetailsTable.addColumn(groupDetailsNameColumn, stringConstants.name());
        groupDetailsTable.addColumn(groupDetailsRacesColumn, stringConstants.races());
        
        groupDetailsSelectionModel = new MultiSelectionModel<LeaderboardDTO>();
        groupDetailsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent arg0) {
                // TODO Auto-generated method stub
            }
        });
        groupDetailsTable.setSelectionModel(groupDetailsSelectionModel);
        
        groupDetailsProvider = new ListDataProvider<LeaderboardDTO>();
        groupDetailsProvider.addDataDisplay(groupDetailsTable);
        groupDetailsPanel.add(groupDetailsTable);
        
        //Create details functionality
        HorizontalPanel groupDetailsFunctionPanel = new HorizontalPanel();
        groupDetailsPanel.add(groupDetailsFunctionPanel);
        
        Button leaderboardUp = new Button(stringConstants.columnMoveUp());
        leaderboardUp.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO Auto-generated method stub
            }
        });
        leaderboardUp.setEnabled(false);
        groupDetailsFunctionPanel.add(leaderboardUp);
        
        Button leaderboardDown = new Button(stringConstants.columnMoveDown());
        leaderboardDown.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // TODO Auto-generated method stub
            }
        });
        leaderboardDown.setEnabled(false);
        groupDetailsFunctionPanel.add(leaderboardDown);
        
        return groupDetailsCaptionPanel;
    }

    private Widget createLeaderboardGroupsGUI() {
        CaptionPanel leaderboardGroupsCaptionPanel = new CaptionPanel(stringConstants.leaderboardGroups());
        
        VerticalPanel leaderboardsGroupPanel = new VerticalPanel();
        leaderboardGroupsCaptionPanel.add(leaderboardsGroupPanel);
        
        //Create functional elements for the leaderboard groups
        HorizontalPanel leaderboardGroupsFunctionPanel = new HorizontalPanel();
        leaderboardsGroupPanel.add(leaderboardGroupsFunctionPanel);
        
        Label filterLeaderboardGroupsLbl = new Label(stringConstants.filterLeaderboardGroupsByName() + ":");
        leaderboardGroupsFunctionPanel.add(filterLeaderboardGroupsLbl);
        
        filterGroupsTextBox = new TextBox();
        filterGroupsTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent arg0) {
                // TODO Auto-generated method stub
            }
        });
        leaderboardGroupsFunctionPanel.add(filterGroupsTextBox);
        
        Button createGroupButton = new Button(stringConstants.createNewLeaderboardGroup());
        createGroupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent arg0) {
                // TODO Auto-generated method stub
            }
        });
        leaderboardGroupsFunctionPanel.add(createGroupButton);
        
        //Create table for leaderboard groups
        leaderboardGroupsProvider = new ListDataProvider<LeaderboardGroupDTO>();
        ListHandler<LeaderboardGroupDTO> leaderboardGroupsListHandler = new ListHandler<LeaderboardGroupDTO>(leaderboardGroupsProvider.getList());
        
        AnchorCell anchorCell = new AnchorCell();
        Column<LeaderboardGroupDTO, SafeHtml> groupNameColumn = new Column<LeaderboardGroupDTO, SafeHtml>(anchorCell) {
            @Override
            public SafeHtml getValue(LeaderboardGroupDTO group) {
                // TODO Return a link to the leaderboard group view instead of just the group name
                return SafeHtmlUtils.fromString(group.name);
            }
        };
        groupNameColumn.setSortable(true);
        leaderboardGroupsListHandler.setComparator(groupNameColumn, new Comparator<LeaderboardGroupDTO>() {
            @Override
            public int compare(LeaderboardGroupDTO group1, LeaderboardGroupDTO group2) {
                return group1.name.compareTo(group2.name);
            }
        });

        TextColumn<LeaderboardGroupDTO> groupDescriptionColumn = new TextColumn<LeaderboardGroupDTO>() {
            @Override
            public String getValue(LeaderboardGroupDTO group) {
                return group.description;
            }
        };

        ImagesBarColumn<LeaderboardGroupDTO, LeaderboardConfigImagesBarCell> groupActionsColumn = new ImagesBarColumn<LeaderboardGroupDTO, LeaderboardConfigImagesBarCell>(
                new LeaderboardConfigImagesBarCell(stringConstants));
        groupActionsColumn.setFieldUpdater(new FieldUpdater<LeaderboardGroupDTO, String>() {
            @Override
            public void update(int index, LeaderboardGroupDTO group, String command) {
                if (command.equals(stringConstants.actionEdit())) {
                    //TODO
                } else if (command.equals(stringConstants.actionRemove())) {
                    //TODO
                }
            }
        });

        leaderboardGroupsTable = new CellTable<LeaderboardGroupDTO>();
        leaderboardGroupsTable.addColumn(groupNameColumn, stringConstants.name());
        leaderboardGroupsTable.addColumn(groupDescriptionColumn, stringConstants.description());
        leaderboardGroupsTable.addColumn(groupActionsColumn, stringConstants.actions());
        leaderboardGroupsTable.addColumnSortHandler(leaderboardGroupsListHandler);
        
        leaderboardGroupsSelectionModel = new SingleSelectionModel<LeaderboardGroupDTO>();
        leaderboardGroupsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                // TODO Auto-generated method stub
            }
        });
        leaderboardGroupsTable.setSelectionModel(leaderboardGroupsSelectionModel);
        
        leaderboardGroupsProvider.addDataDisplay(leaderboardGroupsTable);
        leaderboardsGroupPanel.add(leaderboardGroupsTable);
        
        return leaderboardGroupsCaptionPanel;
    }

    @Override
    public void fillEvents(List<EventDTO> result) {
    }
    
}
