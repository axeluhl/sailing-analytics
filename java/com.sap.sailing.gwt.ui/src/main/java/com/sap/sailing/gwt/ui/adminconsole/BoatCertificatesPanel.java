package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sailing.domain.common.security.SecuredDomainType.COMPETITOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.ORCCertificateUploadConstants;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;

/**
 * This panel houses the functionality to manage the {@link ORCCertificate} linking to the corresponding {@link Boat}.
 * Additionally the BoatTable shows some basic information about the participating Boats and the CertificateTable displays
 * the GPH, Issue Date and identification information. To upload or import some certificates, there is an UploadForm.
 * 
 * @author Daniel Lisunkin (i505543)
 * @author Axel Uhl (d043530)
 *
 */
public class BoatCertificatesPanel extends SimplePanel {
    private final BoatWithCertificateTableWrapper<RefreshableSingleSelectionModel<BoatDTO>> boatTable;
    private final Map<String, BoatDTO> boatsByIdAsString;
    private final CertificatesTableWrapper<RefreshableSingleSelectionModel<ORCCertificate>> certificateTable;
    private final BusyIndicator busyIndicator;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final RegattaIdentifier regattaIdentifier;
    
    private final StringListEditorComposite urls;
    
    private final FileUpload fileUpload;
    
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
    
    private final StringMessages stringMessages;
    
    private final Handler certificateTableSelectionHandler;
    
    private HandlerRegistration certificateTableSelectionHandlerRegistration;
    
    /**
     * Checks that the user has permission to {@link DefaultActions#UPDATE} the regatta for which this
     * panel shows the boats and certificates mappings. Only when this check returns {@code true} will the
     * buttons for importing and assigning certificates be shown, and will the table allow for assignment
     * changes.
     */
    private final Supplier<Boolean> regattaUpdatePermissionCheck;
    
