package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sailing.domain.common.security.SecuredDomainType.COMPETITOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.ORCCertificateUploadConstants;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;

/**
 * This panel houses the functionality to manage the {@link ORCCertificate} linking to the corresponding {@link Boat}.
 * Additionally the BoatTable shows some basic information about the participating Boats and the CertificateTable displays
 * the GPH, Issue Date and identification information. To upload or import some certificates, there is an UploadForm.
 * 
 * @author Daniel Lisunkin (i505543)
 *
 */
public class BoatCertificatesPanel extends SimplePanel {
    private final BoatWithCertificateTableWrapper<RefreshableSingleSelectionModel<BoatDTO>> boatTable;
    private final CertificatesTableWrapper<RefreshableSingleSelectionModel<ORCCertificate>> certificateTable;
    private final BusyIndicator busyIndicator;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final RegattaName regattaIdentifier;
    
    private final StringListEditorComposite urls;
    
    /**
     * The hidden form fields that transmit the {@link ORCCertificateUploadConstants#CERTIFICATE_URLS} parameters
     * with the form submission. The contents are kept in sync with {@link #urls} by a value change handler on
     * {@link #urls}.
     */
    private final List<Hidden> hiddenCertificateUrlsFields;
    
    /**
     * Holds the assignments of certificates made available in the {@link #certificateTable} to boats made available
     * in the {@link #boatTable}.
     */
    private final Map<BoatDTO, ORCCertificate> certificateAssignments;
    
    /**
     * The HTML form used to submit files to be uploaded, the additional certificate URLs, as well as the
     * assignments described in the hidden field {@link #certificateAssignmentsAsJSON}.
     */
    private final FormPanel form;
    
    private final VerticalPanel mainPanel;
    
    private final Hidden certificateAssignmentsAsJSON;
    
    private final StringMessages stringMessages;
    
