package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collections;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;

/**
 * Allows administrators to manage data of a sailing event. This is a temporary panel because the managed event
 * is not connected with any regatta and it used only to manage of basic attributes of the an event.
 * @author Frank Mittag (C5163974)
 * 
 */
public class SailingEventManagementPanel extends SimplePanel {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private CellTable<EventDTO> eventTable;
    private SingleSelectionModel<EventDTO> eventSelectionModel;
    private ListDataProvider<EventDTO> eventProvider;

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

        // sailing events table
        TextColumn<EventDTO> eventNameColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                return event.name;
            }
        };

        TextColumn<EventDTO> venueNameColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                return event.venue != null ? event.venue.name : "";
            }
        };

        TextColumn<EventDTO> publicationUrlColumn = new TextColumn<EventDTO>() {
            @Override
            public String getValue(EventDTO event) {
                return event.publicationUrl;
            }
        };

        ImagesBarColumn<EventDTO, RegattaConfigImagesBarCell> eventActionColumn = new ImagesBarColumn<EventDTO, RegattaConfigImagesBarCell>(
                new RegattaConfigImagesBarCell(stringMessages));
        eventActionColumn.setFieldUpdater(new FieldUpdater<EventDTO, String>() {
            @Override
            public void update(int index, EventDTO event, String value) {
                if (RegattaConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveRegatta(event.name))) {
                        removeEvent(event);
                    }
                } else if (RegattaConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    openEditEventDialog(event);
                }
            }
        });
        
        eventTable = new CellTable<EventDTO>(10000, tableRes);
        eventTable.addColumn(eventNameColumn, stringMessages.event());
        eventTable.addColumn(venueNameColumn, stringMessages.venue());
        eventTable.addColumn(publicationUrlColumn, "Publication URL");
        eventTable.addColumn(eventActionColumn, stringMessages.actions());
        
        eventSelectionModel = new SingleSelectionModel<EventDTO>();
        eventSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
            }
        });
        eventTable.setSelectionModel(eventSelectionModel);

        eventProvider = new ListDataProvider<EventDTO>();
        eventProvider.addDataDisplay(eventTable);
        mainPanel.add(eventTable);
        
        fillEvents();
    }
    
    private void removeEvent(final EventDTO event) {
        sailingService.removeEvent(event.name, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to remove event " + event.name + ": " + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
            }
        });
    }

    private void openCreateEventDialog() {
        List<EventDTO> existingEvents = eventProvider.getList();
        EventCreateDialog dialog = new EventCreateDialog(Collections.unmodifiableCollection(existingEvents), stringMessages,
                new AsyncCallback<EventDTO>() {
                    @Override
                    public void onFailure(Throwable t) {
                    }

                    @Override
                    public void onSuccess(EventDTO newEvent) {
                        createNewEvent(newEvent);
                    }
                });
        dialog.show();
    }

    private void openEditEventDialog(EventDTO selectedEvent) {
        List<EventDTO> existingEvents = eventProvider.getList();
        EventEditDialog dialog = new EventEditDialog(selectedEvent, Collections.unmodifiableCollection(existingEvents), stringMessages,
                new AsyncCallback<EventDTO>() {
                    @Override
                    public void onFailure(Throwable t) {
                    }

                    @Override
                    public void onSuccess(EventDTO newEvent) {
                    }
                });
        dialog.show();
    }

    private void createNewEvent(final EventDTO newEvent) {
        sailingService.createEvent(newEvent.name, newEvent.venue.name, new AsyncCallback<EventDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new event" + newEvent.name + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(EventDTO newEvent) {
            }
        });
    }

    private void fillEvents() {
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
