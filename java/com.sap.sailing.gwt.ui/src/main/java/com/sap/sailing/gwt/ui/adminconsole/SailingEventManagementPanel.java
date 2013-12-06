package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.EventRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLEncoder;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;

/**
 * Allows administrators to manage data of a sailing event. This is a temporary panel because the managed event
 * is not connected with any regatta and it used only to manage of basic attributes of the an event.
 * @author Frank Mittag (C5163974)
 * 
 */
public class SailingEventManagementPanel extends SimplePanel implements EventRefresher {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private CellTable<EventDTO> eventTable;
    private MultiSelectionModel<EventDTO> eventSelectionModel;
    private ListDataProvider<EventDTO> eventProvider;
    private Button removeEventsButton;

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

    public SailingEventManagementPanel(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
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

        TextColumn<EventDTO> publicationUrlColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                return event.publicationUrl;
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

        eventTable = new CellTable<EventDTO>(10000, tableRes);
        eventTable.addColumn(eventNameColumn, stringMessages.event());
        eventTable.addColumn(venueNameColumn, stringMessages.venue());
        eventTable.addColumn(publicationUrlColumn, stringMessages.publicationUrl());
        eventTable.addColumn(isPublicColumn, stringMessages.isPublic());
        eventTable.addColumn(courseAreasColumn, stringMessages.courseAreas());
        eventTable.addColumn(eventActionColumn, stringMessages.actions());

        eventSelectionModel = new MultiSelectionModel<EventDTO>();
        eventSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                removeEventsButton.setEnabled(!eventSelectionModel.getSelectedSet().isEmpty());
            }
        });
        eventTable.setSelectionModel(eventSelectionModel);

        eventProvider = new ListDataProvider<EventDTO>();
        eventProvider.addDataDisplay(eventTable);
        mainPanel.add(eventTable);

        fillEvents();
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
        List<String> regattaNames = new ArrayList<String>();
        //        for(RegattaDTO regatta: updatedEvent.regattas) {
        //            regattaNames.add(regatta.name);
        //        }
        List<CourseAreaDTO> courseAreasToAdd = getCourseAreasToAdd(oldEvent, updatedEvent);

        sailingService.updateEvent(oldEvent.getName(), oldEvent.id, updatedEvent.venue, updatedEvent.publicationUrl, 
                updatedEvent.isPublic, regattaNames, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to update sailing event" + oldEvent.getName() + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                fillEvents();
                if(!oldEvent.getName().equals(updatedEvent.getName())) {
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
        for(CourseAreaDTO courseArea : updatedEvent.venue.getCourseAreas()) {
            newCourseAreaNames.add(courseArea.getName());
        }

        List<String> oldCourseAreaNames = new ArrayList<String>();
        for(CourseAreaDTO courseArea : oldEvent.venue.getCourseAreas()) {
            oldCourseAreaNames.add(courseArea.getName());
        }

        for (String newCourseAreaName : newCourseAreaNames) {
            if (!oldCourseAreaNames.contains(newCourseAreaName))
                courseAreaNamesToAdd.add(newCourseAreaName);
        }

        for(CourseAreaDTO courseArea : updatedEvent.venue.getCourseAreas()) {
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
        sailingService.createEvent(newEvent.getName(), newEvent.venue.getName(), newEvent.publicationUrl, newEvent.isPublic, courseAreaNames, new AsyncCallback<EventDTO>() {
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
                eventProvider.getList().clear();
                eventProvider.getList().addAll(result);
            }
        });
    }
}
