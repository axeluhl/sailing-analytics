package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventsRefresher;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLEncoder;
import com.sap.sailing.gwt.ui.client.shared.panels.LabeledAbstractFilterablePanel;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/**
 * Allows administrators to manage data of a sailing event. This is a temporary panel because the managed event is not
 * connected with any regatta and it used only to manage of basic attributes of the an event.
 * 
 * @author Frank Mittag (C5163974)
 * @author Axel Uhl (d043530)
 */
public class EventManagementPanel extends SimplePanel implements EventsRefresher, LeaderboardGroupsDisplayer {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private CellTable<EventDTO> eventTable;
    private MultiSelectionModel<EventDTO> eventSelectionModel;
    private ListDataProvider<EventDTO> eventProvider;
    private List<EventDTO> allEvents;
    private LabeledAbstractFilterablePanel<EventDTO> filterTextbox;
    private Button removeEventsButton;
    private TextColumn<EventDTO> startEndDateColumn;

    private LabeledAbstractFilterablePanel<LeaderboardGroupDTO> selectedEventLeaderboardGroupsFilterablePanel;
    private CellTable<LeaderboardGroupDTO> selectedEventLeaderboardGroupsTable;
    private MultiSelectionModel<LeaderboardGroupDTO> selectedEventLeaderboardGroupsSelectionModel;
    private ListDataProvider<LeaderboardGroupDTO> selectedEventLeaderboardGroupsProvider;
    
    private LabeledAbstractFilterablePanel<LeaderboardGroupDTO> availableLeaderboardGroupsFilterablePanel;
    private CellTable<LeaderboardGroupDTO> availableLeaderboardGroupsTable;
    private MultiSelectionModel<LeaderboardGroupDTO> availableLeaderboardGroupsSelectionModel;
    private ListDataProvider<LeaderboardGroupDTO> availableLeaderboardGroupsProvider;

