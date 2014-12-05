package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

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
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattaSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventAndRegattaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class StructureImportManagementPanel extends FlowPanel {
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
    private List<EventDTO> existingEvents;
    private ListBox sailingEventsListBox;

    public StructureImportManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, RegattaRefresher regattaRefresher, EventManagementPanel eventManagementPanel) {
        this.eventManagementPanel = eventManagementPanel;
        this.busyIndicator = new SimpleBusyIndicator();
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.regattaRefresher = regattaRefresher;
        this.regattaListComposite = new StructureImportListComposite(this.sailingService, new RegattaSelectionModel(
                true), this.regattaRefresher, this.errorReporter, this.stringMessages);
        regattaListComposite.ensureDebugId("RegattaListComposite");
        createUI();
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
                addUrl();
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
        grid.getColumnFormatter().getElement(0/** 1 */
        ).getStyle().setPaddingTop(2.0, Unit.EM);

        editSeriesPanel = new VerticalPanel();

        add(progressPanel);
        add(vp);
        add(editSeriesPanel);
    }

    private void createEventDetails() {
        Grid grid = new Grid(1, 2);
        sailingEventsListBox = new ListBox(false);
        sailingEventsListBox.ensureDebugId("EventListBox");
        grid.setWidget(0, 0, sailingEventsListBox);
        Button newEventBtn = new Button("Create New Event");
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
                        errorReporter.reportError("Error trying to create new event " + newEvent.getName() + ": "
                                + t.getMessage());
                    }

                    @Override
                    public void onSuccess(EventDTO newEvent) {
                        existingEvents.add(newEvent);
                        sailingEventsListBox.addItem(newEvent.getName());
                        sailingEventsListBox.setSelectedIndex(sailingEventsListBox.getItemCount() - 1);
                    }
                });
    }

    private void addUrl() {
        String valueToValidate = "";
        if (eventIDTextBox.getValue() == null || eventIDTextBox.getValue().length() == 0) {
            valueToValidate = jsonURLTextBox.getValue();
        } else {
            valueToValidate = "http://manage2sail.com/api/public/links/event/" + eventIDTextBox.getValue()
                    + "?accesstoken=bDAv8CwsTM94ujZ&mediaType=json";
        }

        if (valueToValidate == null || valueToValidate.length() == 0) {
            errorReporter.reportError(stringMessages.pleaseEnterNonEmptyUrl());
        }

        else {
            busyIndicator.setBusy(true);
            sailingService.getRegattas(valueToValidate, new AsyncCallback<Iterable<RegattaDTO>>() {
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
                    LinkedHashMap<Set<String>, Set<RegattaDTO>> structure = getRegattaStructure(regattas);
                    createEventDetails();
                    showRegattaStructure(structure);
                }
            });
        }
    }

    private LinkedHashMap<Set<String>, Set<RegattaDTO>> getRegattaStructure(Iterable<RegattaDTO> regattas) {
        final LinkedHashMap<Set<String>, Set<RegattaDTO>> structure = new LinkedHashMap<Set<String>, Set<RegattaDTO>>();

        Set<Set<String>> staticStructures = new HashSet<Set<String>>();
        Set<String> OPENINGMEDAL = new HashSet<String>();
        OPENINGMEDAL.add("Opening Series");
        OPENINGMEDAL.add("Medal");
        staticStructures.add(OPENINGMEDAL);
        Set<String> QUALIFICATIONFINALSMEDAL = new HashSet<String>();
        QUALIFICATIONFINALSMEDAL.add("Qualification");
        QUALIFICATIONFINALSMEDAL.add("Finals");
        QUALIFICATIONFINALSMEDAL.add("Medal");
        staticStructures.add(QUALIFICATIONFINALSMEDAL);
        Set<String> QUALIFICATIONFINALS = new HashSet<String>();
        QUALIFICATIONFINALS.add("Qualification");
        QUALIFICATIONFINALS.add("Finals");
        staticStructures.add(QUALIFICATIONFINALS);
        Set<String> ONESERIES = new HashSet<String>();
        ONESERIES.add("Default");
        staticStructures.add(ONESERIES);
        Set<String> UNDEFINED = new HashSet<String>();
        UNDEFINED.add("Undefined");
        staticStructures.add(UNDEFINED);

        for (Set<String> struct : staticStructures) {
            structure.put(struct, new HashSet<RegattaDTO>());
        }

        for (RegattaDTO regatta : regattas) {
            Set<String> temp = new HashSet<String>();
            for (SeriesDTO series : regatta.series) {
                temp.add(series.getName());
            }
            if (temp.isEmpty()) {
                structure.get(UNDEFINED).add(regatta);
            } else {
                for (Set<String> struct : staticStructures) {
                    if (struct.equals(temp)) {
                        structure.get(struct).add(regatta);
                        break;
                    }
                }
            }
        }
        return structure;
    }

    private void fillRegattas(Iterable<RegattaDTO> regattas) {
        regattaListComposite.fillRegattas((List<RegattaDTO>) regattas);
    }

    private void showRegattaStructure(final LinkedHashMap<Set<String>, Set<RegattaDTO>> structure) {

        Grid grid = new Grid(structure.size(), 2);
        int i = 0;
        for (final Set<String> struct : structure.keySet()) {
            Button editBtn = new Button(stringMessages.editSeries());
            editBtn.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    setDefaultRegatta(structure.get(struct));
                }
            });

            String text = "";
            for (String string : struct) {
                text += string + ", ";
            }
            text += "(" + structure.get(struct).size() + ")";
            grid.setWidget(i, 0, new Label(text));
            grid.setWidget(i, 1, editBtn);
            i++;
        }
        editSeriesPanel.add(grid);
    }

    private void setDefaultRegatta(final Iterable<RegattaDTO> selectedRegattas) {
        List<EventDTO> existingEvents = new ArrayList<EventDTO>();
        EventDTO selectedEvent = getSelectedEvent();
        if (selectedEvent != null) {
            existingEvents.add(getSelectedEvent());
        }
        DefaultRegattaCreateDialog dialog = new DefaultRegattaCreateDialog(existingEvents, selectedRegattas,
                sailingService, errorReporter, stringMessages, new DialogCallback<EventAndRegattaDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(final EventAndRegattaDTO newRegatta) {

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

}
