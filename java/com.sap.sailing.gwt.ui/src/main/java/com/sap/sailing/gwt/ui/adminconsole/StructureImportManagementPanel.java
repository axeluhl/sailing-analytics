package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.ui.adminconsole.StructureImportListComposite.RegattaStructureProvider;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattaSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventAndRegattaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class StructureImportManagementPanel extends FlowPanel implements RegattaStructureProvider {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final RegattaRefresher regattaRefresher;
    private final EventManagementPanel eventManagementPanel;
    private final StructureImportListComposite regattaListComposite;
    private final BusyIndicator busyIndicator;
    private TextBox jsonURLTextBox;
    private TextBox eventIDTextBox;

    private Button listRegattasButton;
    private Button importDetailsButton;
    private VerticalPanel editSeriesPanel;
    private final Grid regattaStructureGrid;
    private List<EventDTO> existingEvents;
    private ListBox sailingEventsListBox;
    
    /**
     * Holds one {@link RegattaDTO} for each distinct regatta structure. The user can work with this panel
     * to adjust the defaults for each structure recognized, working with the table selection to show/hide
     * structures related to regattas in the list. After having set the defaults, pressing the "Import Regatta..."
     * button will apply these defaults during the creation of the regattas selected.
     */
    private final Map<RegattaStructure, RegattaDTO> regattaDefaultsPerStructure;
    
    private final Map<RegattaDTO, RegattaStructure> regattaStructures;
    
    public StructureImportManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, RegattaRefresher regattaRefresher, EventManagementPanel eventManagementPanel) {
        this.regattaDefaultsPerStructure = new HashMap<>();
        this.regattaStructures = new HashMap<>();
        this.eventManagementPanel = eventManagementPanel;
        this.busyIndicator = new SimpleBusyIndicator();
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.regattaRefresher = regattaRefresher;
        this.regattaListComposite = new StructureImportListComposite(this.sailingService, new RegattaSelectionModel(
                true), this.regattaRefresher, this, this.errorReporter, this.stringMessages);
        regattaListComposite.ensureDebugId("RegattaListComposite");
        createUI();
        regattaListComposite.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                updateRegattaStructureGrid();
            }
        });
        regattaStructureGrid = new Grid(0, 2);
    }

    private void createUI() {
        final Panel progressPanel = new FlowPanel();
        progressPanel.add(busyIndicator);
        Grid URLgrid = new Grid(2, 2);
        Label eventIDLabel = new Label(stringMessages.event() + ":");
        Label jsonURLLabel = new Label(stringMessages.jsonUrl() + ":");
        eventIDTextBox = new TextBox();
        eventIDTextBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                jsonURLTextBox.setText("http://manage2sail.com/api/public/links/event/" + eventIDTextBox.getValue()
                        + "?accesstoken=bDAv8CwsTM94ujZ&mediaType=json");
            }
        });
        eventIDTextBox.ensureDebugId("eventIDTextBox");
        eventIDTextBox.setVisibleLength(50);
        jsonURLTextBox = new TextBox();
        jsonURLTextBox.ensureDebugId("JsonURLTextBox");
        jsonURLTextBox.setVisibleLength(100);
        jsonURLTextBox.getElement().setPropertyString("placeholder",
                        "http://manage2sail.com/api/public/links/event/d30883d3-2876-4d7e-af49-891af6cbae1b?accesstoken=bDAv8CwsTM94ujZ&mediaType=json");
        listRegattasButton = new Button(this.stringMessages.listRegattas());
        importDetailsButton = new Button(this.stringMessages.importRegatta());
        importDetailsButton.setEnabled(false);
        importDetailsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getSelectedEvent() != null) {
                    List<RegattaDTO> selectedRegattas = regattaListComposite.getSelectedRegattas();
                    if (!selectedRegattas.isEmpty()) {
                        createRegattas(selectedRegattas, getSelectedEvent());
                    } else {
                        errorReporter.reportError(stringMessages.pleaseSelectAtLeastOneRegatta());
                    }
                } else {
                    errorReporter.reportError(stringMessages.pleaseSelectAnEvent());
                }
            }
        });
        listRegattasButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                listRegattasAndTheirStructures();
            }
        });
        VerticalPanel vp = new VerticalPanel();

        HorizontalPanel buttonPanel = new HorizontalPanel();
        URLgrid.setWidget(0, 0, eventIDLabel);
        URLgrid.setWidget(0, 1, eventIDTextBox);
        URLgrid.setWidget(1, 0, jsonURLLabel);
        URLgrid.setWidget(1, 1, jsonURLTextBox);
        vp.add(URLgrid);
        vp.add(buttonPanel);
        buttonPanel.add(listRegattasButton);
        buttonPanel.add(importDetailsButton);
        Grid grid = new Grid(1, 1);
        vp.add(grid);

        grid.setWidget(0, 0, regattaListComposite);
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().getElement(0/** 1 */).getStyle().setPaddingTop(2.0, Unit.EM);
        editSeriesPanel = new VerticalPanel();
        add(progressPanel);
        add(vp);
        add(editSeriesPanel);
    }

    /**
     * Adds an event selector and a "create new event" button to the {@link #editSeriesPanel} in its first row
     */
    private void createEventSelectionAndCreateEventButtonUI() {
        Grid grid = new Grid(1, 2);
        sailingEventsListBox = new ListBox(false);
        sailingEventsListBox.ensureDebugId("EventListBox");
        grid.setWidget(0, 0, sailingEventsListBox);
        Button newEventBtn = new Button(stringMessages.createNewEvent());
        newEventBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openEventCreateDialog();
            }
        });
        grid.setWidget(0, 1, newEventBtn);
        editSeriesPanel.add(grid);
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorTryingToGetEvents(caught.getMessage()));
            }

            @Override
            public void onSuccess(List<EventDTO> events) {
                existingEvents = events;
                sailingEventsListBox.addItem(stringMessages.selectSailingEvent());
                for (EventDTO event : existingEvents) {
                    sailingEventsListBox.addItem(event.getName());
                }
            }
        });

    }

    private void openEventCreateDialog() {
        EventCreateDialog dialog = new EventCreateDialog(existingEvents, Collections.<LeaderboardGroupDTO>emptyList(), stringMessages,
                new DialogCallback<EventDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(final EventDTO newEvent) {
                        createEvent(newEvent);
                    }
                });
        dialog.show();
    }

    private void createEvent(final EventDTO newEvent) {
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
                        errorReporter.reportError(stringMessages.errorTryingToCreateNewEvent(newEvent.getName(), t.getMessage()));
                    }

                    @Override
                    public void onSuccess(EventDTO newEvent) {
                        existingEvents.add(newEvent);
                        sailingEventsListBox.addItem(newEvent.getName());
                        sailingEventsListBox.setSelectedIndex(sailingEventsListBox.getItemCount() - 1);
                    }
                });
    }

    private void listRegattasAndTheirStructures() {
        final String jsonURL;
        if (eventIDTextBox.getValue() == null || eventIDTextBox.getValue().length() == 0) {
            jsonURL = jsonURLTextBox.getValue();
        } else {
            jsonURL = "http://manage2sail.com/api/public/links/event/" + eventIDTextBox.getValue()
                    + "?accesstoken=bDAv8CwsTM94ujZ&mediaType=json";
        }
        if (jsonURL == null || jsonURL.length() == 0) {
            errorReporter.reportError(stringMessages.pleaseEnterNonEmptyUrl());
        } else {
            busyIndicator.setBusy(true);
            sailingService.getRegattas(jsonURL, new AsyncCallback<Iterable<RegattaDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    busyIndicator.setBusy(false);
                    errorReporter.reportError("Error trying to load regattas");
                }

                @Override
                public void onSuccess(Iterable<RegattaDTO> regattas) {
                    busyIndicator.setBusy(false);
                    editSeriesPanel.clear();
                    fillRegattas(regattas);
                    importDetailsButton.setEnabled(true);
                    createEventSelectionAndCreateEventButtonUI();
                    editSeriesPanel.add(regattaStructureGrid);
                    updateRegattaStructureGrid();
                }
            });
        }
    }

    private void fillRegattas(Iterable<RegattaDTO> regattas) {
        regattaStructures.clear();
        regattaDefaultsPerStructure.clear();
        for (RegattaDTO regatta : regattas) {
            RegattaStructure regattaStructure = new RegattaStructure(regatta);
            if (!regattaDefaultsPerStructure.containsKey(regattaStructure)) {
                RegattaDTO defaultsForRegattasWithStructure = createRegattaDefaults(regatta);
                regattaDefaultsPerStructure.put(regattaStructure, defaultsForRegattasWithStructure);
            }
            regattaStructures.put(regatta, regattaStructure);
        }
        regattaListComposite.fillRegattas((List<RegattaDTO>) regattas);
    }

    /**
     * Creates a new basically empty default regatta structure description object; this can be used as
     * an initial template that a user can modify as needed before instantiating the template for a set
     * of regattas.
     */
    private RegattaDTO createRegattaDefaults(RegattaDTO regatta) {
        RegattaDTO result = new RegattaDTO();
        result.setName("Default");
        result.boatClass = new BoatClassDTO(BoatClassDTO.DEFAULT_NAME, /* hull length in meters */ 5);
        result.series = regatta.series;
        return result;
    }

    /**
     * Updates the grid holding the regatta structure descriptions with the corresponding "edit" buttons
     */
    private void updateRegattaStructureGrid() {
        while (regattaStructureGrid.getRowCount() > 0) {
            regattaStructureGrid.removeRow(0);
        }
        int i = 0;
        for (final Entry<RegattaStructure, RegattaDTO> e : regattaDefaultsPerStructure.entrySet()) {
            Button editBtn = new Button(stringMessages.editSeries());
            editBtn.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    editRegattaDefaults(e.getValue());
                }
            });
            regattaStructureGrid.setWidget(i, 0, new Label(e.getKey().toString()));
            regattaStructureGrid.setWidget(i, 1, editBtn);
            i++;
        }
    }

    private void editRegattaDefaults(final RegattaDTO selectedRegatta) {
        List<EventDTO> existingEvents = new ArrayList<EventDTO>();
        EventDTO selectedEvent = getSelectedEvent();
        if (selectedEvent != null) {
            existingEvents.add(getSelectedEvent());
        }
        DefaultRegattaCreateDialog dialog = new DefaultRegattaCreateDialog(existingEvents, selectedRegatta,
                sailingService, errorReporter, stringMessages, new DialogCallback<EventAndRegattaDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(final EventAndRegattaDTO newRegatta) {
                        // FIXME why isn't the association between regatta and event not evaluated here? Where *is* it evaluated?
                    }
                });
        dialog.ensureDebugId("DefaultRegattaCreateDialog");
        dialog.show();
    }

    private EventDTO getSelectedEvent() {
        EventDTO result = null;
        int selIndex = sailingEventsListBox.getSelectedIndex();
        if (selIndex > 0) { // the zero index represents the 'no selection' text
            String itemText = sailingEventsListBox.getItemText(selIndex);
            for (EventDTO eventDTO : existingEvents) {
                if (eventDTO.getName().equals(itemText)) {
                    result = eventDTO;
                    break;
                }
            }
        }
        return result;
    }

    private void createRegattas(final Iterable<RegattaDTO> selectedRegattas, EventDTO newEvent) {
        eventManagementPanel.fillEvents();
        sailingService.createRegattaStructure(selectedRegattas, newEvent, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorAddingResultImportUrl(caught.getMessage()));
            }

            @Override
            public void onSuccess(Void result) {
                regattaRefresher.fillRegattas();
            }
        });
    }

    @Override
    public RegattaStructure getRegattaStructure(RegattaDTO regatta) {
        return regattaStructures.get(regatta);
    }

}
