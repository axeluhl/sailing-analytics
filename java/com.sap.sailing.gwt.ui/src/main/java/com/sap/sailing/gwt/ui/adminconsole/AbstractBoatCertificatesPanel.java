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
import com.google.gwt.json.client.JSONString;
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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.domain.common.orc.ORCCertificateUploadConstants;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.controls.busyindicator.BusyIndicator;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.controls.listedit.StringListEditorComposite;
import com.sap.sse.gwt.client.dialog.DialogUtils;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.dto.SecuredDTO;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;

/**
 * This panel houses the functionality to manage the {@link ORCCertificate} linking to the corresponding {@link Boat}.
 * Additionally the BoatTable shows some basic information about the participating Boats and the CertificateTable
 * displays the GPH, Issue Date and identification information. To upload or import some certificates, there is an
 * UploadForm.
 * <p>
 * 
 * This is an abstract base class that can be adapted by subclasses to fit either a {@link RegattaDTO} or a
 * {@link RaceColumnDTO}/{@link FleetDTO} context.
 * 
 * @author Daniel Lisunkin (i505543)
 * @author Axel Uhl (d043530)
 *
 */
public abstract class AbstractBoatCertificatesPanel extends SimplePanel {
    private final BoatWithCertificateTableWrapper<RefreshableMultiSelectionModel<BoatDTO>> boatTable;
    private final Map<String, BoatDTO> boatsByIdAsString;
    private final CertificatesTableWrapper<RefreshableSingleSelectionModel<ORCCertificate>> certificateTable;
    private final BusyIndicator busyIndicator;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    
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
     * Checks that the user has permission to {@link DefaultActions#UPDATE} the context to which changes in certificate
     * assignments will be stored, such as a regatta or a race whose log would then be updated. Only when this check
     * returns {@code true} will the buttons for importing and assigning certificates be shown, and will the table allow
     * for assignment changes.
     */
    private final Supplier<Boolean> contextUpdatePermissionCheck;
    private final String errorContext;
    
    /**
     * After invoking this superclass constructor, and after doing any other initialization work, subclasses must call {@link #refresh}.
     */
    public AbstractBoatCertificatesPanel(final SailingServiceAsync sailingService, final UserService userService,
            final SecuredDTO objectToCheckUpdatePermissionFor, final StringMessages stringMessages,
            final ErrorReporter errorReporter, Supplier<Boolean> contextUpdatePermissionCheck, String errorContext) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.certificateTableSelectionHandler = createCertificateTableSelectionHandler();
        this.errorContext = errorContext;
        boatsByIdAsString = new HashMap<>();
        fileUpload = new FileUpload();
        fileUpload.getElement().setAttribute("multiple", "multiple");
        fileUpload.setName(ORCCertificateUploadConstants.CERTIFICATES);
        certificateAssignments = new HashMap<>();
        this.urls = new StringListEditorComposite(Collections.emptySet(), stringMessages, stringMessages.courseAreas(), IconResources.INSTANCE.removeIcon(),
                /* suggested values */ Collections.emptySet(), stringMessages.enterURLsForCertificateDownload());
        this.hiddenCertificateUrlsFields = new ArrayList<>();
        this.boatTable = new BoatWithCertificateTableWrapper<>(sailingService, userService, stringMessages,
                errorReporter, /* multiSelection */ true, /* enablePager */ true, TableWrapper.DEFAULT_PAGING_SIZE, /* allow actions */ true,
                boat->unlink(boat), objectToCheckUpdatePermissionFor, boat->certificateAssignments.containsKey(boat));
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
        mainPanel.add(createSearchPanel(stringMessages));
        mainPanel.add(tablesPanel);
        final Button refreshButton = topButtonPanel.addUnsecuredAction(stringMessages.refresh(), this::refresh);
        refreshButton.ensureDebugId("RefreshButton");
        this.contextUpdatePermissionCheck = contextUpdatePermissionCheck;
        final Button importCertificatesButton = topButtonPanel.addAction(stringMessages.importCertificates(),
                contextUpdatePermissionCheck, this::importCertificates);
        importCertificatesButton.ensureDebugId("ImportCertificatesButton");
        final Button suggestCertificatesButton = topButtonPanel.addAction(stringMessages.suggestCertificatesForSelectedBoats(),
                contextUpdatePermissionCheck, this::suggestCertificates);
        suggestCertificatesButton.ensureDebugId("SuggestCertificatesButton");
        // TABLE - Boats
        CaptionPanel boatCaptionPanel = new CaptionPanel(stringMessages.boats());
        boatCaptionPanel.add(boatTable);
        tablesPanel.setWidget(0, 0, boatCaptionPanel);
        // TABLE - Certificates
        CaptionPanel certificatesCaptionPanel = new CaptionPanel(stringMessages.certificates());
        certificatesCaptionPanel.add(certificateTable);
        wireSelectionModels();
        tablesPanel.setWidget(0, 1, certificatesCaptionPanel);
    }

