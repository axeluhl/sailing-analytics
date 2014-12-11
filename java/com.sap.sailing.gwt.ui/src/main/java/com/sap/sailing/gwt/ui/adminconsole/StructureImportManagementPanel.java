package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
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
    private final FlexTable regattaStructureGrid;
    private List<EventDTO> existingEvents;
    private ListBox sailingEventsListBox;
    
    /**
     * Holds one {@link RegattaDTO} for each distinct regatta structure. The user can work with this panel
     * to adjust the defaults for each structure recognized, working with the table selection to show/hide
     * structures related to regattas in the list. After having set the defaults, pressing the "Import Regatta..."
     * button will apply these defaults during the creation of the regattas selected. When modifying the
     * defaults such that a different {@link RegattaStructure} will be needed to describe the modified version,
     * the old entry shall be removed and a new one with the {@link RegattaStructure} matching the modified
     * {@link RegattaDTO} shall be inserted.
     */
    private final Map<RegattaStructure, RegattaDTO> regattaDefaultsPerStructure;
    
    /**
     * Maps from the {@link RegattaDTO}s coming from the XRR import to the structure to be used when creating
     * the regatta in the back-end. When a regatta is first added to this map, the {@link RegattaStructure} object
     * is originally created using the key as construction parameter. Over time, the user may evolve the structure
     * to be used for import using the editing UIs, leading to a different {@link RegattaStructure} to be associated
     * where the key is no longer part of the equivalence class defined by the {@link RegattaStructure} value.
     * A typical case is a modification in the fleet structure, e.g., because the fleets weren't correctly recognized
     * during the import and required some manual intervention.<p>
     * 
     * The default settings to be used when finally creating the regatta in the back-end are maintained
     * in {@link #regattaDefaultsPerStructure} for which the values of this map can be used as key.
     */
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
        regattaStructureGrid = new FlexTable();
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
                    List<RegattaDTO> selectedOriginalRegattasFromXRR = regattaListComposite.getSelectedRegattas();
                    if (!selectedOriginalRegattasFromXRR.isEmpty()) {
                        createRegattas(selectedOriginalRegattasFromXRR, getSelectedEvent());
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

    private void fillRegattas(Iterable<RegattaDTO> regattasFromXRR) {
        regattaStructures.clear();
        regattaDefaultsPerStructure.clear();
        for (RegattaDTO regattaFromXRR : regattasFromXRR) {
            RegattaStructure regattaStructure = new RegattaStructure(regattaFromXRR);
            if (!regattaDefaultsPerStructure.containsKey(regattaStructure)) {
                RegattaDTO defaultsForRegattasWithStructure = createRegattaDefaults(regattaFromXRR);
                regattaDefaultsPerStructure.put(regattaStructure, defaultsForRegattasWithStructure);
            }
            regattaStructures.put(regattaFromXRR, regattaStructure);
        }
        regattaListComposite.fillRegattas((List<RegattaDTO>) regattasFromXRR);
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
        final List<RegattaDTO> originalXRRImportedRegattasToConsider;
        if (regattaListComposite.getSelectedRegattas().isEmpty()) {
            originalXRRImportedRegattasToConsider = regattaListComposite.getAllRegattas();
        } else {
            originalXRRImportedRegattasToConsider = regattaListComposite.getSelectedRegattas();
        }
        final Set<RegattaStructure> structures = new HashSet<>();
        for (RegattaDTO originalXRRImportedRegatta : originalXRRImportedRegattasToConsider) {
            structures.add(regattaStructures.get(originalXRRImportedRegatta));
        }
        int i = 0;
        for (RegattaStructure regattaStructure : structures) {
            final int row = i;
            updateRegattaStructureGridRow(row, regattaStructure);
            i++;
        }
    }

    /**
     * Updates a single row in the {@link #regattaStructureGrid}
     */
    private void updateRegattaStructureGridRow(final int row, final RegattaStructure regattaStructure) {
        Button editBtn = new Button(stringMessages.editSeries());
        editBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                editRegattaDefaults(row, regattaStructure);
            }
        });
        regattaStructureGrid.setWidget(row, 0, new Label(regattaStructure.toString()));
        regattaStructureGrid.setWidget(row, 1, editBtn);
    }

    private void editRegattaDefaults(final int row, final RegattaStructure regattaStructure) {
        List<EventDTO> existingEvents = new ArrayList<EventDTO>();
        EventDTO selectedEvent = getSelectedEvent();
        if (selectedEvent != null) {
            existingEvents.add(getSelectedEvent());
        }
        DefaultRegattaCreateDialog dialog = new DefaultRegattaCreateDialog(existingEvents, regattaDefaultsPerStructure.get(regattaStructure),
                sailingService, errorReporter, stringMessages, new DialogCallback<EventAndRegattaDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(final EventAndRegattaDTO newRegattaWithEvent) {
                        final RegattaDTO newRegattaCreationDefaults = newRegattaWithEvent.getRegatta();
                        RegattaStructure newStructureEquivalenceClass = new RegattaStructure(newRegattaCreationDefaults);
                        final RegattaDTO replaced = regattaDefaultsPerStructure.put(newStructureEquivalenceClass, newRegattaCreationDefaults);
                        if (!regattaStructure.equals(newStructureEquivalenceClass)) {
                            // the creation defaults (probably particularly the fleet structure) was changed "incompatibly";
                            // if it equals another existing structure, use the creation defaults just edited for both and
                            // remove the grid line; otherwise replace the grid line;
                            // in any case update the StructureImportListComposite to reflect the changed structure
                            Map<RegattaDTO, RegattaStructure> updatesToPerform = new HashMap<>();
                            for (Entry<RegattaDTO, RegattaStructure> e : regattaStructures.entrySet()) {
                                if (e.getValue().equals(regattaStructure)) {
                                    // found a regatta that referenced the old structure which is now obsolete; let the XRR-imported
                                    // regatta point to the new structure
                                    updatesToPerform.put(e.getKey(), newStructureEquivalenceClass);
                                }
                            }
                            regattaStructures.putAll(updatesToPerform); // let original XRR-imported regattas point to their new structure
                            regattaDefaultsPerStructure.remove(regattaStructure);
                            regattaListComposite.fillRegattas(regattaStructures.keySet());
                            if (replaced != null) {
                                // requires a re-build of the grid because we don't know the other row to update/remove
                                updateRegattaStructureGrid();
                            } else {
                                updateRegattaStructureGridRow(row, newStructureEquivalenceClass);
                            }
                        }
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

    private void createRegattas(final Iterable<RegattaDTO> selectedOriginalRegattasFromXRR, EventDTO newEvent) {
        eventManagementPanel.fillEvents();
        final Set<RegattaDTO> regattaConfigurationsToCreate = new HashSet<>();
        for (RegattaDTO originalRegattaFromXRR : selectedOriginalRegattasFromXRR) {
            RegattaDTO cloneFromDefaults = new RegattaDTO(regattaDefaultsPerStructure.get(regattaStructures.get(originalRegattaFromXRR)));
            cloneFromDefaults.setName(originalRegattaFromXRR.getName());
            cloneFromDefaults.boatClass = originalRegattaFromXRR.boatClass;
            regattaConfigurationsToCreate.add(cloneFromDefaults);
        }
        sailingService.createRegattaStructure(regattaConfigurationsToCreate, newEvent, new AsyncCallback<Void>() {
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
