package com.sap.sailing.gwt.ui.adminconsole;

import static com.sap.sailing.domain.common.security.SecuredDomainType.REGATTA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.gwt.ui.client.EventsRefresher;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;

/**
 * Allows administrators to manage the structure of a regatta. Each regatta consists of several substructures like
 * races, series and groups (big fleets divided into racing groups).
 * 
 * @author Frank Mittag (C5163974)
 */
public class RegattaManagementPanel extends SimplePanel implements RegattasDisplayer {

    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final RegattaRefresher regattaRefresher;
    private final EventsRefresher eventsRefresher;
    private final RefreshableMultiSelectionModel<RegattaDTO> refreshableRegattaMultiSelectionModel;
    private final RegattaListComposite regattaListComposite;
    private final RegattaDetailsComposite regattaDetailsComposite;
    private final UserService userService;
    
    public RegattaManagementPanel(SailingServiceAsync sailingService, UserService userService,
            ErrorReporter errorReporter, StringMessages stringMessages, RegattaRefresher regattaRefresher,
            EventsRefresher eventsRefresher) {
        this.sailingService = sailingService;
        this.userService = userService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;
        this.eventsRefresher = eventsRefresher;

        final VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");

        final CaptionPanel regattasPanel = new CaptionPanel(stringMessages.regattas());
        mainPanel.add(regattasPanel);
        final VerticalPanel regattasContentPanel = new VerticalPanel();
        regattasPanel.setContentWidget(regattasContentPanel);
        
        final AccessControlledButtonPanel buttonPanel = new AccessControlledButtonPanel(userService, REGATTA);
        final Button create = buttonPanel.addCreateAction(stringMessages.addRegatta(), this::openCreateRegattaDialog);
        create.ensureDebugId("AddRegattaButton");

        final Button remove = buttonPanel.addRemoveAction(stringMessages.remove(), new Command() {

            @Override
            public void execute() {
                if (askUserForConfirmation()) {
                    // unmodifiable collection can't be sent to the server.
                    final Collection<RegattaIdentifier> regattas = createModifiableCollection();
                    removeRegattas(regattas);
                }
            }

            private boolean askUserForConfirmation() {
                if (refreshableRegattaMultiSelectionModel
                        .itemIsSelectedButNotVisible(regattaListComposite.getRegattaTable().getVisibleItems())) {
                    final String regattaNames = refreshableRegattaMultiSelectionModel.getSelectedSet().stream()
                            .map(RegattaDTO::getName).collect(Collectors.joining("\n"));
                    return Window.confirm(stringMessages.doYouReallyWantToRemoveNonVisibleRegattas(regattaNames));
                }
                return Window.confirm(stringMessages.doYouReallyWantToRemoveRegattas());
            }
        });
        regattasContentPanel.add(buttonPanel);

        regattaListComposite = new RegattaListComposite(sailingService, userService, regattaRefresher, errorReporter,
                stringMessages);
        regattaListComposite.ensureDebugId("RegattaListComposite");
        refreshableRegattaMultiSelectionModel = regattaListComposite.getRefreshableMultiSelectionModel();
        refreshableRegattaMultiSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                List<RegattaDTO> selectedRegattas = new ArrayList<>(refreshableRegattaMultiSelectionModel.getSelectedSet());
                final RegattaIdentifier selectedRegatta;
                if (selectedRegattas.size() == 1) {
                    selectedRegatta = selectedRegattas.iterator().next().getRegattaIdentifier();
                    if (selectedRegatta != null && regattaListComposite.getAllRegattas() != null) {
                        for (RegattaDTO regattaDTO : regattaListComposite.getAllRegattas()) {
                            if (regattaDTO.getRegattaIdentifier().equals(selectedRegatta)) {
                                regattaDetailsComposite.setRegatta(regattaDTO);
                                regattaDetailsComposite.setVisible(true);
                                break;
                            }
                        }
                    }
                } else {
                    regattaDetailsComposite.setRegatta(null);
                    regattaDetailsComposite.setVisible(false);
                }
                remove.setEnabled(!selectedRegattas.isEmpty());
                remove.setText(selectedRegattas.size() <= 1 ? stringMessages.remove()
                        : stringMessages.removeNumber(selectedRegattas.size()));
            }
        });
        regattasContentPanel.add(regattaListComposite);
        
        regattaDetailsComposite = new RegattaDetailsComposite(sailingService, userService, regattaRefresher,
                errorReporter, stringMessages);
        regattaDetailsComposite.ensureDebugId("RegattaDetailsComposite");
        regattaDetailsComposite.setVisible(false);
        mainPanel.add(regattaDetailsComposite);
    }

    protected void removeRegattas(Collection<RegattaIdentifier> regattas) {
        if (!regattas.isEmpty()) {
            sailingService.removeRegattas(regattas, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to remove the regattas:" + caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    regattaRefresher.fillRegattas();
                }
            });
        }
    }

    private void openCreateRegattaDialog() {
        final Collection<RegattaDTO> existingRegattas = Collections.unmodifiableCollection(regattaListComposite.getAllRegattas());
        sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                openCreateRegattaDialog(existingRegattas, Collections.<EventDTO>emptyList());
            }

            @Override
            public void onSuccess(List<EventDTO> result) {
                openCreateRegattaDialog(existingRegattas, Collections.unmodifiableList(result));
            }
        });
    }

    private void openCreateRegattaDialog(Collection<RegattaDTO> existingRegattas, final List<EventDTO> existingEvents) {
        RegattaWithSeriesAndFleetsCreateDialog dialog = new RegattaWithSeriesAndFleetsCreateDialog(existingRegattas, existingEvents, /*eventToSelect*/ null, sailingService, stringMessages,
                new CreateRegattaCallback(userService, sailingService, stringMessages, errorReporter, regattaRefresher,
                        eventsRefresher, existingEvents));
        dialog.ensureDebugId("RegattaCreateDialog");
        dialog.show();
    }

    @Override
    public void fillRegattas(Iterable<RegattaDTO> regattas) {
        regattaListComposite.fillRegattas(regattas);
    }
    
    private Collection<RegattaIdentifier> createModifiableCollection() {
        Collection<RegattaIdentifier> regattas = new HashSet<RegattaIdentifier>();
        for (RegattaDTO regatta : refreshableRegattaMultiSelectionModel.getSelectedSet()) {
            regattas.add(regatta.getRegattaIdentifier());
        }
        return regattas;
    }
}
