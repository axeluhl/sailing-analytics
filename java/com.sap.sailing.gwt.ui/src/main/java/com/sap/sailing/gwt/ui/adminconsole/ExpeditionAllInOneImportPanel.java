package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.ExpeditionAllInOneConstants;
import com.sap.sailing.gwt.common.client.suggestion.BoatClassMasterdataSuggestOracle;
import com.sap.sailing.gwt.common.client.suggestion.RegattaSuggestOracle;
import com.sap.sailing.gwt.ui.adminconsole.resulthandling.ExpeditionDataImportResponse;
import com.sap.sailing.gwt.ui.adminconsole.resulthandling.ExpeditionDataImportResultsDialog;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;

/**
 * The UI form to upload data for expedition all in one import.
 */
public class ExpeditionAllInOneImportPanel extends Composite implements RegattasDisplayer {
    private static final String URL_SAILINGSERVER_EXPEDITION_FULL_IMPORT = "/../../sailingserver/expedition/import";

    private final RegattaSuggestOracle regattaOracle;

    public ExpeditionAllInOneImportPanel(final StringMessages stringMessages, final SailingServiceAsync sailingService,
            final ErrorReporter errorReporter, final RegattaRefresher regattaRefresher) {
        final FormPanel formPanel = new FormPanel();
        final BusyIndicator busyIndicator = new SimpleBusyIndicator();
        final Button uploadButton = new Button(stringMessages.upload());
        formPanel.getElement().setAttribute("autocomplete", "off");
        uploadButton.addClickHandler(event -> {
            uploadButton.setEnabled(false);
            busyIndicator.setBusy(true);
            formPanel.submit();
        });
        formPanel.setMethod(FormPanel.METHOD_POST);
        formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
        formPanel.setAction(GWT.getHostPageBaseURL() + URL_SAILINGSERVER_EXPEDITION_FULL_IMPORT);
        final VerticalPanel contentPanel = new VerticalPanel();
        formPanel.add(contentPanel);
        final Hidden localeField = new Hidden(ExpeditionAllInOneConstants.REQUEST_PARAMETER_LOCALE, LocaleInfo.getCurrentLocale().getLocaleName());
        contentPanel.add(localeField);
        final FileUpload fileUpload = new FileUpload();
        fileUpload.setName("upload");
        contentPanel.add(fileUpload);

        final FlowPanel importModePanel = new FlowPanel();
        contentPanel.add(importModePanel);
        final HorizontalPanel regattaNamePanel = new HorizontalPanel();
        regattaNamePanel.setVisible(false);
        final HorizontalPanel boatClassPanel = new HorizontalPanel();
        contentPanel.add(regattaNamePanel);
        final RadioButton newEventImport = new RadioButton(ExpeditionAllInOneConstants.REQUEST_PARAMETER_IMPORT_MODE,
                stringMessages.createNewEvent());
        newEventImport.setFormValue(ExpeditionAllInOneConstants.ImportMode.NEW_EVENT.name());
        newEventImport.setValue(true);
        importModePanel.add(newEventImport);
        newEventImport.addClickHandler(event -> {
            regattaNamePanel.setVisible(false);
            boatClassPanel.setVisible(true);
        });
        final RadioButton newCompetitorImport = new RadioButton(
                ExpeditionAllInOneConstants.REQUEST_PARAMETER_IMPORT_MODE, stringMessages.newExpeditionCompetitor());
        newCompetitorImport.setFormValue(ExpeditionAllInOneConstants.ImportMode.NEW_COMPETITOR.name());
        importModePanel.add(newCompetitorImport);
        newCompetitorImport.addClickHandler(event -> {
            regattaNamePanel.setVisible(true);
            boatClassPanel.setVisible(false);
        });
        final RadioButton newRaceImport = new RadioButton(ExpeditionAllInOneConstants.REQUEST_PARAMETER_IMPORT_MODE,
                stringMessages.newExpeditionRace());
        newRaceImport.setFormValue(ExpeditionAllInOneConstants.ImportMode.NEW_RACE.name());
        importModePanel.add(newRaceImport);
        newRaceImport.addClickHandler(event -> {
            regattaNamePanel.setVisible(true);
            boatClassPanel.setVisible(false);
        });
        final CheckBox importStartData = new CheckBox(stringMessages.importStartData());
        importStartData.setValue(true);
        importStartData.setName(ExpeditionAllInOneConstants.REQUEST_PARAMETER_IMPORT_START_DATA);
        importModePanel.add(importStartData);
        regattaNamePanel.setSpacing(5);
        final Label regattaNameLabel = new Label(stringMessages.regattaName() + ":");
        regattaNamePanel.add(regattaNameLabel);
        regattaNamePanel.setCellVerticalAlignment(regattaNameLabel, HasVerticalAlignment.ALIGN_MIDDLE);
        final TextBox regattaName = new TextBox();
        regattaName.setName(ExpeditionAllInOneConstants.REQUEST_PARAMETER_REGATTA_NAME);

        regattaOracle = new RegattaSuggestOracle();
        final SuggestBox regattaSuggestBox = new SuggestBox(regattaOracle, regattaName);
        regattaSuggestBox.getValueBox().getElement().getStyle().setProperty("minWidth", 30, Unit.EM);
        regattaSuggestBox.getValueBox().getElement().setAttribute("placeholder", stringMessages.startTypingForSuggestions());
        regattaNamePanel.add(regattaSuggestBox);
        regattaNamePanel.setCellVerticalAlignment(regattaSuggestBox, HasVerticalAlignment.ALIGN_MIDDLE);
        boatClassPanel.setSpacing(5);
        contentPanel.add(boatClassPanel);
        final Label boatClassLabel = new Label(stringMessages.boatClass() + ":");
        boatClassPanel.add(boatClassLabel);
        boatClassPanel.setCellVerticalAlignment(boatClassLabel, HasVerticalAlignment.ALIGN_MIDDLE);
        final SuggestBox boatClassInput = new SuggestBox(new BoatClassMasterdataSuggestOracle());
        boatClassInput.getValueBox().getElement().getStyle().setProperty("minWidth", 30, Unit.EM);
        boatClassInput.getValueBox().getElement().setAttribute("placeholder", stringMessages.startTypingForSuggestions());
        boatClassInput.getValueBox().setName(ExpeditionAllInOneConstants.REQUEST_PARAMETER_BOAT_CLASS);
        boatClassPanel.add(boatClassInput);
        boatClassPanel.setCellVerticalAlignment(boatClassInput, HasVerticalAlignment.ALIGN_MIDDLE);
        final HorizontalPanel controlPanel = new HorizontalPanel();
        controlPanel.setSpacing(5);
        controlPanel.add(uploadButton);
        controlPanel.add(busyIndicator);
        contentPanel.add(controlPanel);
        final Runnable validation = () -> {
            final String filename = fileUpload.getFilename(), boatClass = boatClassInput.getValue();
            final String regattaNameValue = regattaSuggestBox.getValue();
            final boolean fileValid = filename != null && !filename.trim().isEmpty();
            final boolean isNewEventImport = Boolean.TRUE.equals(newEventImport.getValue());
            final boolean boatClassValid = boatClass != null && !boatClass.trim().isEmpty();
            final boolean regattaNameValid = regattaNameValue != null && !regattaNameValue.trim().isEmpty();
            uploadButton.setEnabled(fileValid && (isNewEventImport ? boatClassValid : regattaNameValid));
        };
        fileUpload.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validation.run();
            }
        });
        formPanel.addSubmitCompleteHandler(event -> {
            validation.run();
            busyIndicator.setBusy(false);
            final ExpeditionDataImportResponse response = ExpeditionDataImportResponse.parse(event.getResults());
            if (response == null) {
                Notification.notify(StringMessages.INSTANCE.unexpectedErrorDuringFileImport(), NotificationType.ERROR);
            } else if (response.hasEventId()) {
                new ExpeditionAllInOneAfterImportHandler(response.getEventId(), response.getRegattaName(),
                        response.getLeaderboardName(), response.getLeaderboardGroupName(), response.getRaceEntries(),
                        response.getGpsDeviceIds(), response.getSensorDeviceIds(), response.getSensorFixImporterType(),
                        response.getStartTimes(), sailingService,
                        errorReporter, stringMessages);
                regattaRefresher.fillRegattas();
            } else {
                ExpeditionDataImportResultsDialog.showResults(response);
            }
        });
        regattaSuggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            @Override
            public void onSelection(SelectionEvent<Suggestion> event) {
                final String selected = event.getSelectedItem().getReplacementString();
                if (selected != null) {
                    sailingService.getRegattaByName(selected, new AsyncCallback<RegattaDTO>() {
                        @Override
                        public void onSuccess(RegattaDTO result) {
                            validation.run();
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError("Could not determine Regatta " + selected);
                        }
                    });
                }
            }
        });
        regattaSuggestBox.addKeyUpHandler(event -> validation.run());
        fileUpload.addChangeHandler(event -> validation.run());
        boatClassInput.addSelectionHandler(event -> validation.run());
        boatClassInput.addKeyUpHandler(event -> validation.run());
        validation.run();
        initWidget(formPanel);
    }

    @Override
    public void fillRegattas(Iterable<RegattaDTO> regattas) {
        regattaOracle.fillRegattas(regattas);
    }
}
