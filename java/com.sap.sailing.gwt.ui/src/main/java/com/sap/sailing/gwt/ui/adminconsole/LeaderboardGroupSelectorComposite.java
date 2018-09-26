package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.celltable.BaseCelltable;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public class LeaderboardGroupSelectorComposite extends Composite implements HasValueChangeHandlers<Iterable<LeaderboardGroupDTO>> {
    private final StringMessages stringMessages;
    
    private CellTable<LeaderboardGroupDTO> selectedEventLeaderboardGroupsTable;
    private MultiSelectionModel<LeaderboardGroupDTO> selectedEventLeaderboardGroupsSelectionModel;
    private ListDataProvider<LeaderboardGroupDTO> selectedEventLeaderboardGroupsProvider;
    
    private LabeledAbstractFilterablePanel<LeaderboardGroupDTO> availableLeaderboardGroupsFilterablePanel;
    private CellTable<LeaderboardGroupDTO> availableLeaderboardGroupsTable;
    private RefreshableMultiSelectionModel<LeaderboardGroupDTO> availableLeaderboardGroupsSelectionModel;
    private ListDataProvider<LeaderboardGroupDTO> availableLeaderboardGroupsProvider;

    private List<LeaderboardGroupDTO> availableLeaderboardGroups;
    private List<LeaderboardGroupDTO> leaderboardGroups;
    
    private Button addToEventButton;
    private Button removeFromEventButton;
    private final HorizontalPanel splitPanel;

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

    public LeaderboardGroupSelectorComposite(List<LeaderboardGroupDTO> leaderboardGroups, List<LeaderboardGroupDTO> availableLeaderboardGroups, final StringMessages stringMessages) {
        super();
        this.leaderboardGroups = leaderboardGroups;
        this.availableLeaderboardGroups = availableLeaderboardGroups;
        this.stringMessages = stringMessages;
        
        splitPanel = new HorizontalPanel();
        splitPanel.ensureDebugId("EventLeaderboardGroupsAssignmentDetailsPanel");
        splitPanel.setSpacing(5);
        splitPanel.setWidth("100%");

        splitPanel.setVerticalAlignment(HorizontalPanel.ALIGN_TOP);

        final Widget switchingButtonsPanel = createSwitchLeaderboardGroupsGUI();
        splitPanel.add(createLeaderboardGroupOfEventsPanel()); 
        splitPanel.add(switchingButtonsPanel);
        splitPanel.add(createAvailableLeaderboardGroupsPanel());

        initWidget(splitPanel);
    }
    
    private Widget createLeaderboardGroupOfEventsPanel() {
        selectedEventLeaderboardGroupsTable = new BaseCelltable<LeaderboardGroupDTO>(10000000, tableRes);
        CaptionPanel result = new CaptionPanel(stringMessages.leaderboardGroupsOfSelectedEvent());
        result.add(selectedEventLeaderboardGroupsTable);
        selectedEventLeaderboardGroupsSelectionModel = new MultiSelectionModel<>();
        selectedEventLeaderboardGroupsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                removeFromEventButton.setEnabled(!selectedEventLeaderboardGroupsSelectionModel.getSelectedSet().isEmpty());
            }
        });
        selectedEventLeaderboardGroupsTable.setSelectionModel(selectedEventLeaderboardGroupsSelectionModel);
        selectedEventLeaderboardGroupsProvider = new ListDataProvider<>();
        selectedEventLeaderboardGroupsProvider.addDataDisplay(selectedEventLeaderboardGroupsTable);

        TextColumn<LeaderboardGroupDTO> leaderboardGroupNameColumn = new TextColumn<LeaderboardGroupDTO>() {
            @Override
            public String getValue(LeaderboardGroupDTO event) {
                return event.getName();
            }
        };
        final SafeHtmlCell leaderboardsCell = new SafeHtmlCell();
        Column<LeaderboardGroupDTO, SafeHtml> associatedLeaderboardsColumn = new Column<LeaderboardGroupDTO, SafeHtml>(leaderboardsCell) {
            @Override
            public SafeHtml getValue(LeaderboardGroupDTO leaderboardGroup) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                boolean first = true;
                for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                    if (first) {
                        first = false;
                    } else {
                        builder.appendHtmlConstant("<br>");
                    }
                    builder.appendEscaped(leaderboard.getName());
                }
                return builder.toSafeHtml();
            }
        };
        
        leaderboardGroupNameColumn.setSortable(true);
        selectedEventLeaderboardGroupsTable.addColumn(leaderboardGroupNameColumn, stringMessages.name());
        selectedEventLeaderboardGroupsTable.addColumn(associatedLeaderboardsColumn, stringMessages.leaderboards());
        ListHandler<LeaderboardGroupDTO> sortHandler = new ListHandler<LeaderboardGroupDTO>(selectedEventLeaderboardGroupsProvider.getList());
        sortHandler.setComparator(leaderboardGroupNameColumn, new Comparator<LeaderboardGroupDTO>() {
            @Override
            public int compare(LeaderboardGroupDTO e1, LeaderboardGroupDTO e2) {
                return new NaturalComparator().compare(e1.getName(), e2.getName());
            }
        });
        selectedEventLeaderboardGroupsTable.addColumnSortHandler(sortHandler);
        return result;
    }

    private Widget createAvailableLeaderboardGroupsPanel() {
        availableLeaderboardGroupsTable = new BaseCelltable<LeaderboardGroupDTO>(10000000, tableRes);
        CaptionPanel result = new CaptionPanel(stringMessages.availableLeaderboardGroups());
        availableLeaderboardGroupsProvider = new ListDataProvider<>();
        availableLeaderboardGroupsProvider.setList(availableLeaderboardGroups);
        availableLeaderboardGroupsProvider.addDataDisplay(availableLeaderboardGroupsTable);
        availableLeaderboardGroupsFilterablePanel = new LabeledAbstractFilterablePanel<LeaderboardGroupDTO>(new Label(
                stringMessages.filterLeaderboardGroupsByName()), Collections.<LeaderboardGroupDTO> emptyList(),
                availableLeaderboardGroupsTable, availableLeaderboardGroupsProvider) {
            @Override
            public Iterable<String> getSearchableStrings(LeaderboardGroupDTO t) {
                List<String> result = new ArrayList<>();
                result.add(t.getName());
                return result;
            }
        };
        availableLeaderboardGroupsSelectionModel = new RefreshableMultiSelectionModel<>(
                new EntityIdentityComparator<LeaderboardGroupDTO>() {

                    @Override
                    public boolean representSameEntity(LeaderboardGroupDTO dto1, LeaderboardGroupDTO dto2) {
                        return dto1.getName().equals(dto2.getName());
                    }

                    @Override
                    public int hashCode(LeaderboardGroupDTO t) {
                        return t.getName().hashCode();
                    }
                }, availableLeaderboardGroupsFilterablePanel.getAllListDataProvider());
        availableLeaderboardGroupsSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                addToEventButton.setEnabled(!availableLeaderboardGroupsSelectionModel.getSelectedSet().isEmpty());
            }
        });
        availableLeaderboardGroupsTable.setSelectionModel(availableLeaderboardGroupsSelectionModel);
        VerticalPanel vp = new VerticalPanel();
        vp.add(availableLeaderboardGroupsFilterablePanel);
        vp.add(availableLeaderboardGroupsTable);
        result.add(vp);
        
        TextColumn<LeaderboardGroupDTO> leaderboardGroupNameColumn = new TextColumn<LeaderboardGroupDTO>() {
            @Override
            public String getValue(LeaderboardGroupDTO event) {
                return event.getName();
            }
        };
        final SafeHtmlCell leaderboardsCell = new SafeHtmlCell();
        Column<LeaderboardGroupDTO, SafeHtml> associatedLeaderboardsColumn = new Column<LeaderboardGroupDTO, SafeHtml>(leaderboardsCell) {
            @Override
            public SafeHtml getValue(LeaderboardGroupDTO leaderboardGroup) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                boolean first = true;
                for (StrippedLeaderboardDTO leaderboard : leaderboardGroup.getLeaderboards()) {
                    if (first) {
                        first = false;
                    } else {
                        builder.appendHtmlConstant("<br>");
                    }
                    builder.appendEscaped(leaderboard.getName());
                }
                return builder.toSafeHtml();
            }
        };
        
        leaderboardGroupNameColumn.setSortable(true);
        availableLeaderboardGroupsTable.addColumn(leaderboardGroupNameColumn, stringMessages.name());
        availableLeaderboardGroupsTable.addColumn(associatedLeaderboardsColumn, stringMessages.leaderboards());
        ListHandler<LeaderboardGroupDTO> sortHandler = new ListHandler<LeaderboardGroupDTO>(availableLeaderboardGroupsProvider.getList());
        sortHandler.setComparator(leaderboardGroupNameColumn, new Comparator<LeaderboardGroupDTO>() {
            @Override
            public int compare(LeaderboardGroupDTO e1, LeaderboardGroupDTO e2) {
                return new NaturalComparator().compare(e1.getName(), e2.getName());
            }
        });
        availableLeaderboardGroupsTable.addColumnSortHandler(sortHandler);
        return result;
    }

    private Widget createSwitchLeaderboardGroupsGUI() {
        VerticalPanel switchLeaderboardGroupsPanel = new VerticalPanel();
        switchLeaderboardGroupsPanel.setSpacing(5);
        switchLeaderboardGroupsPanel.setWidth("5%");
        addToEventButton = new Button("<-");
        addToEventButton.ensureDebugId("AddLeaderboardGroupsButton");
        addToEventButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addSelectedLeaderboardGroupsToEvent();
            }
        });
        addToEventButton.setEnabled(false);
        switchLeaderboardGroupsPanel.add(addToEventButton);

        removeFromEventButton = new Button("->");
        removeFromEventButton.ensureDebugId("RemoveLeaderboardGroupsButton");
        removeFromEventButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeSelectedLeaderboardGroupsFromEvent();
            }
        });
        removeFromEventButton.setEnabled(false);
        switchLeaderboardGroupsPanel.add(removeFromEventButton);
        return switchLeaderboardGroupsPanel;
    }

    private void addSelectedLeaderboardGroupsToEvent() {
        Set<LeaderboardGroupDTO> leaderboardGroupsToAdd = availableLeaderboardGroupsSelectionModel.getSelectedSet();
        List<UUID> eventLeaderboardGroupUUIDs = new ArrayList<>();
        for (LeaderboardGroupDTO lg : leaderboardGroups) {
            eventLeaderboardGroupUUIDs.add(lg.getId());
        }
        for (LeaderboardGroupDTO lg : leaderboardGroupsToAdd) {
            eventLeaderboardGroupUUIDs.add(lg.getId());
        }
    }
    
    private void removeSelectedLeaderboardGroupsFromEvent() {
        Set<LeaderboardGroupDTO> leaderboardGroupsToRemove = selectedEventLeaderboardGroupsSelectionModel.getSelectedSet();
        List<UUID> eventLeaderboardGroupUUIDs = new ArrayList<>();
        for (LeaderboardGroupDTO lg : leaderboardGroups) {
            eventLeaderboardGroupUUIDs.add(lg.getId());
        }
        for (LeaderboardGroupDTO lg : leaderboardGroupsToRemove) {
            eventLeaderboardGroupUUIDs.remove(lg.getId());
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Iterable<LeaderboardGroupDTO>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public Set<LeaderboardGroupDTO> getSelectedLeaderboardGroups() {
        return selectedEventLeaderboardGroupsSelectionModel.getSelectedSet();
    }

    public void setLeaderboardGroups(List<LeaderboardGroupDTO> leaderboardGroups) {
        selectedEventLeaderboardGroupsProvider.setList(leaderboardGroups);
    }
}
