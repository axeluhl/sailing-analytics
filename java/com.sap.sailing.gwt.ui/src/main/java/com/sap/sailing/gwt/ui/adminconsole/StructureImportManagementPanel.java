package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.gwtbootstrap.client.ui.base.ProgressBarBase.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattaSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class StructureImportManagementPanel extends FlowPanel {

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final RegattaRefresher regattaRefresher;
    private final EventManagementPanel eventManagementPanel;
    private StructureImportListComposite regattaListComposite;
    private RegattaSelectionProvider regattaSelectionProvider;
    private Panel progressPanel;
    private TextBox jsonURLTextBox;
    private TextBox eventIDTextBox;

    private Button listRegattasButton;
    private Button importDetailsButton;

    private StructureImportProgressBar progressBar;
    private Label overallName;
    private int parsed;

    public StructureImportManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, RegattaRefresher regattaRefresher, EventManagementPanel eventManagementPanel) {
        this.eventManagementPanel = eventManagementPanel;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.regattaRefresher = regattaRefresher;
        createUI();

    }

    private void createUI() {
        progressPanel = new FlowPanel();
        Grid URLgrid = new Grid(2, 2);
        Label eventIDLabel = new Label(stringMessages.event() + ":");
        Label jsonURLLabel = new Label(stringMessages.jsonUrl() + ":");
        eventIDTextBox = new TextBox();
        eventIDTextBox.ensureDebugId("eventIDTextBox");
        eventIDTextBox.setVisibleLength(50);
        jsonURLTextBox = new TextBox();
        jsonURLTextBox.ensureDebugId("JsonURLTextBox");
        jsonURLTextBox.setVisibleLength(100);
        listRegattasButton = new Button(this.stringMessages.listRegattas());
        importDetailsButton = new Button(this.stringMessages.importRegatta());
        importDetailsButton.setEnabled(false);
        importDetailsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                List<RegattaDTO> regattas = regattaListComposite.getSelectedRegattas();
                createEventDetails(regattas);
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

        regattaSelectionProvider = new RegattaSelectionModel(true);

        regattaListComposite = new StructureImportListComposite(this.sailingService, this.regattaSelectionProvider,
                this.regattaRefresher, this.errorReporter, this.stringMessages);
        regattaListComposite.ensureDebugId("RegattaListComposite");
        grid.setWidget(0, 0, regattaListComposite);
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().getElement(0/** 1 */
        ).getStyle().setPaddingTop(2.0, Unit.EM);

        add(progressPanel);
        add(vp);
    }

    private List<RegattaDTO> getSelectedRegattas() {
        List<RegattaDTO> regattas = new ArrayList<RegattaDTO>();
        for (RegattaIdentifier selectedRegatta : regattaListComposite.regattaSelectionProvider.getSelectedRegattas()) {
            for (RegattaDTO regattaDTO : regattaListComposite.getAllRegattas()) {
                if (regattaDTO.getRegattaIdentifier().equals(selectedRegatta)) {
                    regattas.add(regattaDTO);
                }
            }
        }
        return regattas;
    }

    private void createEventDetails(List<RegattaDTO> regattas) {
        List<String> regattaNamesTemp = new ArrayList<String>();
        for (RegattaDTO regatta : regattas) {
            regattaNamesTemp.add(regatta.getName());
        }

        final List<String> regattaNames = regattaNamesTemp;
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to get events");
            }

            @Override
            public void onSuccess(List<EventDTO> events) {
                openEventCreateDialog(regattaNames, events);
            }
        });
    }

    private void addUrl() {
        String valueToValidate = "";
        if(eventIDTextBox.getValue()==null||eventIDTextBox.getValue().length()==0){
            valueToValidate = jsonURLTextBox.getValue();
        }else{
            valueToValidate = "http://manage2sail.com/api/public/links/event/"+eventIDTextBox.getValue()+"?accesstoken=bDAv8CwsTM94ujZ&mediaType=json";
        }
        
        if (valueToValidate == null || valueToValidate.length() == 0) {
            errorReporter.reportError(stringMessages.pleaseEnterNonEmptyUrl());
        }

        else {
            sailingService.getRegattas(valueToValidate, new AsyncCallback<List<RegattaDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to load regattas");
                }

                @Override
                public void onSuccess(List<RegattaDTO> regattas) {
                    fillRegattas(regattas);
                    importDetailsButton.setEnabled(true);
                }
            });

        }
    }

    public void fillRegattas(List<RegattaDTO> regattas) {
        regattaListComposite.fillRegattas(regattas);
    }

    private void createProgressBarAndLabel(int amountOfRegattas) {
        overallName = new Label(stringMessages.overallProgress() + ":");
        progressPanel.add(overallName);
        progressBar = new StructureImportProgressBar(amountOfRegattas + 1, Style.ANIMATED);
        progressPanel.add(progressBar);
    }

    private void setProgressBar(final int amountOfRegattas) {
        createProgressBarAndLabel(amountOfRegattas);

        final Timer timer = new Timer() {
            @Override
            public void run() {
                sailingService.getStructureImportOperationProgress(new AsyncCallback<Integer>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorAddingResultImportUrl(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Integer result) {
                        parsed = result;
                        progressBar.setPercent(result);
                    }

                });
                if (parsed >= amountOfRegattas) { /* finished */
                    regattaRefresher.fillRegattas();
                    progressBar.removeFromParent();
                    overallName.removeFromParent();
                    this.cancel();
                }
            }
        };
        timer.scheduleRepeating(2000);
    }

    private void setDefaultRegatta(final List<String> regattaNames, final EventDTO newEvent) {
        Collection<RegattaDTO> existingRegattas = Collections.emptyList();
        List<EventDTO> existingEvents = new ArrayList<EventDTO>();
        existingEvents.add(newEvent);

        DefaultRegattaCreateDialog dialog = new DefaultRegattaCreateDialog(existingRegattas, existingEvents,
                stringMessages, new DialogCallback<RegattaDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(RegattaDTO newRegatta) {
                        createRegattas(regattaNames, newEvent, newRegatta);
                    }
                });
        dialog.ensureDebugId("DefaultRegattaCreateDialog");
        dialog.show();

    }

    private void createRegattas(final List<String> regattaNames, EventDTO newEvent, RegattaDTO defaultRegatta) {
        eventManagementPanel.fillEvents();
        sailingService.createRegattaStructure(regattaNames, newEvent, defaultRegatta, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorAddingResultImportUrl(caught.getMessage()));
            }

            @Override
            public void onSuccess(Void result) {
                setProgressBar(regattaNames.size());
            }
        });
    }

    private void openEventCreateDialog(final List<String> regattaNames, List<EventDTO> events) {
        EventCreateDialog dialog = new EventCreateDialog(Collections.unmodifiableCollection(events), stringMessages,
                new DialogCallback<EventDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(final EventDTO newEvent) {
                        createEventAndRegattaStructure(regattaNames, newEvent);
                    }
                });
        dialog.show();
    }

    private void createEventAndRegattaStructure(final List<String> regattaNames, final EventDTO newEvent) {
        List<String> courseAreaNames = new ArrayList<String>();
        for (CourseAreaDTO courseAreaDTO : newEvent.venue.getCourseAreas()) {
            courseAreaNames.add(courseAreaDTO.getName());
        }
        sailingService.createEvent(newEvent.getName(), newEvent.startDate, newEvent.endDate, newEvent.venue.getName(),
                newEvent.isPublic, courseAreaNames, newEvent.getImageURLs(), newEvent.getVideoURLs(),
                newEvent.getSponsorImageURLs(), newEvent.getLogoImageURL(), newEvent.getOfficialWebsiteURL(),
                new AsyncCallback<EventDTO>() {

                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to create new event " + newEvent.getName() + ": "
                                + t.getMessage());
                    }

                    @Override
                    public void onSuccess(EventDTO newEvent) {
                        setDefaultRegatta(regattaNames, newEvent);
                    }
                });
    }

}
