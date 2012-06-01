package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

/**
 * Allows administrators to manage the structure of a complete event. Each event consists of several substructures like
 * regattas, races, series and groups (big fleets divided into racing groups).
 * 
 * @author Frank Mittag (C5163974)
 * 
 */
public class EventStructureManagementPanel extends SimplePanel {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final List<EventDTO> events;
    private final ListBox eventsComboBox;
    private EventDTO selectedEvent;

    private CaptionPanel eventDetailsCaptionPanel;

    private Label eventVenueLabel;
    private CellTable<RegattaDTO> regattaTable;
    private MultiSelectionModel<RegattaDTO> regattaSelectionModel;
    private ListDataProvider<RegattaDTO> regattaProvider;

    private final AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);

    public EventStructureManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;

        events = new ArrayList<EventDTO>();
        selectedEvent = null;

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");

        HorizontalPanel eventsPanel = new HorizontalPanel();
        eventsPanel.setSpacing(5);
        mainPanel.add(eventsPanel);

        Label managedEventsLabel = new Label(stringMessages.events() + ":");
        eventsPanel.add(managedEventsLabel);

        eventsComboBox = new ListBox();
        eventsComboBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                onEventSelectionChanged();
            }
        });
        eventsPanel.add(eventsComboBox);

        Button createEventBtn = new Button(stringMessages.add());
        createEventBtn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                openCreateEventDialog();
            }
        });
        eventsPanel.add(createEventBtn);

        createEventDetailsPanel();
        mainPanel.add(eventDetailsCaptionPanel);
        eventDetailsCaptionPanel.setVisible(false);

        fillEvents();
    }

    private void createEventDetailsPanel() {
        eventDetailsCaptionPanel = new CaptionPanel();
        eventDetailsCaptionPanel.setWidth("95%");

        VerticalPanel eventDetailsPanel = new VerticalPanel();
        eventDetailsPanel.setSpacing(5);
        eventDetailsCaptionPanel.add(eventDetailsPanel);

        eventVenueLabel = new Label("");
        eventDetailsPanel.add(eventVenueLabel);

        // regatta table
        TextColumn<RegattaDTO> regattaNameColumn = new TextColumn<RegattaDTO>() {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.name;
            }
        };

        TextColumn<RegattaDTO> regattaSeriesColumn = new TextColumn<RegattaDTO>() {
            @Override
            public String getValue(RegattaDTO regatta) {
                String result = "";
                boolean first = true;
                for (SeriesDTO serie : regatta.series) {
                    if (!first) {
                        result += "; ";
                    }
                    result += serie.name;
                    first = false;
                }
                return result;
            }
        };

        regattaTable = new CellTable<RegattaDTO>(200, tableRes);
        regattaTable.setWidth("100%");
        regattaTable.addColumn(regattaNameColumn, stringMessages.regattaName());
        regattaTable.addColumn(regattaSeriesColumn, stringMessages.name());

        regattaSelectionModel = new MultiSelectionModel<RegattaDTO>();
        regattaSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<RegattaDTO> selectedRegatta = regattaSelectionModel.getSelectedSet();
            }
        });
        regattaTable.setSelectionModel(regattaSelectionModel);

        regattaProvider = new ListDataProvider<RegattaDTO>();
        regattaProvider.addDataDisplay(regattaTable);
        eventDetailsPanel.add(regattaTable);

        Button addRegattaBtn = new Button("Add regatta");
        eventDetailsPanel.add(addRegattaBtn);
        addRegattaBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCreateRegattaDialog();
            }
        });
    }

    private void onEventSelectionChanged() {
        int selIndex = eventsComboBox.getSelectedIndex();
        String selItemText = eventsComboBox.getItemText(selIndex);

        for (EventDTO eventDTO : events) {
            if (eventDTO.name.equals(selItemText)) {
                selectedEvent = eventDTO;
                eventDetailsCaptionPanel.setVisible(true);
                updateEventDetails();
                break;
            }
        }
    }

    private void openCreateEventDialog() {
        EventCreateDialog dialog = new EventCreateDialog(Collections.unmodifiableCollection(events), stringMessages,
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

    private void openCreateRegattaDialog() {
        RegattaCreateDialog dialog = new RegattaCreateDialog(Collections.unmodifiableCollection(selectedEvent.regattas), stringMessages,
                new AsyncCallback<RegattaDTO>() {
                    @Override
                    public void onFailure(Throwable t) {
                    }

                    @Override
                    public void onSuccess(RegattaDTO newRegatta) {
                        createNewRegatta(newRegatta);
                    }
                });
        dialog.show();
    }

    private void updateEventDetails() {
        if (selectedEvent != null) {
            eventDetailsCaptionPanel.setCaptionText(selectedEvent.name);
            eventVenueLabel.setText(stringMessages.venue() + ": " + selectedEvent.venue.name);
        }
    }

    private void createNewEvent(final EventDTO newEvent) {
        sailingService.createEvent(newEvent.name, newEvent.venue.name, new AsyncCallback<EventDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new event" + newEvent.name + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(EventDTO newEvent) {
                events.add(newEvent);
                eventsComboBox.addItem(newEvent.name);
                int index = getEventItemIndex(newEvent.name);
                eventsComboBox.setSelectedIndex(index);
            }
        });
    }

    private void createNewRegatta(final RegattaDTO newRegatta) {
//        sailingService.createRegatta(newRegatta.name, newRegatta.boatClass.name, new AsyncCallback<RegattaDTO>() {
//            @Override
//            public void onFailure(Throwable t) {
//                errorReporter.reportError("Error trying to create new regatta" + newRegatta.name + ": " + t.getMessage());
//            }
//
//            @Override
//            public void onSuccess(RegattaDTO newRegatta) {
//            }
//        });
    }

    private int getEventItemIndex(String eventName) {
        for (int i = 0; i < eventsComboBox.getItemCount(); i++) {
            if (eventName.equals(eventsComboBox.getItemText(i))) {
                return i;
            }
        }
        return -1;
    }

    private void fillEvents() {
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getEvents() - Failure: " + caught.getMessage());
            }

            @Override
            public void onSuccess(List<EventDTO> result) {
                events.clear();
                events.addAll(result);
                eventsComboBox.clear();
                eventsComboBox.addItem("Please select an event...");
                eventsComboBox.setSelectedIndex(0);
                for (EventDTO event : result) {
                    eventsComboBox.addItem(event.name);
                }
            }
        });
    }

}
