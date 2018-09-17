package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.domain.common.security.Permission;
import com.sap.sailing.gwt.ui.adminconsole.EditOwnershipDialog.OwnershipDialogResult;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardGroupDialog.LeaderboardGroupDescriptor;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.EventsRefresher;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.MultipleLinkCell;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventBaseDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.adminconsole.HandleTabSelectable;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.FlushableCellTable;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.SelectionCheckboxColumn;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;
import com.sap.sse.security.shared.HasPermissions.DefaultModes;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.SecurityUser;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.shared.UserDTO;

/**
/**
 * A composite showing the list of all sailing events  
 * @author Frank Mittag (C5163974)
 */
public class EventListComposite extends Composite implements EventsRefresher, LeaderboardGroupsDisplayer {
    private final SailingServiceAsync sailingService;
    private final UserService userService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final com.sap.sse.security.ui.client.i18n.StringMessages securityStringMessages = GWT.create(com.sap.sse.security.ui.client.i18n.StringMessages.class);

    private CellTable<EventDTO> eventTable;
    private final RefreshableMultiSelectionModel<EventDTO> refreshableEventSelectionModel;
    private ListDataProvider<EventDTO> eventListDataProvider;
    private List<EventDTO> allEvents;
    private LabeledAbstractFilterablePanel<EventDTO> filterTextbox;
    private Button removeEventsButton;
    private final Label noEventsLabel;

    private final SimplePanel mainPanel;
    private final VerticalPanel panel;

    private Iterable<LeaderboardGroupDTO> availableLeaderboardGroups;