    public static class AnchorCell extends AbstractCell<SafeHtml> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml safeHtml, SafeHtmlBuilder sb) {
            sb.append(safeHtml);
        }
    }

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml cell(String url, String displayName);
    }

    private static AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
    private final HorizontalPanel splitPanel;

    public EventManagementPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");

        HorizontalPanel eventsPanel = new HorizontalPanel();
        eventsPanel.setSpacing(5);
        mainPanel.add(eventsPanel);
        
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fillEvents();
            }
        });
        eventsPanel.add(refreshButton);

        Button createEventBtn = new Button(stringMessages.actionAddEvent());
        createEventBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCreateEventDialog();
            }
        });
        eventsPanel.add(createEventBtn);

        removeEventsButton = new Button(stringMessages.remove());
        removeEventsButton.setEnabled(false);
        removeEventsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm(stringMessages.doYouReallyWantToRemoveEvents())) {
                    removeEvents(eventSelectionModel.getSelectedSet());
                }
            }
        });
        eventsPanel.add(removeEventsButton);

        // sailing events table
        AnchorCell anchorCell = new AnchorCell();
        Column<EventDTO, SafeHtml> eventNameColumn = new Column<EventDTO, SafeHtml>(anchorCell) {
            @Override
            public SafeHtml getValue(EventDTO event) {
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = URLEncoder.encode("/gwt/RegattaOverview.html?event=" + event.id
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                return ANCHORTEMPLATE.cell(link, event.getName());
            }
        };

        TextColumn<EventDTO> venueNameColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                return event.venue != null ? event.venue.getName() : "";
            }
        };

        startEndDateColumn = new TextColumn<EventDTO>() {
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

        final SafeHtmlCell courseAreasCell = new SafeHtmlCell();
        Column<EventDTO, SafeHtml> courseAreasColumn = new Column<EventDTO, SafeHtml>(courseAreasCell) {
            @Override
            public SafeHtml getValue(EventDTO event) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                int courseAreasCount = event.venue.getCourseAreas().size();
                int i = 1;
                for (CourseAreaDTO courseArea : event.venue.getCourseAreas()) {
                    builder.appendEscaped(courseArea.getName());
                    if (i < courseAreasCount) {
                        builder.appendHtmlConstant("<br>");
                    }
                    i++;
                }
                return builder.toSafeHtml();
            }
        };
        
        final SafeHtmlCell leaderboardGroupsCell = new SafeHtmlCell();
        Column<EventDTO, SafeHtml> leaderboardGroupsColumn = new Column<EventDTO, SafeHtml>(leaderboardGroupsCell) {
            @Override
            public SafeHtml getValue(EventDTO event) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                boolean first = true;
                for (LeaderboardGroupDTO lg : event.getLeaderboardGroups()) {
                    builder.appendEscaped(lg.getName());
                    if (first) {
                        first = false;
                    } else {
                        builder.appendHtmlConstant("<br>");
                    }
                }
                return builder.toSafeHtml();
            }
        };

        final SafeHtmlCell associatedRegattasCell = new SafeHtmlCell();
        Column<EventDTO, SafeHtml> associatedRegattasColumn = new Column<EventDTO, SafeHtml>(associatedRegattasCell) {
            @Override
            public SafeHtml getValue(EventDTO event) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                int regattaCount = event.regattas.size();
                int i = 1;
                for (RegattaDTO regatta : event.regattas) {
                    builder.appendEscaped(regatta.getName());
                    if (i < regattaCount) {
                        builder.appendHtmlConstant("<br>");
                    }
                    i++;
                }
                return builder.toSafeHtml();
            }
        };
        
        ImagesBarColumn<EventDTO, EventConfigImagesBarCell> eventActionColumn = new ImagesBarColumn<EventDTO, EventConfigImagesBarCell>(
                new EventConfigImagesBarCell(stringMessages));
        eventActionColumn.setFieldUpdater(new FieldUpdater<EventDTO, String>() {
            @Override
            public void update(int index, EventDTO event, String value) {
                if (EventConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveEvent(event.getName()))) {
                        removeEvent(event);
                    }
                } else if (EventConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    openEditEventDialog(event);
                }
            }
        });

        eventNameColumn.setSortable(true);
        venueNameColumn.setSortable(true);
        isPublicColumn.setSortable(true);
        startEndDateColumn.setSortable(true);
        courseAreasColumn.setSortable(true);
        leaderboardGroupsColumn.setSortable(true);

        eventTable = new CellTable<EventDTO>(10000, tableRes);
        eventTable.addColumn(eventNameColumn, stringMessages.event());
        eventTable.addColumn(venueNameColumn, stringMessages.venue());
        eventTable.addColumn(startEndDateColumn, stringMessages.from() + "/" + stringMessages.to());
        eventTable.addColumn(isPublicColumn, stringMessages.isPublic());
        eventTable.addColumn(courseAreasColumn, stringMessages.courseAreas());
        eventTable.addColumn(leaderboardGroupsColumn, stringMessages.leaderboardGroups());
        eventTable.addColumn(associatedRegattasColumn, stringMessages.regattas());
        eventTable.addColumn(eventActionColumn, stringMessages.actions());

        eventSelectionModel = new MultiSelectionModel<EventDTO>();
        eventSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final boolean somethingSelected = !eventSelectionModel.getSelectedSet().isEmpty();
                removeEventsButton.setEnabled(somethingSelected);
            }
        });
        eventTable.setSelectionModel(eventSelectionModel);

        eventProvider = new ListDataProvider<EventDTO>();
        eventProvider.addDataDisplay(eventTable);
        mainPanel.add(eventTable);
        allEvents = new ArrayList<EventDTO>();
        eventTable.addColumnSortHandler(getEventTableColumnSortHandler(eventProvider.getList(), eventNameColumn,
                venueNameColumn, startEndDateColumn, isPublicColumn, courseAreasColumn));
        filterTextbox = new LabeledAbstractFilterablePanel<EventDTO>(new Label(stringMessages.filterEventsByName()), allEvents,
                eventTable, eventProvider) {
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
        eventsPanel.add(filterTextbox);
        fillEvents();

        // prepare the caption panels and buttons for leaderboard group assignment but leave invisible for now
        splitPanel = new HorizontalPanel();
        splitPanel.ensureDebugId("EventLeaderboardGroupsAssignmentDetailsPanel");
        splitPanel.setSpacing(5);
        splitPanel.setWidth("100%");
        splitPanel.setVisible(false);
        mainPanel.add(splitPanel);

        splitPanel.setVerticalAlignment(HorizontalPanel.ALIGN_TOP);
//        splitPanel.add(createLeaderboardGroupOfEventsPanel(tableRes)); TODO
        splitPanel.add(createSwitchLeaderboardGroupsGUI());
//        splitPanel.add(createAvailableLeaderboardGroupsPanel(tableRes)); TODO
    }

    private Widget createSwitchLeaderboardGroupsGUI() {
        VerticalPanel switchLeaderboardGroupsPanel = new VerticalPanel();
        switchLeaderboardGroupsPanel.setSpacing(5);
        switchLeaderboardGroupsPanel.setWidth("5%");

        Button addToEventButton = new Button("<-");
        addToEventButton.ensureDebugId("AddLeaderboardGroupsButton");
        addToEventButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
//                addSelectedLeaderboardGroupsToEvent(); TODO
            }
        });
        addToEventButton.setEnabled(false);
        switchLeaderboardGroupsPanel.add(addToEventButton);

        Button removeFromEventButton = new Button("->");
        removeFromEventButton.ensureDebugId("RemoveLeaderboardGroupsButton");
        removeFromEventButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
