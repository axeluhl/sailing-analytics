package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.client.EventSelectionProvider;
import com.sap.sailing.gwt.ui.client.EventsRefresher;
import com.sap.sailing.gwt.ui.client.LeaderboardGroupsDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.URLEncoder;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

/**
/**
 * A composite showing the list of all sailing events  
 * @author Frank Mittag (C5163974)
 */
public class EventListComposite extends Composite implements EventsRefresher, LeaderboardGroupsDisplayer {
    private final SailingServiceAsync sailingService;
    private final EventSelectionProvider eventSelectionProvider;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private CellTable<EventDTO> eventTable;
    private MultiSelectionModel<EventDTO> eventSelectionModel;
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
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml cell(String url, String displayName);
    }

    private static AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

    public EventListComposite(final SailingServiceAsync sailingService, final EventSelectionProvider eventSelectionProvider,
            final ErrorReporter errorReporter, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.eventSelectionProvider = eventSelectionProvider;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        availableLeaderboardGroups = Collections.emptyList();
        allEvents = new ArrayList<EventDTO>();

        mainPanel = new SimplePanel();
        panel = new VerticalPanel();
        mainPanel.setWidget(panel);

        HorizontalPanel eventControlsPanel = new HorizontalPanel();
        eventControlsPanel.setSpacing(5);
        panel.add(eventControlsPanel);
        
        Button refreshButton = new Button(stringMessages.refresh());
        refreshButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                fillEvents();
            }
        });
        eventControlsPanel.add(refreshButton);

        Button createEventBtn = new Button(stringMessages.actionAddEvent());
        createEventBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCreateEventDialog();
            }
        });
        eventControlsPanel.add(createEventBtn);

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
        eventControlsPanel.add(removeEventsButton);

        eventListDataProvider = new ListDataProvider<EventDTO>();
        eventTable = createEventTable();
        eventTable.ensureDebugId("EventsCellTable");
        eventTable.setVisible(false);

        @SuppressWarnings("unchecked")
        MultiSelectionModel<EventDTO> multiSelectionModel = (MultiSelectionModel<EventDTO>) eventTable.getSelectionModel();
        eventSelectionModel = multiSelectionModel;

        eventSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                final boolean somethingSelected = !eventSelectionModel.getSelectedSet().isEmpty();
                removeEventsButton.setEnabled(somethingSelected);
                
                List<EventDTO> selectedEvents = getSelectedEvents();
                List<UUID> selectedEventUUIDs = new ArrayList<UUID>();
                for (EventDTO selectedEvent : selectedEvents) {
                    selectedEventUUIDs.add(selectedEvent.id);
                }
                EventListComposite.this.eventSelectionProvider.setSelection(selectedEventUUIDs);
            }
        });
        
        panel.add(eventTable);
        filterTextbox = new LabeledAbstractFilterablePanel<EventDTO>(new Label(stringMessages.filterEventsByName()), allEvents,
                eventTable, eventListDataProvider) {
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
        eventControlsPanel.add(filterTextbox);
        
        noEventsLabel = new Label("No events defined yet.");
        noEventsLabel.ensureDebugId("NoRegattasLabel");
        noEventsLabel.setWordWrap(false);
        panel.add(noEventsLabel);

        fillEvents();
        
        initWidget(mainPanel);
    }

    private CellTable<EventDTO> createEventTable() {
        CellTable<EventDTO> table = new CellTable<EventDTO>(/* pageSize */10000, tableRes);
        eventListDataProvider.addDataDisplay(table);
        table.setWidth("100%");

        SelectionCheckboxColumn<EventDTO> eventSelectionCheckboxColumn = new SelectionCheckboxColumn<EventDTO>(tableRes.cellTableStyle().cellTableCheckboxSelected(),
            tableRes.cellTableStyle().cellTableCheckboxDeselected(), tableRes.cellTableStyle().cellTableCheckboxColumnCell()) {
            @Override
            protected ListDataProvider<EventDTO> getListDataProvider() {
                return eventListDataProvider;
            }

            @Override
            public Boolean getValue(EventDTO row) {
                return eventTable.getSelectionModel().isSelected(row);
            }
        };
        
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
                    builder.appendEscaped(courseArea.getName());
                    if (i < courseAreasCount) {
                        builder.appendHtmlConstant(",&nbsp;");
                        // not more than  4 course areas per line
                        if(i % 4 == 0) {
                            builder.appendHtmlConstant("<br>");
                        }
                    }
                    i++;
                }
                return builder.toSafeHtml();
            }
        };
        
        SafeHtmlCell leaderboardGroupsCell = new SafeHtmlCell();
        Column<EventDTO, SafeHtml> leaderboardGroupsColumn = new Column<EventDTO, SafeHtml>(leaderboardGroupsCell) {
            @Override
            public SafeHtml getValue(EventDTO event) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                boolean first = true;
                for (LeaderboardGroupDTO lg : event.getLeaderboardGroups()) {
                    if (first) {
                        first = false;
                    } else {
                        builder.appendHtmlConstant("<br>");
                    }
                    builder.appendEscaped(lg.getName());
                }
                return builder.toSafeHtml();
            }
        };

        TextColumn<EventDTO> imageURLsColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                String result = "";
                int imageCount = Util.size(event.getImageURLs());
                if(imageCount > 0) {
                    result = imageCount + " image(s)";
                }
                return result;
            }
        };

        TextColumn<EventDTO> videoURLsColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                String result = "";
                int videoCount = Util.size(event.getVideoURLs());
                if(videoCount > 0) {
                    result = videoCount + " video(s)";
                }
                return result;
            }
        };

        TextColumn<EventDTO> sponsorImageURLsColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                String result = "";
                int sponsorImageCount = Util.size(event.getSponsorImageURLs());
                if(sponsorImageCount > 0) {
                    result = sponsorImageCount + " sponsor image(s)";
                }
                return result;
            }
        };

        SafeHtmlCell associatedRegattasCell = new SafeHtmlCell();
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
        imageURLsColumn.setSortable(true);
        videoURLsColumn.setSortable(true);
        sponsorImageURLsColumn.setSortable(true);
        leaderboardGroupsColumn.setSortable(true);

        table.addColumn(eventSelectionCheckboxColumn, eventSelectionCheckboxColumn.getHeader());
        table.addColumn(eventNameColumn, stringMessages.event());
        table.addColumn(venueNameColumn, stringMessages.venue());
        table.addColumn(startEndDateColumn, stringMessages.from() + "/" + stringMessages.to());
        table.addColumn(isPublicColumn, stringMessages.isPublic());
        table.addColumn(courseAreasColumn, stringMessages.courseAreas());
        table.addColumn(leaderboardGroupsColumn, stringMessages.leaderboardGroups());
        table.addColumn(imageURLsColumn, stringMessages.imageURLs());
        table.addColumn(videoURLsColumn, stringMessages.videoURLs());
        table.addColumn(sponsorImageURLsColumn, stringMessages.sponsorImageURLs());
        table.addColumn(associatedRegattasColumn, stringMessages.regattas());
        table.addColumn(eventActionColumn, stringMessages.actions());
        table.setSelectionModel(eventSelectionCheckboxColumn.getSelectionModel(), eventSelectionCheckboxColumn.getSelectionManager());

        table.addColumnSortHandler(getEventTableColumnSortHandler(eventListDataProvider.getList(), eventNameColumn,
                venueNameColumn, startEndDateColumn, isPublicColumn));
        table.getColumnSortList().push(startEndDateColumn);

        return table;
    }

    private ListHandler<EventDTO> getEventTableColumnSortHandler(List<EventDTO> eventRecords,
            Column<EventDTO, SafeHtml> eventNameColumn, TextColumn<EventDTO> venueNameColumn,
            TextColumn<EventDTO> startEndDateColumn, TextColumn<EventDTO> isPublicColumn) {
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
        List<LeaderboardGroupDTO> existingLeaderboardGroups = new ArrayList<LeaderboardGroupDTO>();
        Util.addAll(availableLeaderboardGroups, existingLeaderboardGroups);
        EventCreateDialog dialog = new EventCreateDialog(Collections.unmodifiableCollection(existingEvents), existingLeaderboardGroups,
                stringMessages, new DialogCallback<EventDTO>() {
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
        List<EventDTO> existingEvents = new ArrayList<EventDTO>(eventListDataProvider.getList());
        existingEvents.remove(selectedEvent);
        List<LeaderboardGroupDTO> existingLeaderboardGroups = new ArrayList<LeaderboardGroupDTO>();
        Util.addAll(availableLeaderboardGroups, existingLeaderboardGroups);
        EventEditDialog dialog = new EventEditDialog(selectedEvent, Collections.unmodifiableCollection(existingEvents),  
                existingLeaderboardGroups, stringMessages,
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
        List<CourseAreaDTO> courseAreasToAdd = courseAreasToAddAndRemove.getA();
        List<CourseAreaDTO> courseAreasToRemove = courseAreasToAddAndRemove.getB();
        List<UUID> updatedEventLeaderboardGroupIds = new ArrayList<>();
        for (LeaderboardGroupDTO leaderboardGroup : updatedEvent.getLeaderboardGroups()) {
            updatedEventLeaderboardGroupIds.add(leaderboardGroup.getId());
        }
        sailingService.updateEvent(oldEvent.id, oldEvent.getName(), updatedEvent.getDescription(),
                updatedEvent.startDate, updatedEvent.endDate, updatedEvent.venue,
                updatedEvent.isPublic, updatedEventLeaderboardGroupIds,
                updatedEvent.getOfficialWebsiteURL(), updatedEvent.getLogoImageURL(),
                updatedEvent.getImageURLs(), updatedEvent.getVideoURLs(), updatedEvent.getSponsorImageURLs(),
                new AsyncCallback<EventDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to update sailing event" + oldEvent.getName() + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(EventDTO result) {
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
        for (CourseAreaDTO courseArea : courseAreasToAdd) {
            sailingService.createCourseArea(oldEvent.id, courseArea.getName(), new AsyncCallback<Void>() {

                @Override
                public void onFailure(Throwable t) {
                    errorReporter.reportError("Error trying to add course area to sailing event " + oldEvent.getName()
                            + ": " + t.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    fillEvents();
                }

            });
        }
        for (CourseAreaDTO courseArea : courseAreasToRemove) {
            sailingService.removeCourseArea(oldEvent.id, courseArea.id, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable t) {
                    errorReporter.reportError("Error trying to remove course area from sailing event " + oldEvent.getName()
                            + ": " + t.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    fillEvents();
                }
            });
        }
    }

    private Pair<List<CourseAreaDTO>, List<CourseAreaDTO>> getCourseAreasToAdd(final EventDTO oldEvent, final EventDTO updatedEvent) {
        List<CourseAreaDTO> courseAreasToAdd = new ArrayList<CourseAreaDTO>(updatedEvent.venue.getCourseAreas());
        courseAreasToAdd.removeAll(oldEvent.venue.getCourseAreas());
        List<CourseAreaDTO> courseAreasToRemove = new ArrayList<CourseAreaDTO>(oldEvent.venue.getCourseAreas());
        courseAreasToRemove.removeAll(updatedEvent.venue.getCourseAreas());
        return new Pair<List<CourseAreaDTO>, List<CourseAreaDTO>>(courseAreasToAdd, courseAreasToRemove);
    }

    private void createNewEvent(final EventDTO newEvent) {
        List<String> courseAreaNames = new ArrayList<String>();
        for (CourseAreaDTO courseAreaDTO : newEvent.venue.getCourseAreas()) {
            courseAreaNames.add(courseAreaDTO.getName());
        }
        sailingService.createEvent(newEvent.getName(), newEvent.getDescription(), newEvent.startDate, newEvent.endDate,
                newEvent.venue.getName(), newEvent.isPublic, courseAreaNames, newEvent.getImageURLs(),
                newEvent.getVideoURLs(), newEvent.getSponsorImageURLs(), newEvent.getLogoImageURL(),
                newEvent.getOfficialWebsiteURL(), new AsyncCallback<EventDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new event " + newEvent.getName() + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(EventDTO newEvent) {
                fillEvents();
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
                
                Set<UUID> selectedEventUUIDs = new HashSet<>();
                for (EventDTO selectedEvent : eventSelectionModel.getSelectedSet()) {
                    selectedEventUUIDs.add(selectedEvent.id);
                }
                eventSelectionModel.clear();
                allEvents.clear();
                allEvents.addAll(events);
                filterTextbox.updateAll(allEvents);
                for (EventDTO e : allEvents) {
                    if (selectedEventUUIDs.contains(e.id)) {
                        eventSelectionModel.setSelected(e, true);
                    }
                }
            }
        });
    }

    public List<EventDTO> getAllEvents() {
        return allEvents;
    }
    
    private List<EventDTO> getSelectedEvents() {
        List<EventDTO> result = new ArrayList<EventDTO>();
        if (eventListDataProvider != null) {
            for (EventDTO Event : eventListDataProvider.getList()) {
                if (eventSelectionModel.isSelected(Event)) {
                    result.add(Event);
                }
            }
        }
        return result;
    }
}
