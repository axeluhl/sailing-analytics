package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
    private HorizontalPanel splitPanel;
    
    private TextBox groupsFilterTextBox;
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
    
    private ArrayList<LeaderboardGroupDTO> availableLeaderboardGroups;
    private ArrayList<LeaderboardDTO> availableLeaderboards;

    public LeaderboardGroupConfigPanel(SailingServiceAsync sailingService, EventRefresher eventRefresher,
            ErrorReporter errorReporter, StringMessages stringMessages) {
        super(sailingService, eventRefresher, errorReporter, stringMessages);
        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        availableLeaderboardGroups = new ArrayList<LeaderboardGroupDTO>();
        availableLeaderboards = new ArrayList<LeaderboardDTO>();
        
        //Build GUI
        mainPanel = new VerticalPanel();
        mainPanel.setSpacing(5);
        mainPanel.setWidth("95%");
        add(mainPanel);
        
        mainPanel.add(createLeaderboardGroupsGUI(tableRes));
        
        splitPanel = new HorizontalPanel();
        splitPanel.setSpacing(5);
        splitPanel.setWidth("100%");
        splitPanel.setVisible(false);
        mainPanel.add(splitPanel);

        splitPanel.setVerticalAlignment(HorizontalPanel.ALIGN_TOP);
        splitPanel.add(createLeaderboardGroupDetailsGUI(tableRes));
        splitPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
        splitPanel.add(createSwitchLeaderboardsGUI());
        splitPanel.setVerticalAlignment(HorizontalPanel.ALIGN_TOP);
        splitPanel.add(createLeaderboardsGUI(tableRes));
        
        //Load Data
        loadGroups();
        loadLeaderboards();
    }
    
    private void loadGroups() {
        sailingService.getLeaderboardGroups(new AsyncCallback<List<LeaderboardGroupDTO>>() {
            @Override
            public void onSuccess(List<LeaderboardGroupDTO> groups) {
                availableLeaderboardGroups.clear();
                if (groups != null) {
                    availableLeaderboardGroups.addAll(groups);
                }
                leaderboardGroupsProvider.getList().clear();
                leaderboardGroupsProvider.getList().addAll(availableLeaderboardGroups);
            }
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to obtain list of leaderboard groups: " + t.getMessage());
            }
        });
    }
    
    private void loadLeaderboards() {
        sailingService.getLeaderboards(new AsyncCallback<List<LeaderboardDTO>>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to obtain list of leaderboards: " + t.getMessage());
            }
            @Override
            public void onSuccess(List<LeaderboardDTO> leaderboards) {
                availableLeaderboards.clear();
                if (leaderboards != null) {
                    availableLeaderboards.addAll(leaderboards);
                }
                leaderboardGroupsProvider.getList().clear();
                leaderboardGroupsProvider.getList().addAll(availableLeaderboardGroups);
            }
        });
    }

    private Widget createSwitchLeaderboardsGUI() {
        VerticalPanel switchLeaderboardsPanel = new VerticalPanel();
        switchLeaderboardsPanel.setSpacing(5);
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

    private Widget createLeaderboardsGUI(Resources tableRes) {
        CaptionPanel leaderboardsCaptionPanel = new CaptionPanel(stringConstants.leaderboards());
        leaderboardsCaptionPanel.setWidth("95%");
        
        VerticalPanel leaderboardsPanel = new VerticalPanel();
        leaderboardsCaptionPanel.add(leaderboardsPanel);
        
        //Create leaderboards functional elements
        HorizontalPanel leaderboardsFunctionPanel = new HorizontalPanel();
        leaderboardsFunctionPanel.setSpacing(5);
        leaderboardsPanel.add(leaderboardsFunctionPanel);
        
        Label filterLeaderboardsLabel = new Label(stringConstants.filterLeaderboardsByName() + ":");
        leaderboardsFunctionPanel.add(filterLeaderboardsLabel);
        
        filterLeaderboardsTextBox = new TextBox();
        groupsFilterTextBox.addKeyUpHandler(new KeyUpHandler() {
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
        leaderboardsNameColumn.setSortable(true);
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

        leaderboardsTable = new CellTable<LeaderboardDTO>(200, tableRes);
        leaderboardsTable.setWidth("100%");
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

    private Widget createLeaderboardGroupDetailsGUI(Resources tableRes) {
        CaptionPanel groupDetailsCaptionPanel = new CaptionPanel();
        groupDetailsCaptionPanel.setWidth("95%");
        
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

        groupDetailsTable = new CellTable<LeaderboardDTO>(200, tableRes);
        groupDetailsTable.setWidth("100%");
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
        groupDetailsFunctionPanel.setSpacing(5);
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

    private Widget createLeaderboardGroupsGUI(Resources tableRes) {
        CaptionPanel leaderboardGroupsCaptionPanel = new CaptionPanel(stringConstants.leaderboardGroups());
        
        VerticalPanel leaderboardsGroupPanel = new VerticalPanel();
        leaderboardGroupsCaptionPanel.add(leaderboardsGroupPanel);
        
        //Create functional elements for the leaderboard groups
        HorizontalPanel leaderboardGroupsFunctionPanel = new HorizontalPanel();
        leaderboardGroupsFunctionPanel.setSpacing(5);
        leaderboardsGroupPanel.add(leaderboardGroupsFunctionPanel);
        
        Label filterLeaderboardGroupsLbl = new Label(stringConstants.filterLeaderboardGroupsByName() + ":");
        leaderboardGroupsFunctionPanel.add(filterLeaderboardGroupsLbl);
        
        groupsFilterTextBox = new TextBox();
        groupsFilterTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                groupsFilterChanged();
            }
        });
        leaderboardGroupsFunctionPanel.add(groupsFilterTextBox);
        
        Button createGroupButton = new Button(stringConstants.createNewLeaderboardGroup());
        createGroupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addNewGroup();
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
                //TODO Display line breaks in description
                return group.description;
            }
        };

        ImagesBarColumn<LeaderboardGroupDTO, LeaderboardGroupConfigImagesBarCell> groupActionsColumn = new ImagesBarColumn<LeaderboardGroupDTO, LeaderboardGroupConfigImagesBarCell>(
                new LeaderboardGroupConfigImagesBarCell(stringConstants));
        groupActionsColumn.setFieldUpdater(new FieldUpdater<LeaderboardGroupDTO, String>() {
            @Override
            public void update(int index, LeaderboardGroupDTO group, String command) {
                if (command.equals("ACTION_EDIT")) {
                    final String oldGroupName = group.name;
                    ArrayList<LeaderboardGroupDTO> otherExistingGroups = new ArrayList<LeaderboardGroupDTO>(availableLeaderboardGroups);
                    otherExistingGroups.remove(group);
                    LeaderboardGroupEditDialog dialog = new LeaderboardGroupEditDialog(group, otherExistingGroups, stringConstants, new AsyncCallback<LeaderboardGroupDTO>() {
                        @Override
                        public void onFailure(Throwable t) {}
                        @Override
                        public void onSuccess(LeaderboardGroupDTO group) {
                            updateGroup(oldGroupName, group);
                        }
                    });
                    dialog.show();
                } else if (command.equals("ACTION_REMOVE")) {
                    //TODO
                    if (Window.confirm("Do you really want to remove the leaderboard: '" + group.name + "' ?")) {
                        removeLeaderboardGroup(group);
                    }
                }
            }
        });

        leaderboardGroupsTable = new CellTable<LeaderboardGroupDTO>(200, tableRes);
        leaderboardGroupsTable.setWidth("100%");
        leaderboardGroupsTable.addColumn(groupNameColumn, stringConstants.name());
        leaderboardGroupsTable.addColumn(groupDescriptionColumn, stringConstants.description());
        leaderboardGroupsTable.addColumn(groupActionsColumn, stringConstants.actions());
        leaderboardGroupsTable.addColumnSortHandler(leaderboardGroupsListHandler);
        
        leaderboardGroupsSelectionModel = new SingleSelectionModel<LeaderboardGroupDTO>();
        leaderboardGroupsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                groupSelectionChanged();
            }
        });
        leaderboardGroupsTable.setSelectionModel(leaderboardGroupsSelectionModel);
        
        leaderboardGroupsProvider.addDataDisplay(leaderboardGroupsTable);
        leaderboardsGroupPanel.add(leaderboardGroupsTable);
        
        return leaderboardGroupsCaptionPanel;
    }

    private void addNewGroup() {
        LeaderboardGroupCreateDialog dialog = new LeaderboardGroupCreateDialog(
                Collections.unmodifiableCollection(availableLeaderboardGroups), stringConstants,
                new AsyncCallback<LeaderboardGroupDTO>() {
            @Override
            public void onFailure(Throwable t) {}
            @Override
            public void onSuccess(LeaderboardGroupDTO newGroup) {
                createNewGroup(newGroup);
            }
        });
        dialog.show();
    }
    
    private void createNewGroup(final LeaderboardGroupDTO newGroup) {
        sailingService.createLeaderboardGroup(newGroup.name, newGroup.description, new AsyncCallback<LeaderboardGroupDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new leaderboard group" + newGroup.name
                        + ": " + t.getMessage());
            }
            @Override
            public void onSuccess(LeaderboardGroupDTO newGroup) {
                availableLeaderboardGroups.add(newGroup);
                leaderboardGroupsProvider.getList().add(newGroup);
                leaderboardGroupsSelectionModel.setSelected(newGroup, true);
                groupSelectionChanged();
            }
        });
    }
    
    private void updateGroup(final String oldGroupName, final LeaderboardGroupDTO groupToUpdate) {
        sailingService.updateLeaderboardGroup(oldGroupName, groupToUpdate.name, groupToUpdate.description, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to update leaderboard group " + oldGroupName + ": "
                        + t.getMessage());
            }
            @Override
            public void onSuccess(Void v) {
                //Update the availableLeaderboardGroups and the list of displayed groups
                for (int i = 0; i < availableLeaderboardGroups.size(); i++) {
                    LeaderboardGroupDTO group = availableLeaderboardGroups.get(i);
                    if (oldGroupName.equals(group.name)) {
                        availableLeaderboardGroups.set(i, groupToUpdate);
                        int displayedIndex = leaderboardGroupsProvider.getList().indexOf(group);
                        if (displayedIndex != -1) {
                            leaderboardGroupsProvider.getList().set(displayedIndex, groupToUpdate);
                        }
                    }
                }
                leaderboardGroupsProvider.refresh();
            }
        });
    }

    private void removeLeaderboardGroup(final LeaderboardGroupDTO group) {
        sailingService.removeLeaderboardGroup(group.name, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to remove leaderboard group " + group.name + ": "
                        + t.getMessage());
            }
            @Override
            public void onSuccess(Void v) {
                availableLeaderboardGroups.remove(group);
                leaderboardGroupsProvider.getList().remove(group);
                
                //Check if the removed group was the selected one
                LeaderboardGroupDTO selectedGroup = leaderboardGroupsSelectionModel.getSelectedObject();
                if (selectedGroup != null && selectedGroup.name.equals(group.name)) {
                    leaderboardGroupsSelectionModel.setSelected(null, true);
                }
            }
        });
    }
    
    private void groupsFilterChanged() {
        List<String> filter = Arrays.asList(groupsFilterTextBox.getText().split("\\s"));
        leaderboardGroupsProvider.getList().clear();
        for (LeaderboardGroupDTO group : availableLeaderboardGroups) {
            if (!textContainingStringsToCheck(filter, group.name)) {
                leaderboardGroupsProvider.getList().add(group);
            }
        }
        //Now sort again according to selected criterion
        ColumnSortEvent.fire(leaderboardGroupsTable, leaderboardGroupsTable.getColumnSortList());
    }
    
    private void groupSelectionChanged() {
        //TODO what happens when the group selection changes
    }

    @Override
    public void fillEvents(List<EventDTO> result) {
    }
    
}