    public static class AnchorCell extends AbstractCell<SafeHtml> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml safeHtml, SafeHtmlBuilder sb) {
            sb.append(safeHtml);
        }
    }

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a target=\"_blank\" href=\"{0}\">{1}</a>")
        SafeHtml cell(SafeUri safeUri, String displayName);
    }

    private static AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
    private final RegattaRefresher regattaRefresher;
    private final EventsRefresher eventsRefresher;
    private final HandleTabSelectable handleTabSelectable;
    
    public EventListComposite(final SailingServiceAsync sailingService, UserService userService, final ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, EventsRefresher eventsRefresher, final HandleTabSelectable handleTabSelectable,
            final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.userService = userService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;
        this.eventsRefresher = eventsRefresher;
        this.handleTabSelectable = handleTabSelectable;
        availableLeaderboardGroups = Collections.emptyList();
        allEvents = new ArrayList<EventDTO>();

        mainPanel = new SimplePanel();
        panel = new VerticalPanel();
        mainPanel.setWidget(panel);

        HorizontalPanel eventControlsPanel = new HorizontalPanel();
        eventControlsPanel.setSpacing(5);
        panel.add(eventControlsPanel);
        
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.ensureDebugId("RefreshEventsButton");
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fillEvents();
            }
        });
        eventControlsPanel.add(refreshButton);

        Button createEventBtn = new Button(stringMessages.actionAddEvent());
        createEventBtn.ensureDebugId("CreateEventButton");
        createEventBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCreateEventDialog();
            }
        });
        eventControlsPanel.add(createEventBtn);
        if (!userService.getCurrentUser().hasPermission(Permission.EVENT.getStringPermission(DefaultModes.CREATE))) {
            createEventBtn.setVisible(false);
        }

        removeEventsButton = new Button(stringMessages.remove());
        removeEventsButton.ensureDebugId("RemoveEventsButton");
        removeEventsButton.setEnabled(false);
        removeEventsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {  
                if(askUserForConfirmation()){
                    removeEvents(refreshableEventSelectionModel.getSelectedSet());
                }
            }

            private boolean askUserForConfirmation() {
                if(refreshableEventSelectionModel.itemIsSelectedButNotVisible(eventTable.getVisibleItems())){
                    final String eventNames = refreshableEventSelectionModel.getSelectedSet().stream().map(e -> e.getName()).collect(Collectors.joining("\n"));
                    return Window.confirm(stringMessages.doYouReallyWantToRemoveNonVisibleEvents(eventNames));
                }
                return Window.confirm(stringMessages.doYouReallyWantToRemoveEvents());
            }
        });
        eventControlsPanel.add(removeEventsButton);

        eventListDataProvider = new ListDataProvider<EventDTO>();
        filterTextbox = new LabeledAbstractFilterablePanel<EventDTO>(new Label(stringMessages.filterEventsByName()), allEvents,
                new CellTable<EventDTO>(), eventListDataProvider) {
            @Override
            public Iterable<String> getSearchableStrings(EventDTO t) {
                List<String> result = new ArrayList<String>();
                result.add(t.getName());
                result.add(t.venue.getName());
                for (CourseAreaDTO c : t.venue.getCourseAreas()) {
                    result.add(c.getName());
                }
                for (LeaderboardGroupDTO lg : t.getLeaderboardGroups()) {
                    result.add(lg.getName());
                }
                return result;
            }
        };
        eventTable = createEventTable(userService.getCurrentUser());
        eventTable.ensureDebugId("EventsCellTable");
        filterTextbox.setTable(eventTable);
        @SuppressWarnings("unchecked")
        final RefreshableMultiSelectionModel<EventDTO> selectionModel = (RefreshableMultiSelectionModel<EventDTO>) eventTable.getSelectionModel();
        refreshableEventSelectionModel = selectionModel;
        eventTable.setVisible(false);

        this.refreshableEventSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final boolean somethingSelected = !refreshableEventSelectionModel.getSelectedSet().isEmpty();
                removeEventsButton.setEnabled(somethingSelected);
                final int numberOfItemsSelected = refreshableEventSelectionModel.getSelectedSet().size();
                removeEventsButton.setText(numberOfItemsSelected <= 1 ? stringMessages.remove() : stringMessages.removeNumber(numberOfItemsSelected));
            }
        });
        panel.add(filterTextbox);
        panel.add(eventTable);
        noEventsLabel = new Label(stringMessages.noEventsYet());
        noEventsLabel.ensureDebugId("NoRegattasLabel");
        noEventsLabel.setWordWrap(false);
        panel.add(noEventsLabel);
        fillEvents();
        initWidget(mainPanel);
    }

    private CellTable<EventDTO> createEventTable(UserDTO user) {
        FlushableCellTable<EventDTO> table = new FlushableCellTable<EventDTO>(/* pageSize */10000, tableRes);
        eventListDataProvider.addDataDisplay(table);
        table.setWidth("100%");

        SelectionCheckboxColumn<EventDTO> eventSelectionCheckboxColumn = new SelectionCheckboxColumn<EventDTO>(
                tableRes.cellTableStyle().cellTableCheckboxSelected(),
                tableRes.cellTableStyle().cellTableCheckboxDeselected(),
                tableRes.cellTableStyle().cellTableCheckboxColumnCell(), new EntityIdentityComparator<EventDTO>() {
                    @Override
                    public boolean representSameEntity(EventDTO dto1, EventDTO dto2) {
                        return dto1.id.equals(dto2.id);
                    }
                    @Override
                    public int hashCode(EventDTO t) {
                        return t.id.hashCode();
                    }
                },filterTextbox.getAllListDataProvider(),table);
        
        AnchorCell anchorCell = new AnchorCell();
        Column<EventDTO, SafeHtml> eventNameColumn = new Column<EventDTO, SafeHtml>(anchorCell) {
            @Override
            public SafeHtml getValue(EventDTO event) {
                String link = "";
                if(event != null && event.id != null){
                    link = EntryPointLinkFactory.createEventPlaceLink(event.id.toString(), new HashMap<String, String>());
                }
                return ANCHORTEMPLATE.cell(UriUtils.fromString(link), event.getName());
            }
        };

        TextColumn<EventDTO> venueNameColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                return event.venue != null ? event.venue.getName() : "";
            }
        };

        TextColumn<EventDTO> startEndDateColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                return DateAndTimeFormatterUtil.formatDateRange(event.startDate, event.endDate);
            }
        };

        TextColumn<EventDTO> isPublicColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                return event.isPublic ? stringMessages.yes() : stringMessages.no();
            }
        };

        SafeHtmlCell courseAreasCell = new SafeHtmlCell();
        Column<EventDTO, SafeHtml> courseAreasColumn = new Column<EventDTO, SafeHtml>(courseAreasCell) {
            @Override
            public SafeHtml getValue(EventDTO event) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                int courseAreasCount = event.venue.getCourseAreas().size();
                int i = 1;
                for (CourseAreaDTO courseArea : event.venue.getCourseAreas()) {
                    builder.appendEscaped(courseArea.getName() == null ? "null" : courseArea.getName());
                    if (i < courseAreasCount) {
                        builder.appendHtmlConstant(",&nbsp;");
                        // not more than  4 course areas per line
                        if (i % 4 == 0) {
                            builder.appendHtmlConstant("<br>");
                        }
                    }
                    i++;
                }
                return builder.toSafeHtml();
            }
        };

        MultipleLinkCell leaderboardGroupsCell = new MultipleLinkCell(true);
        Column<EventDTO, List<MultipleLinkCell.CellLink>> leaderboardGroupsColumn = new Column<EventDTO, List<MultipleLinkCell.CellLink>>(
                leaderboardGroupsCell) {
            @Override
            public List<MultipleLinkCell.CellLink> getValue(EventDTO event) {
                List<MultipleLinkCell.CellLink> links = new ArrayList<>();
                for (LeaderboardGroupDTO lg : event.getLeaderboardGroups()) {
                    links.add(new MultipleLinkCell.CellLink(lg.getName()));
                }
                return links;
            }
        };

        leaderboardGroupsCell.setOnLinkClickHandler(new ValueUpdater<String>() {
            @Override
            public void update(String value) {
                Map<String, String> params = new HashMap<>();
                params.put("LeaderBoardGroupName", value);
                handleTabSelectable.selectTabByNames(stringMessages.leaderboards(), stringMessages.leaderboardGroups(),
                        params);
            }
        });

        TextColumn<EventDTO> imagesColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                String result = "";
                int imageCount = Util.size(event.getImages());
                if(imageCount > 0) {
                    result = imageCount + " image(s)"; // TODO i18n
                }
                return result;
            }
        };

        TextColumn<EventDTO> videosColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                String result = "";
                int videoCount = Util.size(event.getVideos());
                if(videoCount > 0) {
                    result = videoCount + " video(s)"; // TODO i18n
                }
                return result;
            }
        };
        
        TextColumn<EventDTO> groupOwnerColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                final UserGroup groupOwner = event.getOwnership().getTenantOwner();
                return groupOwner == null ? "" : groupOwner.getName();
            }
        };
        TextColumn<EventDTO> userOwnerColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                final SecurityUser userOwner = event.getOwnership().getUserOwner();
                return userOwner == null ? "" : userOwner.getName();
            }
        };
        AccessControlledActionsColumn<EventDTO, EventConfigImagesBarCell> eventActionColumn =
                new AccessControlledActionsColumn<EventDTO, EventConfigImagesBarCell>(new EventConfigImagesBarCell(stringMessages)) {
            @Override
            public Iterable<DefaultModes> getAllowedActions(EventDTO event) {
                final ArrayList<DefaultModes> allowedActions = new ArrayList<>();
                for (DefaultModes action : Arrays.asList(DefaultModes.UPDATE, DefaultModes.DELETE, DefaultModes.CHANGE_OWNERSHIP)) {
                    if (user.hasPermission(Permission.EVENT.getPermissionForObjects(action, event.id.toString()),
                            event.getOwnership(), event.getAccessControlList())) {
                        allowedActions.add(action);
                    }
                }
                return allowedActions;
            }
        };
        eventActionColumn.setFieldUpdater(new FieldUpdater<EventDTO, String>() {
            @Override
            public void update(int index, EventDTO event, String value) {
                if (DefaultModes.DELETE.name().equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveEvent(event.getName()))) {
                        removeEvent(event);
                    }
                } else if (DefaultModes.UPDATE.name().equals(value)) {
                    openEditEventDialog(event);
                } else if (DefaultModes.CHANGE_OWNERSHIP.name().equals(value)) {
                    openOwnershipDialog(userService.getUserManagementService(), event);
                }
            }
        });

        eventNameColumn.setSortable(true);
        venueNameColumn.setSortable(true);
        isPublicColumn.setSortable(true);
        startEndDateColumn.setSortable(true);
        courseAreasColumn.setSortable(true);
        leaderboardGroupsColumn.setSortable(true);
        groupOwnerColumn.setSortable(true);
        userOwnerColumn.setSortable(true);

        table.addColumn(eventSelectionCheckboxColumn, eventSelectionCheckboxColumn.getHeader());
        table.addColumn(eventNameColumn, stringMessages.event());
        table.addColumn(venueNameColumn, stringMessages.venue());
        table.addColumn(startEndDateColumn, stringMessages.from() + "/" + stringMessages.to());
        table.addColumn(isPublicColumn, stringMessages.isPublic());
        table.addColumn(courseAreasColumn, stringMessages.courseAreas());
        table.addColumn(leaderboardGroupsColumn, stringMessages.leaderboardGroups());
        table.addColumn(imagesColumn, stringMessages.images());
        table.addColumn(videosColumn, stringMessages.videos());
        table.addColumn(groupOwnerColumn, securityStringMessages.group());
        table.addColumn(userOwnerColumn, securityStringMessages.user());
        table.addColumn(eventActionColumn, stringMessages.actions());
        table.setSelectionModel(eventSelectionCheckboxColumn.getSelectionModel(), eventSelectionCheckboxColumn.getSelectionManager());

        table.addColumnSortHandler(getEventTableColumnSortHandler(eventListDataProvider.getList(),
                eventSelectionCheckboxColumn, eventNameColumn, venueNameColumn, startEndDateColumn, isPublicColumn,
                courseAreasColumn, leaderboardGroupsColumn, groupOwnerColumn, userOwnerColumn));
        table.getColumnSortList().push(startEndDateColumn);

        return table;
    }

    private void openOwnershipDialog(UserManagementServiceAsync userManagementService, EventDTO event) {
        new EditOwnershipDialog(userManagementService, event.getOwnership(), securityStringMessages, new DialogCallback<OwnershipDialogResult>() {
            @Override
            public void ok(OwnershipDialogResult editedObject) {
                userManagementService.setOwnership(editedObject.getOwnership(), Permission.EVENT.getQualifiedObjectIdentifier(event.id.toString()), event.getName(),
                        new AsyncCallback<QualifiedObjectIdentifier>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.errorUpdatingOwnership(event.getName()));
                            }

                            @Override
                            public void onSuccess(QualifiedObjectIdentifier result) {
                                event.setOwnership(editedObject.getOwnership());
                                updateEvent(event, event);
                            }
                    
                });
            }

            @Override
            public void cancel() {
            }
        }).show();
    }
    
    private ListHandler<EventDTO> getEventTableColumnSortHandler(List<EventDTO> eventRecords,
            SelectionCheckboxColumn<EventDTO> eventSelectionCheckboxColumn, Column<EventDTO, SafeHtml> eventNameColumn,
            TextColumn<EventDTO> venueNameColumn, TextColumn<EventDTO> startEndDateColumn,
            TextColumn<EventDTO> isPublicColumn, Column<EventDTO, SafeHtml> courseAreasColumn,
            Column<EventDTO, List<MultipleLinkCell.CellLink>> leaderboardGroupsColumn,
            TextColumn<EventDTO> groupOwnerColumn, TextColumn<EventDTO> userOwnerColumn) {
        ListHandler<EventDTO> result = new ListHandler<EventDTO>(eventRecords);
        result.setComparator(eventSelectionCheckboxColumn, eventSelectionCheckboxColumn.getComparator());
        result.setComparator(eventNameColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return new NaturalComparator().compare(e1.getName(), e2.getName());
            }
        });
        result.setComparator(venueNameColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return new NaturalComparator().compare(e1.venue.getName(), e2.venue.getName());
            }
        });
        result.setComparator(startEndDateColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                int result;
                if(e1.startDate != null && e2.startDate != null) {
                    result = e2.startDate.compareTo(e1.startDate);
                } else if(e1.startDate == null && e2.startDate != null) {
                    result = 1;
                } else if(e1.startDate != null && e2.startDate == null) {
                    result = -1;
                } else {
                    result = 0;
                }
                return result;
            }
        });
        result.setComparator(isPublicColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return e1.isPublic == e2.isPublic ? 0 : e1.isPublic ? 1 : -1;
            }
        });
        result.setComparator(courseAreasColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return e1.venue.getCourseAreas().toString().compareTo(e2.venue.getCourseAreas().toString());
            }
        });
        result.setComparator(leaderboardGroupsColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return e1.getLeaderboardGroups().toString().compareTo(e2.getLeaderboardGroups().toString());
            }
        });
        result.setComparator(groupOwnerColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return (e1.getOwnership().getTenantOwner()==null?"":e1.getOwnership().getTenantOwner().getName()).compareTo(e2.getOwnership().getTenantOwner()==null?"":e2.getOwnership().getTenantOwner().getName());
            }
        });
        result.setComparator(userOwnerColumn, new Comparator<EventDTO>() {
            @Override
            public int compare(EventDTO e1, EventDTO e2) {
                return (e1.getOwnership().getUserOwner()==null?"":e1.getOwnership().getUserOwner().getName()).compareTo(e2.getOwnership().getUserOwner()==null?"":e2.getOwnership().getUserOwner().getName());
            }
        });
        return result;
    }

    private void removeEvents(Collection<EventDTO> events) {
        if (!events.isEmpty()) {
            Collection<UUID> eventIds = new HashSet<UUID>();
            for (EventDTO event : events) {
                eventIds.add(event.id);
            }
            sailingService.removeEvents(eventIds, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to remove the events:" + caught.getMessage());
                }
                @Override
                public void onSuccess(Void result) {
                    fillEvents();
                }
            });
        }
    }

    private void removeEvent(final EventDTO event) {
        sailingService.removeEvent(event.id, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to remove event " + event.getName() + ": " + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                fillEvents();
            }
        });
    }

    private void openCreateEventDialog() {
        List<EventDTO> existingEvents = new ArrayList<EventDTO>(eventListDataProvider.getList());
        final List<LeaderboardGroupDTO> existingLeaderboardGroups = new ArrayList<LeaderboardGroupDTO>();
        Util.addAll(availableLeaderboardGroups, existingLeaderboardGroups);
        EventCreateDialog dialog = new EventCreateDialog(Collections.unmodifiableCollection(existingEvents), existingLeaderboardGroups,
                sailingService, stringMessages, new DialogCallback<EventDTO>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(final EventDTO newEvent) {
                createNewEvent(newEvent, existingLeaderboardGroups);
            }
        });
        dialog.ensureDebugId("EventCreateDialog");
        dialog.show();
    }
    
    private void openCreateDefaultRegattaDialog(final EventDTO createdEvent) {
        CreateDefaultRegattaDialog dialog = new CreateDefaultRegattaDialog(sailingService, stringMessages, errorReporter, new DialogCallback<Void>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(Void editedObject) {
                sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                openCreateRegattaDialog(Collections.<RegattaDTO>emptyList(), Collections.<EventDTO>emptyList(), createdEvent);
                            }

                            @Override
                            public void onSuccess(List<EventDTO> result) {
                                openCreateRegattaDialog(Collections.<RegattaDTO>emptyList(), Collections.unmodifiableList(result), createdEvent);
                            }
                        });

                    }

                    @Override
                    public void onSuccess(final List<RegattaDTO> existingRegattas) {
                        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                openCreateRegattaDialog(existingRegattas, Collections.<EventDTO>emptyList(), createdEvent);
                            }

                            @Override
                            public void onSuccess(List<EventDTO> result) {
                                openCreateRegattaDialog(existingRegattas, Collections.unmodifiableList(result), createdEvent);
                            }
                        });                        
                    }
                });
                                
                
            }
        });
        dialog.ensureDebugId("CreateDefaultRegattaDialog");
        dialog.show();
    }
    
    private void openCreateRegattaDialog(List<RegattaDTO> existingRegattas,
            List<EventDTO> existingEvents, EventDTO createdEvent) {
        RegattaWithSeriesAndFleetsCreateDialog dialog = new RegattaWithSeriesAndFleetsCreateDialog(existingRegattas, existingEvents, createdEvent, stringMessages,
                new CreateRegattaCallback(sailingService, stringMessages, errorReporter, regattaRefresher, eventsRefresher, existingEvents));
        dialog.ensureDebugId("RegattaCreateDialog");
        dialog.show();
    }
    
    /**
     * @param newEvent the new event as created by the server, already including a valid {@link EventBaseDTO#id} value.
     */
    private void openLeaderboardGroupCreationDialog(final List<LeaderboardGroupDTO> existingLeaderboardGroups, final EventDTO newEvent) {
        LeaderboardGroupCreateDialog leaderboardGroupCreateDialog = new LeaderboardGroupCreateDialog(existingLeaderboardGroups, stringMessages, new DialogCallback<LeaderboardGroupDialog.LeaderboardGroupDescriptor>() {
            @Override
            public void ok(final LeaderboardGroupDescriptor newGroup) {
                sailingService.createLeaderboardGroup(newGroup.getName(), newGroup.getDescription(),
                        newGroup.getDisplayName(), newGroup.isDisplayLeaderboardsInReverseOrder(),
                        newGroup.getOverallLeaderboardDiscardThresholds(), newGroup.getOverallLeaderboardScoringSchemeType(), new MarkedAsyncCallback<LeaderboardGroupDTO>(
                                new AsyncCallback<LeaderboardGroupDTO>() {
                                    @Override
                                    public void onFailure(Throwable t) {
                                        errorReporter.reportError(stringMessages.errorCreatingLeaderboardGroup(newGroup.getName())
                                                + ": " + t.getMessage());
                                    }
                                    @Override
                                    public void onSuccess(LeaderboardGroupDTO newGroup) {
                                        newEvent.addLeaderboardGroup(newGroup);
                                        // fillEvents() will have replaced newEvent in allEvents by a new copy coming from the server which
                                        // doesn't know about the new leaderboard group yet. An updateEvent call will link the leaderboard group
                                        // to the event on the server
                                        EventDTO matchingEvent = null;
                                        for (EventDTO event : allEvents) {
                                            if (event.id.equals(newEvent.id)) {
                                                matchingEvent = event;
                                            }
                                        }
                                        if (matchingEvent != null) {
                                            updateEvent(matchingEvent, newEvent);
                                        } else {
                                            errorReporter.reportError("Could not find the event with name "+newEvent.getName()+" to which the leaderboardgroup should be added");
                                        }
                                        openCreateDefaultRegattaDialog(newEvent);
                                    }
                                }));
            }

            @Override
            public void cancel() {
            }
        });
        leaderboardGroupCreateDialog.setFieldsBasedOnEventName(newEvent.getName(), newEvent.getDescription());
        leaderboardGroupCreateDialog.ensureDebugId("LeaderboardGroupCreateDialog");
        leaderboardGroupCreateDialog.show();
    }

    private void openEditEventDialog(final EventDTO selectedEvent) {
        List<EventDTO> existingEvents = new ArrayList<EventDTO>(eventListDataProvider.getList());
        existingEvents.remove(selectedEvent);
        List<LeaderboardGroupDTO> existingLeaderboardGroups = new ArrayList<LeaderboardGroupDTO>();
        Util.addAll(availableLeaderboardGroups, existingLeaderboardGroups);
        EventEditDialog dialog = new EventEditDialog(selectedEvent, Collections.unmodifiableCollection(existingEvents),  
                existingLeaderboardGroups, sailingService, stringMessages,
                new DialogCallback<EventDTO>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(EventDTO updatedEvent) {
                updateEvent(selectedEvent, updatedEvent);
            }
        });
        dialog.show();
    }

    private void updateEvent(final EventDTO oldEvent, final EventDTO updatedEvent) {
        Pair<List<CourseAreaDTO>, List<CourseAreaDTO>> courseAreasToAddAndRemove = getCourseAreasToAdd(oldEvent, updatedEvent);
        final List<CourseAreaDTO> courseAreasToAdd = courseAreasToAddAndRemove.getA();
        final List<CourseAreaDTO> courseAreasToRemove = courseAreasToAddAndRemove.getB();
        final Iterable<UUID> updatedEventLeaderboardGroupIds = updatedEvent.getLeaderboardGroupIds();
        sailingService.updateEvent(oldEvent.id, oldEvent.getName(), updatedEvent.getDescription(),
                updatedEvent.startDate, updatedEvent.endDate, updatedEvent.venue,
                updatedEvent.isPublic, updatedEventLeaderboardGroupIds,
                updatedEvent.getOfficialWebsiteURL(),
                updatedEvent.getBaseURL(),
                updatedEvent.getSailorsInfoWebsiteURLs(), updatedEvent.getImages(),
                updatedEvent.getVideos(), updatedEvent.getWindFinderReviewedSpotsCollectionIds(), new AsyncCallback<EventDTO>() {
         @Override
         public void onFailure(Throwable t) {
        errorReporter.reportError("Error trying to update sailing event" + oldEvent.getName() + ": " + t.getMessage());
         }

         @Override
         public void onSuccess(EventDTO result) {
        fillEvents();
        final String[] namesOfCourseAreasToAdd = new String[courseAreasToAdd.size()];
        int i=0;
        for (CourseAreaDTO courseAreaToAdd : courseAreasToAdd) {
            namesOfCourseAreasToAdd[i++] = courseAreaToAdd.getName();
        }
        sailingService.createCourseAreas(oldEvent.id, namesOfCourseAreasToAdd, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to add course area to sailing event " + oldEvent.getName()
                        + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                final UUID[] idsOfCourseAreasToRemove = new UUID[courseAreasToRemove.size()];
                int j=0;
                for (CourseAreaDTO courseAreaToRemove : courseAreasToRemove) {
                    idsOfCourseAreasToRemove[j++] = courseAreaToRemove.id;
                }
                sailingService.removeCourseAreas(oldEvent.id, idsOfCourseAreasToRemove, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to remove course area from sailing event " + oldEvent.getName()
                                + ": " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        fillEvents();
                        if (!oldEvent.getName().equals(updatedEvent.getName())) {
                            sailingService.renameEvent(oldEvent.id, updatedEvent.getName(), new AsyncCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    errorReporter.reportError("Error trying to rename sailing event " + oldEvent.getName() + ": " + t.getMessage());
                                }
                            });
                        }
                    }
                });
            }
        });
         }
      });
    }

    private Pair<List<CourseAreaDTO>, List<CourseAreaDTO>> getCourseAreasToAdd(final EventDTO oldEvent, final EventDTO updatedEvent) {
        List<CourseAreaDTO> courseAreasToAdd = new ArrayList<CourseAreaDTO>(updatedEvent.venue.getCourseAreas());
        courseAreasToAdd.removeAll(oldEvent.venue.getCourseAreas());
        List<CourseAreaDTO> courseAreasToRemove = new ArrayList<CourseAreaDTO>(oldEvent.venue.getCourseAreas());
        courseAreasToRemove.removeAll(updatedEvent.venue.getCourseAreas());
        return new Pair<List<CourseAreaDTO>, List<CourseAreaDTO>>(courseAreasToAdd, courseAreasToRemove);
    }

    private void createNewEvent(final EventDTO newEvent, final List<LeaderboardGroupDTO> existingLeaderboardGroups) {
        List<String> courseAreaNames = new ArrayList<String>();
        for (CourseAreaDTO courseAreaDTO : newEvent.venue.getCourseAreas()) {
            courseAreaNames.add(courseAreaDTO.getName());
        }
        sailingService.createEvent(newEvent.getName(), newEvent.getDescription(), newEvent.startDate, newEvent.endDate,
                newEvent.venue.getName(), newEvent.isPublic, courseAreaNames, newEvent.getOfficialWebsiteURL(), newEvent.getBaseURL(),
                newEvent.getSailorsInfoWebsiteURLs(), newEvent.getImages(), newEvent.getVideos(), newEvent.getLeaderboardGroupIds(),
                userService.getCurrentUser().getDefaultTenant().getName(), new AsyncCallback<EventDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new event " + newEvent.getName() + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(final EventDTO newEvent) {
                fillEvents();
                if (newEvent.getLeaderboardGroups().isEmpty()) {
                    // show simple Dialog
                    DataEntryDialog<Void> dialog = new CreateDefaultLeaderboardGroupDialog(
                            sailingService, stringMessages, errorReporter, new DialogCallback<Void>() {
                        @Override
                        public void ok(Void editedObject) {
                            openLeaderboardGroupCreationDialog(existingLeaderboardGroups, newEvent);
                        }

                        @Override
                        public void cancel() {
                        }
                    });
                    dialog.ensureDebugId("CreateDefaultLeaderboardGroupConfirmDialog");
                    dialog.show();
                } else {
                    openCreateDefaultRegattaDialog(newEvent);
                }
            }
        });
    }

    @Override
    public void fillLeaderboardGroups(Iterable<LeaderboardGroupDTO> leaderboardGroups) {
        availableLeaderboardGroups = leaderboardGroups;
    }

    @Override
    public void fillEvents() {
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getEvents() - Failure: " + caught.getMessage());
            }

            @Override
            public void onSuccess(List<EventDTO> events) {
                if (events.isEmpty()) {
                    eventTable.setVisible(false);
                    noEventsLabel.setVisible(true);
                } else {
                    eventTable.setVisible(true);
                    noEventsLabel.setVisible(false);
                }
                allEvents.clear();
                allEvents.addAll(events);
                filterTextbox.updateAll(allEvents);
                eventTable.redraw();
            }
        });
    }

    public List<EventDTO> getAllEvents() {
        return allEvents;
    }
    
    public RefreshableMultiSelectionModel<EventDTO> getRefreshableMultiSelectionModel() {
        return refreshableEventSelectionModel;
    }

    @Override
    public void setupLeaderboardGroups(Map<String, String> params) {
    }
}
