package com.sap.sailing.gwt.ui.adminconsole;

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
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattaSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventAndRegattaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class StructureImportManagementPanel extends FlowPanel {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final RegattaRefresher regattaRefresher;
    private final EventManagementPanel eventManagementPanel;
    private final StructureImportListComposite regattaListComposite;
    private RegattaSelectionProvider regattaSelectionProvider;
    private Panel progressPanel;
    private TextBox jsonURLTextBox;
    private TextBox eventIDTextBox;
    private Iterable<RegattaDTO> addSpecificRegattas;

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
        this.regattaListComposite = new StructureImportListComposite(this.sailingService,
                new RegattaSelectionModel(true), this.regattaRefresher, this.errorReporter, this.stringMessages);
        regattaListComposite.ensureDebugId("RegattaListComposite");
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
                List<RegattaDTO> selectedRegattas = regattaListComposite.getSelectedRegattas();
                createEventDetails(selectedRegattas);
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

        grid.setWidget(0, 0, regattaListComposite);
        grid.getRowFormatter().setVerticalAlign(0, HasVerticalAlignment.ALIGN_TOP);
        grid.getColumnFormatter().getElement(0/** 1 */
        ).getStyle().setPaddingTop(2.0, Unit.EM);

        add(progressPanel);
        add(vp);
    }

    private void createEventDetails(final Iterable<RegattaDTO> regattas) {
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to get events");
            }
            @Override
            public void onSuccess(List<EventDTO> events) {
                setDefaultRegatta(regattas, events);
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
            sailingService.getRegattas(valueToValidate, new AsyncCallback<Iterable<RegattaDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to load regattas");
                }
                @Override
                public void onSuccess(Iterable<RegattaDTO> regattas) {
                    addSpecificRegattas = regattas;
                    fillRegattas(regattas);
                    importDetailsButton.setEnabled(true);
                }
            });
        }
    }

    private void fillRegattas(Iterable<RegattaDTO> regattas) {
        regattaListComposite.fillRegattas((List<RegattaDTO>)regattas);
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

    private void setDefaultRegatta(final Iterable<RegattaDTO> selectedRegattas, final List<EventDTO> newEvent) {
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorAddingResultImportUrl(caught.getMessage()));
            }

            @Override
            public void onSuccess(List<EventDTO> result) {
                Collection<RegattaDTO> existingRegattas = Collections.emptyList();
                DefaultRegattaCreateDialog dialog = new DefaultRegattaCreateDialog(existingRegattas, result,
                        sailingService, errorReporter, stringMessages, new DialogCallback<EventAndRegattaDTO>() {
                            @Override
                            public void cancel() {
                            }
                            @Override
                            public void ok(final EventAndRegattaDTO newRegatta) {
//                                sailingService.getRegattaStructure(selectedRegattas,
//                                        new AsyncCallback<Set<RegattaStructureDTO>>() {
//                                            @Override
//                                            public void onFailure(Throwable caught) {
//                                                // errorReporter.reportError(stringMessages.errorAddingResultImportUrl(caught
//                                                // .getMessage()));
//                                            }
//
//                                            @Override
//                                            public void onSuccess(Set<RegattaStructureDTO> result) {
//                                                // TODO UI aufbauen
                                                createRegattas(selectedRegattas, newRegatta.getEvent(),
                                                        newRegatta.getRegatta());
//                                            }
//                                        });

                            }
                        });
                dialog.ensureDebugId("DefaultRegattaCreateDialog");
                dialog.show();

            }
        });

    }

    private void createRegattas(final Iterable<RegattaDTO> regattaNames, EventDTO newEvent, RegattaDTO defaultRegatta) {
        eventManagementPanel.fillEvents();
        sailingService.createRegattaStructure(regattaNames, newEvent, defaultRegatta, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorAddingResultImportUrl(caught.getMessage()));
            }

            @Override
            public void onSuccess(Void result) {
//                setProgressBar(regattaNames.size());
            }
        });
    }

}