//                removeSelectedLeaderboardGroupsFromEvent(); TODO
            }
        });
        removeFromEventButton.setEnabled(false);
        switchLeaderboardGroupsPanel.add(removeFromEventButton);
        return switchLeaderboardGroupsPanel;
    }

    private ListHandler<EventDTO> getEventTableColumnSortHandler(List<EventDTO> eventRecords,
            Column<EventDTO, SafeHtml> eventNameColumn, TextColumn<EventDTO> venueNameColumn,
            TextColumn<EventDTO> startEndDateColumn, TextColumn<EventDTO> isPublicColumn,
            Column<EventDTO, SafeHtml> courseAreasColumn) {
        ListHandler<EventDTO> result = new ListHandler<EventDTO>(eventRecords);
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
                return new NaturalComparator().compare(Arrays.toString(e1.venue.getCourseAreas().toArray()),
                        Arrays.toString(e2.venue.getCourseAreas().toArray()));
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
        List<EventDTO> existingEvents = new ArrayList<EventDTO>(eventProvider.getList());
        EventCreateDialog dialog = new EventCreateDialog(Collections.unmodifiableCollection(existingEvents), stringMessages,
                new DialogCallback<EventDTO>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(EventDTO newEvent) {
                createNewEvent(newEvent);
            }
        });
        dialog.show();
    }

    private void openEditEventDialog(final EventDTO selectedEvent) {
        List<EventDTO> existingEvents = new ArrayList<EventDTO>(eventProvider.getList());
        existingEvents.remove(selectedEvent);
        EventEditDialog dialog = new EventEditDialog(selectedEvent, Collections.unmodifiableCollection(existingEvents), stringMessages,
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
        List<CourseAreaDTO> courseAreasToAdd = getCourseAreasToAdd(oldEvent, updatedEvent);
        List<UUID> updatedEventLeaderboardGroupIds = new ArrayList<>();
        for (LeaderboardGroupDTO leaderboardGroup : updatedEvent.getLeaderboardGroups()) {
            updatedEventLeaderboardGroupIds.add(leaderboardGroup.getId());
        }
        sailingService.updateEvent(oldEvent.id, oldEvent.getName(), updatedEvent.startDate, updatedEvent.endDate, updatedEvent.venue,
                updatedEvent.isPublic, updatedEventLeaderboardGroupIds, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to update sailing event" + oldEvent.getName() + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                fillEvents();
                if (!oldEvent.getName().equals(updatedEvent.getName())) {
                    sailingService.renameEvent(oldEvent.id, updatedEvent.getName(), new AsyncCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            fillEvents();
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            errorReporter.reportError("Error trying to rename sailing event " + oldEvent.getName() + ": " + t.getMessage());
                                            
                        }
                    });
                }
            }
        });

        if (courseAreasToAdd.size() > 0) {
            for (CourseAreaDTO courseArea : courseAreasToAdd) {
                sailingService.createCourseArea(oldEvent.id, courseArea.getName(), new AsyncCallback<CourseAreaDTO>() {

                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to add course area to sailing event " + oldEvent.getName() + ": " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(CourseAreaDTO result) {
                        fillEvents();
                    }

                });
            }
        }
    }

    private List<CourseAreaDTO> getCourseAreasToAdd(final EventDTO oldEvent, final EventDTO updatedEvent) {
        List<CourseAreaDTO> courseAreasToAdd = new ArrayList<CourseAreaDTO>();
        List<String> courseAreaNamesToAdd = new ArrayList<String>();

        List<String> newCourseAreaNames = new ArrayList<String>();
        for (CourseAreaDTO courseArea : updatedEvent.venue.getCourseAreas()) {
            newCourseAreaNames.add(courseArea.getName());
        }

        List<String> oldCourseAreaNames = new ArrayList<String>();
        for (CourseAreaDTO courseArea : oldEvent.venue.getCourseAreas()) {
            oldCourseAreaNames.add(courseArea.getName());
        }

        for (String newCourseAreaName : newCourseAreaNames) {
            if (!oldCourseAreaNames.contains(newCourseAreaName))
                courseAreaNamesToAdd.add(newCourseAreaName);
        }

        for (CourseAreaDTO courseArea : updatedEvent.venue.getCourseAreas()) {
            if (courseAreaNamesToAdd.contains(courseArea.getName())) {
                courseAreasToAdd.add(courseArea);
            }
        }

        return courseAreasToAdd;
    }

    private void createNewEvent(final EventDTO newEvent) {
        List<String> courseAreaNames = new ArrayList<String>();
        for (CourseAreaDTO courseAreaDTO : newEvent.venue.getCourseAreas()) {
            courseAreaNames.add(courseAreaDTO.getName());
        }
        sailingService.createEvent(newEvent.getName(), newEvent.startDate, newEvent.endDate, newEvent.venue.getName(),
                newEvent.isPublic, courseAreaNames, new AsyncCallback<EventDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new event" + newEvent.getName() + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(EventDTO newEvent) {
                fillEvents();
            }
        });
    }

    public void fillEvents() {
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getEvents() - Failure: " + caught.getMessage());
            }

            @Override
            public void onSuccess(List<EventDTO> result) {
                allEvents.clear();
                allEvents.addAll(result);
                eventTable.getColumnSortList().push(startEndDateColumn);
                filterTextbox.updateAll(allEvents);
            }
        });
    }

    @Override
    public void fillLeaderboardGroups(Iterable<LeaderboardGroupDTO> leaderboardGroups) {
        // TODO Auto-generated method stub
        
    }
}
