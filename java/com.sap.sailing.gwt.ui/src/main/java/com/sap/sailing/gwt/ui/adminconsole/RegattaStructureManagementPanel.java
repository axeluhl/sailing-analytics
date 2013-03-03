package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattaSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RegattaSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

/**
 * Allows administrators to manage the structure of a complete event. Each event consists of several substructures like
 * regattas, races, series and groups (big fleets divided into racing groups).
 * 
 * @author Frank Mittag (C5163974)
 * 
 */
public class RegattaStructureManagementPanel extends SimplePanel implements RegattaDisplayer, RegattaSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final List<EventDTO> events;
    private ListBox eventsComboBox;
    private EventDTO selectedEvent;
    private CaptionPanel eventDetailsCaptionPanel;
    private Label eventVenueLabel;

    private final RegattaRefresher regattaRefresher;
    private RegattaSelectionProvider regattaSelectionProvider;

    private RegattaListComposite regattaListComposite;
    private RegattaDetailsComposite regattaDetailsComposite;

    private boolean supportEvents = false;
    
    public RegattaStructureManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, RegattaRefresher regattaRefresher) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;

        events = new ArrayList<EventDTO>();
        selectedEvent = null;

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);
        mainPanel.setWidth("100%");

        if(supportEvents) {
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
        } else {
            createEventDetailsPanel();
            mainPanel.add(eventDetailsCaptionPanel);
        }
    }

    private void createEventDetailsPanel() {
        eventDetailsCaptionPanel = new CaptionPanel();
        eventDetailsCaptionPanel.setWidth("95%");

        VerticalPanel eventDetailsPanel = new VerticalPanel();
        eventDetailsPanel.setSpacing(5);
        eventDetailsCaptionPanel.add(eventDetailsPanel);

        eventVenueLabel = new Label("");
        eventDetailsPanel.add(eventVenueLabel);

        createRegattaDetails(eventDetailsPanel);
    }

    private void createRegattaDetails(Panel parentPanel) {
        Button addRegattaBtn = new Button(stringMessages.addRegatta());
        parentPanel.add(addRegattaBtn);
        addRegattaBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCreateRegattaDialog();
            }
        });

        Grid grid = new Grid(1 ,2);
        parentPanel.add(grid);
        
        regattaSelectionProvider = new RegattaSelectionModel(false);
        regattaSelectionProvider.addRegattaSelectionChangeListener(this);
        
        regattaListComposite = new RegattaListComposite(sailingService, regattaSelectionProvider, regattaRefresher, errorReporter, stringMessages);
        grid.setWidget(0, 0, regattaListComposite);
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().getElement(1).getStyle().setPaddingTop(2.0, Unit.EM);
        regattaDetailsComposite = new RegattaDetailsComposite(sailingService, regattaRefresher, errorReporter, stringMessages);
        regattaDetailsComposite.setVisible(false);
        grid.setWidget(0, 1, regattaDetailsComposite);
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

    private void openCreateRegattaDialog() {
        Collection<RegattaDTO> existingRegattas = null;
        if(supportEvents) {
            existingRegattas = Collections.unmodifiableCollection(selectedEvent.regattas);
        } else {
            existingRegattas = Collections.unmodifiableCollection(regattaListComposite.getAllRegattas());
        }
        
        RegattaWithSeriesAndFleetsCreateDialog dialog = new RegattaWithSeriesAndFleetsCreateDialog(existingRegattas, stringMessages,
                new DialogCallback<RegattaDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(RegattaDTO newRegatta) {
                        createNewRegatta(newRegatta);
                    }
                });
        dialog.show();
    }

    private void updateEventDetails() {
        if (selectedEvent != null) {
            eventDetailsCaptionPanel.setCaptionText(selectedEvent.name);
            eventVenueLabel.setText(stringMessages.venue() + ": " + selectedEvent.venue.name);
            
            // load the regattas for this event
            sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
                @Override
                public void onFailure(Throwable t) {
                    errorReporter.reportError("Error trying to read regattas of event " + selectedEvent.name + ": " + t.getMessage());
                }

                @Override
                public void onSuccess(List<RegattaDTO> regattas) {
                    regattaListComposite.fillRegattas(regattas);
                }
            });
        }
    }

    private void createNewEvent(final EventDTO newEvent) {
        sailingService.createEvent(newEvent.name, newEvent.venue.name, newEvent.publicationUrl, newEvent.isPublic, new AsyncCallback<EventDTO>() {
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
        LinkedHashMap<String, Pair<List<Triple<String, Integer, Color>>, Boolean>> seriesStructure =
                new LinkedHashMap<String, Pair<List<Triple<String, Integer, Color>>, Boolean>>();
        for (SeriesDTO seriesDTO : newRegatta.series) {
            List<Triple<String, Integer, Color>> fleets = new ArrayList<Triple<String, Integer, Color>>();
            for(FleetDTO fleetDTO : seriesDTO.getFleets()) {
                Triple<String, Integer, Color> fleetTriple = new Triple<String, Integer, Color>(fleetDTO.name, fleetDTO.getOrderNo(), fleetDTO.getColor());
                fleets.add(fleetTriple);
            }
            Pair<List<Triple<String, Integer, Color>>, Boolean> seriesPair = new Pair<List<Triple<String, Integer, Color>>, Boolean>(fleets, seriesDTO.isMedal());
            seriesStructure.put(seriesDTO.name, seriesPair);
        }
        sailingService.createRegatta(newRegatta.name, newRegatta.boatClass.name, seriesStructure, true,
                newRegatta.scoringScheme, new AsyncCallback<RegattaDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new regatta" + newRegatta.name + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(RegattaDTO regatta) {
                regattaRefresher.fillRegattas();
            }
        });
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

    @Override
    public void fillRegattas(List<RegattaDTO> regattas) {
        regattaListComposite.fillRegattas(regattas);
    }

    @Override
    public void onRegattaSelectionChange(List<RegattaIdentifier> selectedRegattas) {
        RegattaIdentifier selectedRegatta = selectedRegattas.iterator().next();
        if(selectedRegatta != null && regattaListComposite.getAllRegattas() != null) {
            for(RegattaDTO regattaDTO: regattaListComposite.getAllRegattas()) {
                if(regattaDTO.getRegattaIdentifier().equals(selectedRegatta)) {
                    regattaDetailsComposite.setRegatta(regattaDTO);
                    regattaDetailsComposite.setVisible(true);
                    break;
                }
            }
        } else {
            regattaDetailsComposite.setRegatta(null);
            regattaDetailsComposite.setVisible(false);
        }
    }
}