    private Widget createSearchPanel(StringMessages stringMessages) {
        final Button searchButton = new Button(stringMessages.search());
        final CaptionPanel searchPanel = new CaptionPanel(stringMessages.search());
        final VerticalPanel vp = new VerticalPanel();
        searchPanel.add(vp);
        vp.add(new Label(stringMessages.searchForCertificatesExplanation()));
        final Grid searchGrid = new Grid(2, 7);
        vp.add(searchGrid);
        searchGrid.setWidget(0, 0, searchButton);
        searchGrid.setWidget(0, 1, new Label(stringMessages.issuingCountry()));
        final ListBox issuingCountryListBox = new ListBox();
        DialogUtils.makeCountrySelection(issuingCountryListBox, null);
        DialogUtils.linkEnterToButton(searchButton, issuingCountryListBox);
        searchGrid.setWidget(0, 2, issuingCountryListBox);
        searchGrid.setWidget(0, 3, new Label(stringMessages.yearOfIssuance()));
        final IntegerBox yearOfIssuanceBox = new IntegerBox();
        DialogUtils.linkEnterToButton(searchButton, yearOfIssuanceBox);
        searchGrid.setWidget(0, 4, yearOfIssuanceBox);
        searchGrid.setWidget(0, 5, new Label(stringMessages.referenceNumber()));
        final TextBox referenceNumberTextBox = new TextBox();
        DialogUtils.linkEnterToButton(searchButton, referenceNumberTextBox);
        searchGrid.setWidget(0, 6, referenceNumberTextBox);
        searchGrid.setWidget(1, 1, new Label(stringMessages.yachtName()));
        final TextBox yachtNameTextBox = new TextBox();
        DialogUtils.linkEnterToButton(searchButton, yachtNameTextBox);
        searchGrid.setWidget(1, 2, yachtNameTextBox);
        searchGrid.setWidget(1, 3, new Label(stringMessages.sailNumber()));
        final TextBox sailNumberTextBox = new TextBox();
        DialogUtils.linkEnterToButton(searchButton, sailNumberTextBox);
        searchGrid.setWidget(1, 4, sailNumberTextBox);
        searchGrid.setWidget(1, 5, new Label(stringMessages.boatClass()));
        final TextBox boatClassNameTextBox = new TextBox();
        DialogUtils.linkEnterToButton(searchButton, boatClassNameTextBox);
        searchGrid.setWidget(1, 6, boatClassNameTextBox);
        searchButton.addClickHandler(e->{
            busyIndicator.setBusy(true);
            sailingService.searchORCBoatCertificates(DialogUtils.getSelectedCountry(issuingCountryListBox),
                   yearOfIssuanceBox.getValue(), referenceNumberTextBox.getValue().trim().isEmpty() ?
                           null : referenceNumberTextBox.getValue(),
                           yachtNameTextBox.getValue() == null ? null : yachtNameTextBox.getValue(),
                           sailNumberTextBox.getValue() == null ? null : sailNumberTextBox.getValue(),
                           boatClassNameTextBox.getValue() == null ? null : boatClassNameTextBox.getValue(),
                   new AsyncCallback<Set<ORCCertificate>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        busyIndicator.setBusy(false);
                        errorReporter.reportError(stringMessages.errorSearchingForCertificates(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Set<ORCCertificate> result) {
                        busyIndicator.setBusy(false);
                        certificateTable.addCertificates(result);
                    }
           }); 
        });
        return searchPanel;
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
                Notification.notify(
                        stringMessages.errorObtainingCertificates(
                                ((JSONString) json.get(ORCCertificateUploadConstants.MESSAGE)).stringValue()),
                        Notification.NotificationType.ERROR);
                busyIndicator.setBusy(false);
            }
        } catch (Exception ex) {
            Notification.notify(stringMessages.errorAssigningCertificatesToBoats(ex.getMessage()), Notification.NotificationType.ERROR);
            busyIndicator.setBusy(false);
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
            final Set<BoatDTO> selectedBoats = boatTable.getSelectionModel().getSelectedSet();
            final ORCCertificate selectedCertificate = certificateTable.getSelectionModel().getSelectedObject();
            if (selectedBoats != null && !selectedBoats.isEmpty()) {
                for (final BoatDTO boat : selectedBoats) {
                    final ORCCertificate assignedCertificate = certificateAssignments.get(boat);
                    if (contextUpdatePermissionCheck.get()) { // is the user permitted to update the regatta at all?
                        if (selectedCertificate != null) {
                            if (assignedCertificate == null || assignedCertificate == selectedCertificate ||
                                    Window.confirm(stringMessages.reallyChangeAssignedCertificateForBoat(boat.toString()))) {
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
            }
        };
    }

    private void setCertificateSelectionWithoutEventHandling(final ORCCertificate assignedCertificate) {
        // re-adjust selection; the user did not confirm:
        temporarilyDeregisterCertificateTableSelectionHandler();
        certificateTable.getSelectionModel().setSelected(assignedCertificate, true);
    }
    
    private void wireSelectionModels() {
        boatTable.getSelectionModel().addSelectionChangeHandler(e->{
            final ORCCertificate selectedCertificate = certificateTable.getSelectionModel().getSelectedObject();
            final Set<BoatDTO> selectedBoats = boatTable.getSelectionModel().getSelectedSet();
            if (selectedBoats != null && selectedBoats.size() == 1) {
                final BoatDTO selectedBoat = selectedBoats.iterator().next();
                final ORCCertificate assignedCertificate = certificateAssignments.get(selectedBoat);
                if (assignedCertificate == null) {
                    if (selectedCertificate != null) {
                        temporarilyDeregisterCertificateTableSelectionHandler();
                        certificateTable.getSelectionModel().setSelected(selectedCertificate, false);
                    }
                } else {
                    setCertificateSelectionWithoutEventHandling(assignedCertificate);
                }
            } else {
                temporarilyDeregisterCertificateTableSelectionHandler();
                certificateTable.getSelectionModel().setSelected(selectedCertificate, false);
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
    
    private void suggestCertificates() {
        final Set<BoatDTO> selectedBoats = boatTable.getSelectionModel().getSelectedSet();
        if (selectedBoats != null && !selectedBoats.isEmpty()) {
            busyIndicator.setBusy(true);
            sailingService.getSuggestedORCBoatCertificates(new ArrayList<>(selectedBoats),
                    new AsyncCallback<Map<BoatDTO, Set<ORCCertificate>>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            busyIndicator.setBusy(false);
                            errorReporter.reportError(stringMessages.errorSuggestingCertificates(caught.getMessage()));
                        }
    
                        @Override
                        public void onSuccess(Map<BoatDTO, Set<ORCCertificate>> result) {
                            busyIndicator.setBusy(false);
                            for (final Entry<BoatDTO, Set<ORCCertificate>> e : result.entrySet()) {
                                certificateTable.addCertificates(e.getValue());
                                if (!certificateAssignments.containsKey(e.getKey()) && e.getValue().size() == 1) {
                                    // no mapping so far, and exactly one result;
                                    // that's good enough to create a mapping automatically:
                                    certificateAssignments.put(e.getKey(), e.getValue().iterator().next());
                                }
                            }
                        }
            });
        }
    }

    /**
     * Submits the form including the {@link #certificateAssignmentsAsJSON} hidden field, thus making the current
     * mappings held by {@link #certificateAssignments} persistent in the context provided by the concrete implementation.
     */
    public void assignCertificates() {
        final Map<String, ORCCertificate> certificatesByBoatIdAsString = new HashMap<>();
        for (final Entry<BoatDTO, ORCCertificate> e : certificateAssignments.entrySet()) {
            certificatesByBoatIdAsString.put(e.getKey().getIdAsString(), e.getValue());
        }
        final AsyncCallback<Triple<Integer, Integer, Integer>> callback = new AsyncCallback<Triple<Integer, Integer, Integer>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorAssigningCertificates(caught.getMessage()));
            }

            @Override
            public void onSuccess(Triple<Integer, Integer, Integer> result) {
                Notification.notify(stringMessages.insertedAndReplacedAndRemovedCertificateAssignments(result.getA(),
                        result.getB(), result.getC()), Notification.NotificationType.INFO);
            }
        };
        assignCertificates(sailingService, certificatesByBoatIdAsString, callback);
    }

    protected abstract void assignCertificates(SailingServiceAsync sailingService,
            Map<String, ORCCertificate> certificatesByBoatIdAsString,
            AsyncCallback<Triple<Integer, Integer, Integer>> callback);

    protected void refresh() {
        final AsyncCallback<Collection<BoatDTO>> callbackForGetBoats = new AsyncCallback<Collection<BoatDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                reportErrorWhileGettingBoatsForContext(caught, errorContext);
            }

            public void reportErrorWhileGettingBoatsForContext(Throwable caught, final String errorContext) {
                errorReporter.reportError(stringMessages.errorUnableToGetBoatsForRegatta(errorContext, caught.getMessage()));
            }

            @Override
            public void onSuccess(final Collection<BoatDTO> boatResults) {
                boatsByIdAsString.clear();
                for (final BoatDTO boat : boatResults) {
                    boatsByIdAsString.put(boat.getIdAsString(), boat);
                }
                final AsyncCallback<Map<String, ORCCertificate>> callbackForGetCertificates = new AsyncCallback<Map<String, ORCCertificate>>() {
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
                };
                getORCCertificateAssignemtnsByBoatIdAsString(sailingService, callbackForGetCertificates);
            }
        };
        getBoats(sailingService, callbackForGetBoats);

    }

    protected abstract void getBoats(SailingServiceAsync sailingService, final AsyncCallback<Collection<BoatDTO>> callbackForGetBoats);
    
    protected abstract void getORCCertificateAssignemtnsByBoatIdAsString(SailingServiceAsync sailingService, final AsyncCallback<Map<String, ORCCertificate>> callbackForGetCertificates);
}