    public BoatCertificatesPanel(final SailingServiceAsync sailingService, final UserService userService, final String regattaName,
            final StringMessages stringMessages, final ErrorReporter errorReporter) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        certificateAssignments = new HashMap<>();
        this.regattaIdentifier = new RegattaName(regattaName);
        this.urls = new StringListEditorComposite(Collections.emptySet(), stringMessages, stringMessages.courseAreas(), IconResources.INSTANCE.removeIcon(),
                /* suggested values */ Collections.emptySet(), stringMessages.enterURLsForCertificateDownload());
        this.hiddenCertificateUrlsFields = new ArrayList<>();
        this.boatTable = new BoatWithCertificateTableWrapper<>(sailingService, userService, stringMessages,
                errorReporter, /* multiSelection */ false, /* enablePager */ true, TableWrapper.DEFAULT_PAGING_SIZE, /* allow actions */ true,
                boat->unlink(boat), boat->certificateAssignments.containsKey(boat));
        this.certificateTable = new CertificatesTableWrapper<>(sailingService, userService, stringMessages,
                errorReporter, /* multiSelection */ false, /* enablePager */ true, TableWrapper.DEFAULT_PAGING_SIZE);
        busyIndicator = new SimpleBusyIndicator(false, 0.8f);
        this.form = new FormPanel();
        certificateAssignmentsAsJSON = new Hidden(ORCCertificateUploadConstants.CERTIFICATE_SELECTION);
        updateCertificateAssignmentsJSON();
        form.setAction(Window.Location.createUrlBuilder().setPath("/sailingserver/orc-certificate-import").buildString());
        form.setMethod(FormPanel.METHOD_POST);
        form.add(certificateAssignmentsAsJSON);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.addSubmitHandler(e->busyIndicator.setBusy(true));
        form.addSubmitCompleteHandler(e->formSubmitComplete(e));
        mainPanel = new VerticalPanel();
        mainPanel.add(certificateAssignmentsAsJSON);
        mainPanel.add(new Hidden(ORCCertificateUploadConstants.REGATTA, regattaName)); // TODO factor so this can also be used for RaceLog
        form.setWidget(mainPanel);
        this.setWidget(form);
        mainPanel.add(busyIndicator);
        Grid tablesPanel = new Grid(1,2);
        tablesPanel.setWidth("100%");
        final AccessControlledButtonPanel topButtonPanel = new AccessControlledButtonPanel(userService, COMPETITOR);
        mainPanel.add(topButtonPanel);
        mainPanel.add(urls);
        mainPanel.add(tablesPanel);
        // BUTTON - Refresh
        final Button refreshButton = topButtonPanel.addUnsecuredAction(stringMessages.refresh(), this::refreshBoatList);
        refreshButton.ensureDebugId("RefreshButton");
        // BUTTON - Import Certificates
        final Button importCertificatesButton = topButtonPanel.addCreateAction(stringMessages.importCertificates(), this::importCertificates);
        importCertificatesButton.ensureDebugId("ImportCertificatesButton");
        // TODO Add functionality to button and implement Form
        // TABLE - Boats
        CaptionPanel boatCaptionPanel = new CaptionPanel("Boats");
        boatCaptionPanel.add(boatTable);
        tablesPanel.setWidget(0, 0, boatCaptionPanel);
        // TABLE - Certificates
        CaptionPanel certificatesCaptionPanel = new CaptionPanel("Certificates");
        certificatesCaptionPanel.add(certificateTable);
        wireSelectionModels();
        tablesPanel.setWidget(0, 1, certificatesCaptionPanel);
        if (regattaName != null) {
            refreshBoatList();
        }
    }

    private void updateCertificateAssignmentsJSON() {
        final JSONObject certificateAssignmentsJson = new JSONObject();
        final JSONArray assignmentsArray = new JSONArray();
        for (final Entry<BoatDTO, ORCCertificate> e : certificateAssignments.entrySet()) {
            final JSONObject assignmentJson = new JSONObject();
            assignmentJson.put(ORCCertificateUploadConstants.BOAT_ID, new JSONString(e.getKey().getIdAsString()));
            assignmentJson.put(ORCCertificateUploadConstants.CERTIFICATE_ID, new JSONString(e.getValue().getId()));
        }
        certificateAssignmentsJson.put(ORCCertificateUploadConstants.CERTIFICATE_SELECTION, assignmentsArray);
        certificateAssignmentsAsJSON.setValue(certificateAssignmentsJson.toString());
    }

    private void formSubmitComplete(SubmitCompleteEvent e) {
        Notification.notify("Servlet response: "+e.getResults(), NotificationType.INFO);
        busyIndicator.setBusy(false);
        // TODO Implement BoatCertificatesPanel.formSubmitComplete(...)
    }

    public void unlink(BoatDTO boat) {
        final ORCCertificate associatedCertificate = certificateAssignments.remove(boat);
        if (associatedCertificate != null && certificateTable.getSelectionModel().isSelected(associatedCertificate)) {
            certificateTable.getSelectionModel().setSelected(associatedCertificate, false);
        }
        refreshInTable(boat);
    }

    private void refreshInTable(BoatDTO boat) {
        final List<BoatDTO> boatList = boatTable.getDataProvider().getList();
        boatList.set(boatList.indexOf(boat), boat); // cause the table to refresh the boat item regarding linkedness state
    }
    
    private void wireSelectionModels() {
        boatTable.getSelectionModel().addSelectionChangeHandler(e->{
            final ORCCertificate assignedCertificate = certificateAssignments.get(boatTable.getSelectionModel().getSelectedObject());
            if (assignedCertificate == null) {
                final ORCCertificate selectedCertificate = certificateTable.getSelectionModel().getSelectedObject();
                if (selectedCertificate != null) {
                    certificateTable.getSelectionModel().setSelected(selectedCertificate, false);
                }
            } else {
                certificateTable.getSelectionModel().setSelected(assignedCertificate, true);
            }
        });
        certificateTable.getSelectionModel().addSelectionChangeHandler(e->{
            final BoatDTO boat = boatTable.getSelectionModel().getSelectedObject();
            final ORCCertificate selectedCertificate = certificateTable.getSelectionModel().getSelectedObject();
            if (boat != null) {
                final ORCCertificate assignedCertificate = certificateAssignments.get(boat);
                if (selectedCertificate != null) {
                    if (assignedCertificate == null || Window.confirm(stringMessages.reallyChangeAssignedCertificateForBoat(boat.toString()))) {
                        assign(boat, selectedCertificate);
                    }
                } else {
                    if (assignedCertificate != null) {
                        unlink(boat);
                    }
                }
            }
        });
        urls.addValueChangeHandler(valueChangeEvent->{
            final Iterable<String> certificateUrls = valueChangeEvent.getValue();
            updateHiddenCertificateUrlsField(certificateUrls);
        });
        // TODO Implement BoatCertificatesPanel.wireSelectionModels(...)
    }

    private void assign(BoatDTO boat, ORCCertificate selectedCertificate) {
        certificateAssignments.put(boat, selectedCertificate);
        refreshInTable(boat);
        updateCertificateAssignmentsJSON();
    }

    private void updateHiddenCertificateUrlsField(Iterable<String> certificateUrls) {
        for (final Hidden existingHiddenCertificateUrlField : hiddenCertificateUrlsFields) {
            mainPanel.remove(existingHiddenCertificateUrlField);
        }
        hiddenCertificateUrlsFields.clear();
        for (final String certificateUrl : certificateUrls) {
            final Hidden hiddenCertificateUrlField = new Hidden(ORCCertificateUploadConstants.CERTIFICATE_URLS, certificateUrl);
            mainPanel.add(hiddenCertificateUrlField);
            hiddenCertificateUrlsFields.add(hiddenCertificateUrlField);
        }
    }

    private void importCertificates() {
        form.submit();
    }

    private void refreshBoatList() {
        sailingService.getBoatRegistrationsForRegatta(regattaIdentifier, new AsyncCallback<Collection<BoatDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Remote Procedure Call getBoats() - Failure: " + caught.getMessage());
            }

            @Override
            public void onSuccess(Collection<BoatDTO> result) {
                boatTable.setBoats(result);
            }
        });
    }
}