    public BoatCertificatesPanel(final SailingServiceAsync sailingService, final UserService userService, final RegattaDTO regatta,
            final StringMessages stringMessages, final ErrorReporter errorReporter) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.regattaIdentifier = regatta.getRegattaIdentifier();
        this.certificateTableSelectionHandler = createCertificateTableSelectionHandler();
        boatsByIdAsString = new HashMap<>();
        fileUpload = new FileUpload();
        fileUpload.getElement().setAttribute("multiple", "multiple");
        fileUpload.setName(ORCCertificateUploadConstants.CERTIFICATES);
        certificateAssignments = new HashMap<>();
        this.urls = new StringListEditorComposite(Collections.emptySet(), stringMessages, stringMessages.courseAreas(), IconResources.INSTANCE.removeIcon(),
                /* suggested values */ Collections.emptySet(), stringMessages.enterURLsForCertificateDownload());
        this.hiddenCertificateUrlsFields = new ArrayList<>();
        this.boatTable = new BoatWithCertificateTableWrapper<>(sailingService, userService, stringMessages,
                errorReporter, /* multiSelection */ false, /* enablePager */ true, TableWrapper.DEFAULT_PAGING_SIZE, /* allow actions */ true,
                boat->unlink(boat), regatta, boat->certificateAssignments.containsKey(boat));
        this.certificateTable = new CertificatesTableWrapper<>(sailingService, userService, stringMessages,
                errorReporter, /* multiSelection */ false, /* enablePager */ true, TableWrapper.DEFAULT_PAGING_SIZE);
        busyIndicator = new SimpleBusyIndicator(false, 0.8f);
        this.form = new FormPanel();
        form.setAction(Window.Location.createUrlBuilder().setPath("/sailingserver/orc-certificate-import").buildString());
        form.setMethod(FormPanel.METHOD_POST);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.addSubmitHandler(e->busyIndicator.setBusy(true));
        form.addSubmitCompleteHandler(e->formSubmitComplete(e));
        mainPanel = new VerticalPanel();
        form.setWidget(mainPanel);
        this.setWidget(form);
        mainPanel.add(busyIndicator);
        Grid tablesPanel = new Grid(1,2);
        tablesPanel.setWidth("100%");
        final AccessControlledButtonPanel topButtonPanel = new AccessControlledButtonPanel(userService, COMPETITOR);
        mainPanel.add(topButtonPanel);
        final HorizontalPanel urlsPanel = new HorizontalPanel();
        mainPanel.add(urlsPanel);
        urlsPanel.add(new Label(stringMessages.certificateURLs()));
        urlsPanel.add(urls);
        mainPanel.add(fileUpload);
        mainPanel.add(tablesPanel);
        // BUTTON - Refresh
        final Button refreshButton = topButtonPanel.addUnsecuredAction(stringMessages.refresh(), this::refresh);
        refreshButton.ensureDebugId("RefreshButton");
        regattaUpdatePermissionCheck = ()->userService.hasPermission(regatta, DefaultActions.UPDATE);
        final Button importCertificatesButton = topButtonPanel.addAction(stringMessages.importCertificates(),
                regattaUpdatePermissionCheck, this::importCertificates);
        importCertificatesButton.ensureDebugId("ImportCertificatesButton");
        final Button assignCertificatesButton = topButtonPanel.addAction(stringMessages.assignCertificates(), regattaUpdatePermissionCheck,
                this::assignCertificates);
        assignCertificatesButton.ensureDebugId("AssignCertificatesButton");
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
        if (regatta != null) {
            refresh();
        }
    }

    private void formSubmitComplete(SubmitCompleteEvent e) {
        try {
            final JSONObject json = (JSONObject) JSONParser.parseStrict(e.getResults());
            if (json.get(ORCCertificateUploadConstants.CERTIFICATES) != null) {
                sailingService.getORCCertificates(e.getResults(), new AsyncCallback<Collection<ORCCertificate>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        busyIndicator.setBusy(false);
                        errorReporter.reportError(stringMessages.errorObtainingCertificates(caught.getMessage()));
                    }
    
                    @Override
                    public void onSuccess(Collection<ORCCertificate> result) {
                        busyIndicator.setBusy(false);
                        temporarilyDeregisterCertificateTableSelectionHandler();
                        certificateTable.addCertificates(result);
                    }
                });
            } else {
                Notification.notify(stringMessages.errorObtainingCertificates(ORCCertificateUploadConstants.CERTIFICATES), Notification.NotificationType.ERROR);
            }
        } catch (Exception ex) {
            Notification.notify(stringMessages.errorAssigningCertificatesToBoats(ex.getMessage()), Notification.NotificationType.ERROR);
        }
    }

    public void unlink(BoatDTO boat) {
        final ORCCertificate associatedCertificate = certificateAssignments.remove(boat);
        if (associatedCertificate != null && certificateTable.getSelectionModel().isSelected(associatedCertificate)) {
            temporarilyDeregisterCertificateTableSelectionHandler();
            certificateTable.getSelectionModel().setSelected(associatedCertificate, false);
        }
        boatTable.refresh();
    }

    private Handler createCertificateTableSelectionHandler() {
        return e->{
            final BoatDTO boat = boatTable.getSelectionModel().getSelectedObject();
            final ORCCertificate selectedCertificate = certificateTable.getSelectionModel().getSelectedObject();
            if (boat != null) {
                final ORCCertificate assignedCertificate = certificateAssignments.get(boat);
                if (regattaUpdatePermissionCheck.get()) { // is the user permitted to update the regatta at all?
                    if (selectedCertificate != null) {
                        if (assignedCertificate == null || Window.confirm(stringMessages.reallyChangeAssignedCertificateForBoat(boat.toString()))) {
                            assign(boat, selectedCertificate);
                        } else if (assignedCertificate != null) {
                            // re-adjust selection; the user did not confirm:
                            temporarilyDeregisterCertificateTableSelectionHandler();
                            certificateTable.getSelectionModel().setSelected(assignedCertificate, true);
                        }
                    } else {
                        if (assignedCertificate != null) {
                            unlink(boat);
                        }
                    }
                } else {
                    temporarilyDeregisterCertificateTableSelectionHandler();
                    if (assignedCertificate == null) {
                        if (selectedCertificate != null) {
                            certificateTable.getSelectionModel().setSelected(selectedCertificate, false);
                        }
                    } else {
                        certificateTable.getSelectionModel().setSelected(assignedCertificate, true);
                    }
                }
            }
        };
    }

    public void setCertificateSelectionWithoutEventHandling(final ORCCertificate assignedCertificate) {
        // re-adjust selection; the user did not confirm:
        temporarilyDeregisterCertificateTableSelectionHandler();
        certificateTable.getSelectionModel().setSelected(assignedCertificate, true);
    }
    private void wireSelectionModels() {
        boatTable.getSelectionModel().addSelectionChangeHandler(e->{
            final ORCCertificate assignedCertificate = certificateAssignments.get(boatTable.getSelectionModel().getSelectedObject());
            if (assignedCertificate == null) {
                final ORCCertificate selectedCertificate = certificateTable.getSelectionModel().getSelectedObject();
                if (selectedCertificate != null) {
                    temporarilyDeregisterCertificateTableSelectionHandler();
                    certificateTable.getSelectionModel().setSelected(selectedCertificate, false);
                }
            } else {
                setCertificateSelectionWithoutEventHandling(assignedCertificate);
            }
        });
        certificateTableSelectionHandlerRegistration = certificateTable.getSelectionModel().addSelectionChangeHandler(certificateTableSelectionHandler);
        urls.addValueChangeHandler(valueChangeEvent->{
            final Iterable<String> certificateUrls = valueChangeEvent.getValue();
            updateHiddenCertificateUrlsField(certificateUrls);
        });
    }
    
    private void temporarilyDeregisterCertificateTableSelectionHandler() {
        if (certificateTableSelectionHandlerRegistration != null) {
            certificateTableSelectionHandlerRegistration.removeHandler();
            certificateTableSelectionHandlerRegistration = null;
            // It is necessary to do this with the ScheduleDeferred() method,
            // because the SelectionChangeEvent isn't fired directly after
            // selection changes. So an remove of SelectionChangeHandler before
            // the selection change and and new registration directly after it
            // isn't possible.
            Scheduler.get().scheduleDeferred(() -> certificateTableSelectionHandlerRegistration = certificateTable.getSelectionModel().
                    addSelectionChangeHandler(certificateTableSelectionHandler));
        }        
    }

    private void assign(BoatDTO boat, ORCCertificate selectedCertificate) {
        certificateAssignments.put(boat, selectedCertificate);
        boatTable.getDataProvider().getList().set(boatTable.getDataProvider().getList().indexOf(boat), boat);
        boatTable.refresh();
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

    /**
     * Submits the form without the {@link #certificateAssignmentsAsJSON}, making sure that no assignment
     * takes place yet.
     */
    private void importCertificates() {
        form.submit();
    }

    /**
     * Submits the form including the {@link #certificateAssignmentsAsJSON} hidden field
     */
    private void assignCertificates() {
        final Map<String, ORCCertificate> certificatesByBoatIdAsString = new HashMap<>();
        for (final Entry<BoatDTO, ORCCertificate> e : certificateAssignments.entrySet()) {
            certificatesByBoatIdAsString.put(e.getKey().getIdAsString(), e.getValue());
        }
        sailingService.assignORCPerformanceCurveCertificates(regattaIdentifier, certificatesByBoatIdAsString,
                new AsyncCallback<Triple<Integer, Integer, Integer>>() {
                    @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorAssigningCertificates(caught.getMessage()));
            }

            @Override
            public void onSuccess(Triple<Integer, Integer, Integer> result) {
                Notification.notify(stringMessages.insertedAndReplacedAndRemovedCertificateAssignments(
                        result.getA(), result.getB(), result.getC()), Notification.NotificationType.INFO);
            }
        });
    }

    private void refresh() {
        sailingService.getBoatRegistrationsForRegatta(regattaIdentifier, new AsyncCallback<Collection<BoatDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorUnableToGetBoatsForRegatta(regattaIdentifier.toString(), caught.getMessage()));
            }

            @Override
            public void onSuccess(final Collection<BoatDTO> boatResults) {
                boatsByIdAsString.clear();
                for (final BoatDTO boat : boatResults) {
                    boatsByIdAsString.put(boat.getIdAsString(), boat);
                }
                sailingService.getORCCertificateAssignmentsByBoatIdAsString(regattaIdentifier, new AsyncCallback<Map<String, ORCCertificate>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorObtainingCertificates(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Map<String, ORCCertificate> result) {
                        certificateAssignments.clear();
                        final Map<String, ORCCertificate> certificatesById = new HashMap<>();
                        final Set<BoatDTO> boatsToRefreshInTable = new HashSet<>();
                        for (final Entry<String, ORCCertificate> e : result.entrySet()) {
                            // avoid duplicates with same ID:
                            final ORCCertificate certificateToUse = certificatesById.computeIfAbsent(e.getValue().getId(), key->e.getValue());
                            final BoatDTO boat = boatsByIdAsString.get(e.getKey());
                            if (boat != null) {
                                certificateAssignments.put(boat, certificateToUse);
                                boatsToRefreshInTable.add(boat);
                            }
                        }
                        temporarilyDeregisterCertificateTableSelectionHandler();
                        certificateTable.setCertificates(new HashSet<>(certificatesById.values()));
                        boatTable.setBoats(boatResults);
                    }
                });
            }
        });
    }
}
