package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.AnchorCell;
import com.sap.sailing.gwt.ui.client.AbstractRegattaPanel;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLFactory;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class LeaderboardGroupConfigPanel extends AbstractRegattaPanel {

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml cell(String url, String displayName);
    }

    private static final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    
    private VerticalPanel mainPanel;
    private HorizontalPanel splitPanel;
    private CaptionPanel groupDetailsCaptionPanel;
    
    private TextBox groupsFilterTextBox;
    private CellTable<LeaderboardGroupDTO> groupsTable;
    private SingleSelectionModel<LeaderboardGroupDTO> groupsSelectionModel;
    private ListDataProvider<LeaderboardGroupDTO> groupsProvider;
    
    private CellTable<StrippedLeaderboardDTO> groupDetailsTable;
    private MultiSelectionModel<StrippedLeaderboardDTO> groupDetailsSelectionModel;
    private ListDataProvider<StrippedLeaderboardDTO> groupDetailsProvider;
    private Button editDescriptionButton;
    private Button abortDescriptionButton;
    private Button saveDescriptionButton;
    private TextArea descriptionTextArea;
    private Button leaderboardUpButton;
    private Button leaderboardDownButton;
    
    private TextBox leaderboardsFilterTextBox;
    private CellTable<StrippedLeaderboardDTO> leaderboardsTable;
    private MultiSelectionModel<StrippedLeaderboardDTO> leaderboardsSelectionModel;
    private ListDataProvider<StrippedLeaderboardDTO> leaderboardsProvider;
    
    private Button moveToLeaderboardsButton;
    private Button moveToGroupButton;
    
    private ArrayList<LeaderboardGroupDTO> availableLeaderboardGroups;
    private ArrayList<StrippedLeaderboardDTO> availableLeaderboards;

    public LeaderboardGroupConfigPanel(SailingServiceAsync sailingService, RegattaRefresher regattaRefresher,
            ErrorReporter errorReporter, StringMessages stringMessages) {
        super(sailingService, regattaRefresher, errorReporter, stringMessages);
        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        availableLeaderboardGroups = new ArrayList<LeaderboardGroupDTO>();
        availableLeaderboards = new ArrayList<StrippedLeaderboardDTO>();
        
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
        splitPanel.setVerticalAlignment(HorizontalPanel.ALIGN_BOTTOM);
        splitPanel.add(createLeaderboardsGUI(tableRes));
        
        //Load Data
        loadGroups();
        loadLeaderboards();
    }

    private Widget createSwitchLeaderboardsGUI() {
        VerticalPanel switchLeaderboardsPanel = new VerticalPanel();
        switchLeaderboardsPanel.setSpacing(5);
        switchLeaderboardsPanel.setWidth("5%");
        
        moveToGroupButton = new Button("<-");
        moveToGroupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveToGroup();
            }
        });
        moveToGroupButton.setEnabled(false);
        switchLeaderboardsPanel.add(moveToGroupButton);
        
        moveToLeaderboardsButton = new Button("->");
        moveToLeaderboardsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveToLeaderboards();
            }
        });
        moveToLeaderboardsButton.setEnabled(false);
        switchLeaderboardsPanel.add(moveToLeaderboardsButton);
        
        return switchLeaderboardsPanel;
    }

    private Widget createLeaderboardsGUI(Resources tableRes) {
        CaptionPanel leaderboardsCaptionPanel = new CaptionPanel(stringMessages.leaderboards());
        leaderboardsCaptionPanel.setWidth("95%");
        
        VerticalPanel leaderboardsPanel = new VerticalPanel();
        leaderboardsCaptionPanel.add(leaderboardsPanel);
        
        //Create leaderboards functional elements
        HorizontalPanel leaderboardsFunctionPanel = new HorizontalPanel();
        leaderboardsFunctionPanel.setSpacing(5);
        leaderboardsPanel.add(leaderboardsFunctionPanel);
        
        Label filterLeaderboardsLabel = new Label(stringMessages.filterLeaderboardsByName() + ":");
        leaderboardsFunctionPanel.add(filterLeaderboardsLabel);
        
        leaderboardsFilterTextBox = new TextBox();
        leaderboardsFilterTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                leaderboardsFilterChanged();
            }
        });
        leaderboardsFunctionPanel.add(leaderboardsFilterTextBox);
        
        Button refreshLeaderboardsButton = new Button(stringMessages.refresh());
        refreshLeaderboardsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshLeaderboardsList();
            }
        });
        leaderboardsFunctionPanel.add(refreshLeaderboardsButton);
        
        //Create leaderboards table
        leaderboardsProvider = new ListDataProvider<StrippedLeaderboardDTO>();
        ListHandler<StrippedLeaderboardDTO> leaderboardsListHandler = new ListHandler<StrippedLeaderboardDTO>(leaderboardsProvider.getList());
        
        TextColumn<StrippedLeaderboardDTO> leaderboardsNameColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                return leaderboard.name;
            }
        };
        leaderboardsNameColumn.setSortable(true);
        leaderboardsListHandler.setComparator(leaderboardsNameColumn, new Comparator<StrippedLeaderboardDTO>() {
            @Override
            public int compare(StrippedLeaderboardDTO l1, StrippedLeaderboardDTO l2) {
                return l1.name.compareTo(l2.name);
            }
        });
        
        TextColumn<StrippedLeaderboardDTO> leaderboardsRacesColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                String result = "";
                boolean first = true;
                for (RaceColumnDTO race : leaderboard.getRaceList()) {
                    if (!first) {
                        result += "; ";
                    }
                    result += race.getRaceColumnName();
                    first = false;
                }
                return result;
            }
        };

        leaderboardsTable = new CellTable<StrippedLeaderboardDTO>(10000, tableRes);
        leaderboardsTable.setWidth("100%");
        leaderboardsTable.addColumnSortHandler(leaderboardsListHandler);
        leaderboardsTable.addColumn(leaderboardsNameColumn, stringMessages.leaderboardName());
        leaderboardsTable.addColumn(leaderboardsRacesColumn, stringMessages.races());

        leaderboardsSelectionModel = new MultiSelectionModel<StrippedLeaderboardDTO>();
        leaderboardsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<StrippedLeaderboardDTO> selectedLeaderboards = leaderboardsSelectionModel.getSelectedSet();
                moveToGroupButton.setEnabled(selectedLeaderboards != null && selectedLeaderboards.size() > 0);
            }
        });
        leaderboardsTable.setSelectionModel(leaderboardsSelectionModel);

        leaderboardsProvider.addDataDisplay(leaderboardsTable);
        leaderboardsPanel.add(leaderboardsTable);
        
        return leaderboardsCaptionPanel;
    }

    private Widget createLeaderboardGroupDetailsGUI(Resources tableRes) {
        groupDetailsCaptionPanel = new CaptionPanel();
        groupDetailsCaptionPanel.setWidth("95%");
        
        VerticalPanel groupDetailsPanel = new VerticalPanel();
        groupDetailsPanel.setSpacing(7);
        groupDetailsCaptionPanel.add(groupDetailsPanel);
        
        //Create description area
        CaptionPanel descriptionCaptionPanel = new CaptionPanel(stringMessages.description());
        groupDetailsPanel.add(descriptionCaptionPanel);
        
        VerticalPanel descriptionPanel = new VerticalPanel();
        descriptionCaptionPanel.add(descriptionPanel);
        
        descriptionTextArea = new TextArea();
        descriptionTextArea.setCharacterWidth(60);
        descriptionTextArea.setVisibleLines(8);
        descriptionTextArea.getElement().getStyle().setProperty("resize", "none");
        descriptionTextArea.setReadOnly(true);
        descriptionPanel.add(descriptionTextArea);
        
        HorizontalPanel descriptionFunctionsPanel = new HorizontalPanel();
        descriptionFunctionsPanel.setSpacing(5);
        descriptionPanel.add(descriptionFunctionsPanel);
        
        editDescriptionButton = new Button(stringMessages.edit());
        editDescriptionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setDescriptionEditable(true);
            }
        });
        descriptionFunctionsPanel.add(editDescriptionButton);
        
        abortDescriptionButton = new Button(stringMessages.abort());
        abortDescriptionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject();
                setDescriptionEditable(false);
                descriptionTextArea.setText(selectedGroup.description);
            }
        });
        abortDescriptionButton.setVisible(false);
        abortDescriptionButton.setEnabled(false);
        descriptionFunctionsPanel.add(abortDescriptionButton);
        
        saveDescriptionButton = new Button(stringMessages.save());
        saveDescriptionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                saveDescriptionChanges();
            }
        });
        saveDescriptionButton.setEnabled(false);
        saveDescriptionButton.setVisible(false);
        descriptionFunctionsPanel.add(saveDescriptionButton);
        
        //Create leaderboard table
        TextColumn<StrippedLeaderboardDTO> groupDetailsNameColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                return leaderboard.name;
            }
        };
        
        TextColumn<StrippedLeaderboardDTO> groupDetailsRacesColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                String result = "";
                boolean first = true;
                for (RaceColumnDTO race : leaderboard.getRaceList()) {
                    if (!first) {
                        result += "; ";
                    }
                    result += race.getRaceColumnName();
                    first = false;
                }
                return result;
            }
        };

        groupDetailsTable = new CellTable<StrippedLeaderboardDTO>(10000, tableRes);
        groupDetailsTable.setWidth("100%");
        groupDetailsTable.addColumn(groupDetailsNameColumn, stringMessages.leaderboardName());
        groupDetailsTable.addColumn(groupDetailsRacesColumn, stringMessages.races());
        
        groupDetailsSelectionModel = new MultiSelectionModel<StrippedLeaderboardDTO>();
        groupDetailsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<StrippedLeaderboardDTO> selectedLeaderboardsInGroup = groupDetailsSelectionModel.getSelectedSet();
                moveToLeaderboardsButton.setEnabled(selectedLeaderboardsInGroup != null && !selectedLeaderboardsInGroup.isEmpty());
                leaderboardDownButton.setEnabled(selectedLeaderboardsInGroup != null && selectedLeaderboardsInGroup.size() == 1);
                leaderboardUpButton.setEnabled(selectedLeaderboardsInGroup != null && selectedLeaderboardsInGroup.size() == 1);
            }
        });
        groupDetailsTable.setSelectionModel(groupDetailsSelectionModel);
        
        groupDetailsProvider = new ListDataProvider<StrippedLeaderboardDTO>();
        groupDetailsProvider.addDataDisplay(groupDetailsTable);
        groupDetailsPanel.add(groupDetailsTable);
        
        //Create details functionality
        HorizontalPanel groupDetailsFunctionPanel = new HorizontalPanel();
        groupDetailsFunctionPanel.setSpacing(5);
        groupDetailsPanel.add(groupDetailsFunctionPanel);
        
        leaderboardUpButton = new Button(stringMessages.columnMoveUp());
        leaderboardUpButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveLeaderboardInGroupUp();
            }
        });
        leaderboardUpButton.setEnabled(false);
        groupDetailsFunctionPanel.add(leaderboardUpButton);
        
        leaderboardDownButton = new Button(stringMessages.columnMoveDown());
        leaderboardDownButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveLeaderboardInGroupDown();
            }
        });
        leaderboardDownButton.setEnabled(false);
        groupDetailsFunctionPanel.add(leaderboardDownButton);
        
        return groupDetailsCaptionPanel;
    }

    private Widget createLeaderboardGroupsGUI(Resources tableRes) {
        CaptionPanel leaderboardGroupsCaptionPanel = new CaptionPanel(stringMessages.leaderboardGroups());
        
        VerticalPanel leaderboardsGroupPanel = new VerticalPanel();
        leaderboardGroupsCaptionPanel.add(leaderboardsGroupPanel);
        
        //Create functional elements for the leaderboard groups
        HorizontalPanel leaderboardGroupsFunctionPanel = new HorizontalPanel();
        leaderboardGroupsFunctionPanel.setSpacing(5);
        leaderboardsGroupPanel.add(leaderboardGroupsFunctionPanel);
        
        Label filterLeaderboardGroupsLbl = new Label(stringMessages.filterLeaderboardGroupsByName() + ":");
        leaderboardGroupsFunctionPanel.add(filterLeaderboardGroupsLbl);
        
        groupsFilterTextBox = new TextBox();
        groupsFilterTextBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                groupsFilterChanged();
            }
        });
        leaderboardGroupsFunctionPanel.add(groupsFilterTextBox);
        
        Button createGroupButton = new Button(stringMessages.createNewLeaderboardGroup());
        createGroupButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addNewGroup();
            }
        });
        leaderboardGroupsFunctionPanel.add(createGroupButton);
        
        //Create table for leaderboard groups
        groupsProvider = new ListDataProvider<LeaderboardGroupDTO>();
        ListHandler<LeaderboardGroupDTO> leaderboardGroupsListHandler = new ListHandler<LeaderboardGroupDTO>(groupsProvider.getList());
        
        AnchorCell anchorCell = new AnchorCell();
        Column<LeaderboardGroupDTO, SafeHtml> groupNameColumn = new Column<LeaderboardGroupDTO, SafeHtml>(anchorCell) {
            @Override
            public SafeHtml getValue(LeaderboardGroupDTO group) {
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = URLFactory.INSTANCE.encode("/gwt/Spectator.html?leaderboardGroupName=" + group.name
                        + "&showRaceDetails=true"
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                return ANCHORTEMPLATE.cell(link, group.name);
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
                return group.description.length() <= 100 ? group.description : group.description.substring(0, 98) + "...";
            }
        };
        TextColumn<LeaderboardGroupDTO> hasOverallLeaderboardColumn = new TextColumn<LeaderboardGroupDTO>() {
            @Override
            public String getValue(LeaderboardGroupDTO group) {
            	String result = stringMessages.no();
            	if(group.hasOverallLeaderboard()) {
            	    result =  stringMessages.yes() + " (" + ScoringSchemeTypeFormatter.format(group.getOverallLeaderboardScoringSchemeType(), stringMessages) +")";
            	}
            	return  result;
            }
        };
        
        ImagesBarColumn<LeaderboardGroupDTO, LeaderboardGroupConfigImagesBarCell> groupActionsColumn = new ImagesBarColumn<LeaderboardGroupDTO, LeaderboardGroupConfigImagesBarCell>(
                new LeaderboardGroupConfigImagesBarCell(stringMessages));
        groupActionsColumn.setFieldUpdater(new FieldUpdater<LeaderboardGroupDTO, String>() {
            @Override
            public void update(int index, LeaderboardGroupDTO group, String command) {
                if (command.equals("ACTION_EDIT")) {
                    final String oldGroupName = group.name;
                    ArrayList<LeaderboardGroupDTO> otherExistingGroups = new ArrayList<LeaderboardGroupDTO>(availableLeaderboardGroups);
                    otherExistingGroups.remove(group);
                    LeaderboardGroupEditDialog dialog = new LeaderboardGroupEditDialog(group, otherExistingGroups, stringMessages, new DialogCallback<LeaderboardGroupDTO>() {
                        @Override
                        public void cancel() {}
                        @Override
                        public void ok(LeaderboardGroupDTO group) {
                            updateGroup(oldGroupName, group);
                        }
                    });
                    dialog.show();
                } else if (command.equals("ACTION_REMOVE")) {
                    if (Window.confirm("Do you really want to remove the leaderboard group: '" + group.name + "' ?")) {
                        removeLeaderboardGroup(group);
                    }
                }
            }
        });

        groupsTable = new CellTable<LeaderboardGroupDTO>(10000, tableRes);
        groupsTable.setWidth("100%");
        groupsTable.addColumn(groupNameColumn, stringMessages.name());
        groupsTable.addColumn(groupDescriptionColumn, stringMessages.description());
        groupsTable.addColumn(hasOverallLeaderboardColumn, stringMessages.useOverallLeaderboard());
        groupsTable.addColumn(groupActionsColumn, stringMessages.actions());
        groupsTable.addColumnSortHandler(leaderboardGroupsListHandler);
        
        groupsSelectionModel = new SingleSelectionModel<LeaderboardGroupDTO>();
        groupsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                groupSelectionChanged();
            }
        });
        groupsTable.setSelectionModel(groupsSelectionModel);
        
        groupsProvider.addDataDisplay(groupsTable);
        leaderboardsGroupPanel.add(groupsTable);
        
        return leaderboardGroupsCaptionPanel;
    }
    
    private void loadGroups() {
        sailingService.getLeaderboardGroups(false /*withGeoLocationData*/, new AsyncCallback<List<LeaderboardGroupDTO>>() {
            @Override
            public void onSuccess(List<LeaderboardGroupDTO> groups) {
                availableLeaderboardGroups.clear();
                if (groups != null) {
                    availableLeaderboardGroups.addAll(groups);
                }
                groupsProvider.getList().clear();
                groupsProvider.getList().addAll(availableLeaderboardGroups);
            }
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to obtain list of leaderboard groups: " + t.getMessage());
            }
        });
    }
    
    private void loadLeaderboards() {
        sailingService.getLeaderboards(new AsyncCallback<List<StrippedLeaderboardDTO>>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to obtain list of leaderboards: " + t.getMessage());
            }
            @Override
            public void onSuccess(List<StrippedLeaderboardDTO> leaderboards) {
                availableLeaderboards.clear();
                if (leaderboards != null) {
                    availableLeaderboards.addAll(leaderboards);
                }
                leaderboardsProvider.getList().clear();
                leaderboardsProvider.getList().addAll(availableLeaderboards);
            }
        });
    }
    
    private void refreshLeaderboardsList() {
        final LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject();
        final Set<StrippedLeaderboardDTO> selectedLeaderboards = leaderboardsSelectionModel.getSelectedSet();
        sailingService.getLeaderboards(new AsyncCallback<List<StrippedLeaderboardDTO>>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to obtain list of leaderboards: " + t.getMessage());
            }
            @Override
            public void onSuccess(List<StrippedLeaderboardDTO> leaderboards) {
                availableLeaderboards.clear();
                if (leaderboards != null) {
                    availableLeaderboards.addAll(leaderboards);
                }
                leaderboardsProvider.getList().clear();
                leaderboardsProvider.getList().addAll(availableLeaderboards);
                if (selectedGroup != null) {
                    leaderboardsProvider.getList().removeAll(selectedGroup.leaderboards);
                }
                leaderboardsFilterTextBox.setText("");
                leaderboardsSelectionModel.clear();
                for (StrippedLeaderboardDTO leaderboard : selectedLeaderboards) {
                    leaderboardsSelectionModel.setSelected(leaderboard, true);
                }
            }
        });
    }

    private void addNewGroup() {
        LeaderboardGroupCreateDialog dialog = new LeaderboardGroupCreateDialog(
                Collections.unmodifiableCollection(availableLeaderboardGroups), stringMessages,
                new DialogCallback<LeaderboardGroupDTO>() {
            @Override
            public void cancel() {}
            @Override
            public void ok(LeaderboardGroupDTO newGroup) {
                createNewGroup(newGroup);
            }
        });
        dialog.show();
    }
    
    private void createNewGroup(final LeaderboardGroupDTO newGroup) {
        sailingService.createLeaderboardGroup(newGroup.name, newGroup.description, newGroup.displayGroupsInReverseOrder,
                newGroup.getOverallLeaderboardDiscardThresholds(),
                newGroup.getOverallLeaderboardScoringSchemeType(), new AsyncCallback<LeaderboardGroupDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new leaderboard group" + newGroup.name
                        + ": " + t.getMessage());
            }
            @Override
            public void onSuccess(LeaderboardGroupDTO newGroup) {
                availableLeaderboardGroups.add(newGroup);
                groupsProvider.getList().add(newGroup);
                groupsSelectionModel.setSelected(newGroup, true);
                groupSelectionChanged();
            }
        });
    }
    
    private void updateGroup(final String oldGroupName, final LeaderboardGroupDTO groupToUpdate) {
        List<String> leaderboardNames = new ArrayList<String>();
        for (StrippedLeaderboardDTO leaderboardDTO : groupToUpdate.leaderboards) {
            leaderboardNames.add(leaderboardDTO.name);
        }
        sailingService.updateLeaderboardGroup(oldGroupName, groupToUpdate.name, groupToUpdate.description,
                leaderboardNames, groupToUpdate.getOverallLeaderboardDiscardThresholds(),
                groupToUpdate.getOverallLeaderboardScoringSchemeType(), new AsyncCallback<Void>() {
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
                        int displayedIndex = groupsProvider.getList().indexOf(group);
                        if (displayedIndex != -1) {
                            groupsProvider.getList().set(displayedIndex, groupToUpdate);
                        }
                    }
                }
                groupsProvider.refresh();
            }
        });
    }
    private void updateGroup(final LeaderboardGroupDTO group) {
        updateGroup(group.name, group);
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
                groupsProvider.getList().remove(group);
                
                //Check if the removed group was the selected one
                LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject();
                if (selectedGroup != null && selectedGroup.name.equals(group.name)) {
                    groupsSelectionModel.setSelected(null, true);
                }
            }
        });
    }
    
    private void groupsFilterChanged() {
        List<String> filter = Arrays.asList(groupsFilterTextBox.getText().split("\\s"));
        groupsProvider.getList().clear();
        for (LeaderboardGroupDTO group : availableLeaderboardGroups) {
            if (!textContainingStringsToCheck(filter, group.name)) {
                groupsProvider.getList().add(group);
            }
        }
        //Now sort again according to selected criterion
        ColumnSortEvent.fire(groupsTable, groupsTable.getColumnSortList());
    }
    
    private void groupSelectionChanged() {
        final LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject();
        splitPanel.setVisible(selectedGroup != null);
        if (selectedGroup != null) {
            sailingService.getLeaderboardGroupByName(selectedGroup.name, false /*withGeoLocationData*/, new AsyncCallback<LeaderboardGroupDTO>() {
                @Override
                public void onFailure(Throwable t) {
                    errorReporter.reportError("Error trying to obtain the leaderboard group " + selectedGroup.name + ": " + t.getMessage());
                }
                @Override
                public void onSuccess(LeaderboardGroupDTO result) {
                    //Updating the data lists
                    availableLeaderboardGroups.set(availableLeaderboardGroups.indexOf(selectedGroup), result);
                    groupsSelectionModel.setSelected(result, true);

                    //Display details of the group
                    groupDetailsCaptionPanel.setCaptionText(stringMessages.detailsOfLeaderboardGroup() + " '" + result.name + "'");
                    descriptionTextArea.setText(result.description);
                    setDescriptionEditable(false);
                    
                    groupDetailsSelectionModel.clear();
                    groupDetailsProvider.getList().clear();
                    groupDetailsProvider.getList().addAll(result.leaderboards);
                    
                    //Reload available leaderboards and remove leaderboards of the group from the list
                    leaderboardsSelectionModel.clear();
                    leaderboardsFilterTextBox.setText("");
                    leaderboardsProvider.getList().clear();
                    leaderboardsProvider.getList().addAll(availableLeaderboards);
                    leaderboardsProvider.getList().removeAll(result.leaderboards);
                }
            });
        }
    }
    
    private void leaderboardsFilterChanged() {
        LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject();
        List<String> filter = Arrays.asList(leaderboardsFilterTextBox.getText().split("\\s"));
        leaderboardsProvider.getList().clear();
        for (StrippedLeaderboardDTO leaderboard : availableLeaderboards) {
            if (!textContainingStringsToCheck(filter, leaderboard.name) && !selectedGroup.leaderboards.contains(leaderboard)) {
                leaderboardsProvider.getList().add(leaderboard);
            } else {
                leaderboardsSelectionModel.setSelected(leaderboard, false);
            }
        }
        //Now sort again according to selected criterion
        ColumnSortEvent.fire(leaderboardsTable, leaderboardsTable.getColumnSortList());
    }
    
    private void moveToLeaderboards() {
        LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject();
        Set<StrippedLeaderboardDTO> selectedLeaderboards = groupDetailsSelectionModel.getSelectedSet();
        if (selectedGroup != null && selectedLeaderboards != null && selectedLeaderboards.size() > 0) {
            for (StrippedLeaderboardDTO leaderboard : selectedLeaderboards) {
                selectedGroup.leaderboards.remove(leaderboard);
                groupDetailsProvider.getList().remove(leaderboard);
                groupDetailsSelectionModel.setSelected(leaderboard, false);
                leaderboardsProvider.getList().add(leaderboard);
            }
            updateGroup(selectedGroup);
            //Refilters the leaderboards list (hides the moved leaderboards if they don't fit to the filter) and resorts the list
            leaderboardsFilterChanged();
        }
    }
    
    private void moveToGroup() {
        LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject();
        ArrayList<StrippedLeaderboardDTO> selectedLeaderboards = new ArrayList<StrippedLeaderboardDTO>(leaderboardsSelectionModel.getSelectedSet());
        if (selectedGroup != null && selectedLeaderboards != null && !selectedLeaderboards.isEmpty()) {
            Collections.sort(selectedLeaderboards, new Comparator<StrippedLeaderboardDTO>() {
                @Override
                public int compare(StrippedLeaderboardDTO l1, StrippedLeaderboardDTO l2) {
                    List<StrippedLeaderboardDTO> leaderboards = leaderboardsProvider.getList();
                    return ((Integer) leaderboards.indexOf(l1)).compareTo(leaderboards.indexOf(l2));
                }
            });
            for (StrippedLeaderboardDTO leaderboard : selectedLeaderboards) {
                if (!selectedGroup.leaderboards.contains(leaderboard)) {
                    selectedGroup.leaderboards.add(leaderboard);
                    groupDetailsProvider.getList().add(leaderboard);
                    leaderboardsProvider.getList().remove(leaderboard);
                    leaderboardsSelectionModel.setSelected(leaderboard, false);
                }
            }
            updateGroup(selectedGroup);
        }
    }
    
    private void moveLeaderboardInGroupUp() {
        LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject(); 
        Set<StrippedLeaderboardDTO> selectedLeaderboards = groupDetailsSelectionModel.getSelectedSet();
        if (selectedLeaderboards != null && selectedLeaderboards.size() == 1) {
            StrippedLeaderboardDTO selectedLeaderboard = selectedLeaderboards.iterator().next();
            moveLeaderboardInGroup(selectedGroup, selectedLeaderboard, -1);
        }
    }
    
    private void moveLeaderboardInGroupDown() {
        LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject(); 
        Set<StrippedLeaderboardDTO> selectedLeaderboards = groupDetailsSelectionModel.getSelectedSet();
        if (selectedLeaderboards != null && selectedLeaderboards.size() == 1) {
            StrippedLeaderboardDTO selectedLeaderboard = selectedLeaderboards.iterator().next();
            moveLeaderboardInGroup(selectedGroup, selectedLeaderboard, 1);
        }
    }
    
    private void moveLeaderboardInGroup(LeaderboardGroupDTO group, StrippedLeaderboardDTO leaderboard, int direction) {
        int index = group.leaderboards.indexOf(leaderboard);
        int destIndex = index + direction;
        if (destIndex >= 0 && destIndex < group.leaderboards.size()) {
            StrippedLeaderboardDTO temp = group.leaderboards.get(destIndex);
            group.leaderboards.set(destIndex, leaderboard);
            group.leaderboards.set(index, temp);
            groupDetailsProvider.getList().clear();
            groupDetailsProvider.getList().addAll(group.leaderboards);
            
            updateGroup(group);
        }
    }

    @Override
    public void fillRegattas(List<RegattaDTO> result) {
    }

    private void setDescriptionEditable(boolean isEditable) {
        LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject();
        if (selectedGroup != null) {
            editDescriptionButton.setEnabled(!isEditable);
            editDescriptionButton.setVisible(!isEditable);
            abortDescriptionButton.setEnabled(isEditable);
            abortDescriptionButton.setVisible(isEditable);
            saveDescriptionButton.setEnabled(isEditable);
            saveDescriptionButton.setVisible(isEditable);
            descriptionTextArea.setReadOnly(!isEditable);
        }
    }

    private void saveDescriptionChanges() {
        String newDescription = descriptionTextArea.getText();
        LeaderboardGroupDTO selectedGroup = groupsSelectionModel.getSelectedObject();
        if (newDescription != null && newDescription.length() > 0) {
            selectedGroup.description = newDescription;
            setDescriptionEditable(false);
            updateGroup(selectedGroup);
        } else {
            Window.alert(stringMessages.pleaseEnterNonEmptyDescription() + ".");
            descriptionTextArea.setText(selectedGroup.description);
        }
    }
    
}
