package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.dto.RegattaCreationParametersDTO;
import com.sap.sailing.domain.common.dto.SeriesCreationParametersDTO;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CourseAreaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/**
 * Allows administrators to manage the structure of a regatta. Each regatta consists of several substructures like
 * races, series and groups (big fleets divided into racing groups).
 * 
 * @author Frank Mittag (C5163974)
 * 
 */
public class RegattaManagementPanel extends SimplePanel implements RegattasDisplayer {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;

    private final RegattaRefresher regattaRefresher;
    private final RefreshableMultiSelectionModel<RegattaDTO> refreshableRegattaMultiSelectionModel;
    private Button removeRegattaButton;

    private RegattaListComposite regattaListComposite;
    private RegattaDetailsComposite regattaDetailsComposite;

    private final CaptionPanel regattasPanel;
    
    public RegattaManagementPanel(SailingServiceAsync sailingService, ErrorReporter errorReporter,
            StringMessages stringMessages, RegattaRefresher regattaRefresher) {
        this.sailingService = sailingService;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;

        VerticalPanel mainPanel = new VerticalPanel();
        setWidget(mainPanel);
        mainPanel.setWidth("100%");

        regattasPanel = new CaptionPanel(stringMessages.regattas());
        mainPanel.add(regattasPanel);
        VerticalPanel regattasContentPanel = new VerticalPanel();
        regattasPanel.setContentWidget(regattasContentPanel);
        
        HorizontalPanel regattaManagementControlsPanel = new HorizontalPanel();
        regattaManagementControlsPanel.setSpacing(5);
        
        Button addRegattaBtn = new Button(stringMessages.addRegatta());
        addRegattaBtn.ensureDebugId("AddRegattaButton");
        addRegattaBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openCreateRegattaDialog();
            }
        });
        regattaManagementControlsPanel.add(addRegattaBtn);
        
        removeRegattaButton = new Button(stringMessages.remove());
        removeRegattaButton.ensureDebugId("RemoveRegattaButton");
        removeRegattaButton.setEnabled(false);
        removeRegattaButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm("Do you really want to remove the regattas? This will also remove all leaderboards for the regattas!")) {
                    //Creating a new Collection, because getSelectedRegattas returns an 
                    //unmodifiable collection, which can't be sent to the server.
                    Collection<RegattaIdentifier> regattas = new HashSet<RegattaIdentifier>();
                    for (RegattaDTO regatta : refreshableRegattaMultiSelectionModel.getSelectedSet()) {
                        regattas.add(regatta.getRegattaIdentifier());
                    }
                    removeRegattas(regattas);
                }
            }
        });
        regattaManagementControlsPanel.add(removeRegattaButton);
        regattasContentPanel.add(regattaManagementControlsPanel);
        regattaListComposite = new RegattaListComposite(sailingService, regattaRefresher, errorReporter, stringMessages);
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
                removeRegattaButton.setEnabled(!selectedRegattas.isEmpty());
            }            
        });
        regattasContentPanel.add(regattaListComposite);
        
        regattaDetailsComposite = new RegattaDetailsComposite(sailingService, regattaRefresher, errorReporter, stringMessages);
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
        RegattaWithSeriesAndFleetsCreateDialog dialog = new RegattaWithSeriesAndFleetsCreateDialog(existingRegattas, existingEvents, stringMessages,
                new DialogCallback<RegattaDTO>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(RegattaDTO newRegatta) {
                createNewRegatta(newRegatta, existingEvents);
            }
        });
        dialog.ensureDebugId("RegattaCreateDialog");
        dialog.show();
    }
    
    private void openCreateDefaultRegattaLeaderboardDialog(final RegattaDTO newRegatta, final List<EventDTO> existingEvents) {
        CreateDefaultRegattaLeaderboardDialog dialog = new CreateDefaultRegattaLeaderboardDialog(sailingService, stringMessages, errorReporter, newRegatta, new DialogCallback<RegattaIdentifier>() {
            @Override
            public void ok(RegattaIdentifier regattaIdentifier) {
                sailingService.createRegattaLeaderboard(regattaIdentifier, /* displayName */ null, new int[]{},
                        new AsyncCallback<StrippedLeaderboardDTO>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to create default regatta leaderboard for " + newRegatta.getName()
                                + ": " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(StrippedLeaderboardDTO result) {
                        if (newRegatta.defaultCourseAreaUuid != null) {
                            // Show the event's leaderboard groups and allow the user to pick one to assign the regatta leaderboard to
                            final EventDTO event = getEventForCourseArea(existingEvents, newRegatta.defaultCourseAreaUuid);
                            if (!event.getLeaderboardGroups().isEmpty()) {
                                openRegattaLeaderboardToLeaderboardGroupOfEventLinkingDialog(result, event);
                            }
                        }
                    }

                });
            }

            @Override
            public void cancel() {
            }
        });
        dialog.ensureDebugId("CreateDefaultRegattaDialog");
        dialog.show();
    }
    
    /**
     * When a new regatta with a new regatta leaderboard has been created, the user will now be given the chance to link
     * the regatta leaderboard into a leaderboard group of the event out of which the regatta chose its default course area.
     * 
     * @param newRegattaLeaderboard the new regatta leaderboard that the user may link now to a leaderboard group of an event
     * @param eventToLinkRegattaTo an event that has at least one {@link EventDTO#getLeaderboardGroups() leaderboard group}
     */
    private void openRegattaLeaderboardToLeaderboardGroupOfEventLinkingDialog(final StrippedLeaderboardDTO newRegattaLeaderboard, EventDTO eventToLinkRegattaTo) {
        LinkRegattaLeaderboardToLeaderboardGroupOfEventDialog dialog = new LinkRegattaLeaderboardToLeaderboardGroupOfEventDialog(sailingService, stringMessages, errorReporter, newRegattaLeaderboard, eventToLinkRegattaTo,
                new DialogCallback<LeaderboardGroupDTO>() {
                    @Override
                    public void ok(final LeaderboardGroupDTO selectedLeaderboardGroup) {
                        final List<String> leaderboardNames = new ArrayList<>();
                        for (StrippedLeaderboardDTO leaderboard : selectedLeaderboardGroup.getLeaderboards()) {
                            leaderboardNames.add(leaderboard.name);
                        }
                        leaderboardNames.add(newRegattaLeaderboard.name);
                        sailingService.updateLeaderboardGroup(selectedLeaderboardGroup.getName(), selectedLeaderboardGroup.getName(), selectedLeaderboardGroup.description,
                                selectedLeaderboardGroup.getDisplayName(), leaderboardNames, selectedLeaderboardGroup.getOverallLeaderboardDiscardThresholds(),
                                selectedLeaderboardGroup.getOverallLeaderboardScoringSchemeType(), new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError(stringMessages.failedToLinkLeaderboardToLeaderboardGroup(newRegattaLeaderboard.name, selectedLeaderboardGroup.getName()));
                                    }

                                    @Override
                                    public void onSuccess(Void result) {
                                    }
                                }));
                    }

                    @Override
                    public void cancel() {
                    }
        });
        dialog.ensureDebugId("LinkRegattaLeaderboardToLeaderboardGroupOfEventDialog");
        dialog.show();
    }

    private EventDTO getEventForCourseArea(final List<EventDTO> existingEvents, final UUID courseAreaId) {
        EventDTO result = null;
        eventLoop:
        for (final EventDTO event : existingEvents) {
            if (event.venue != null) {
                for (CourseAreaDTO courseArea : event.venue.getCourseAreas()) {
                    if (courseArea.id.equals(courseAreaId)) {
                        result = event;
                        break eventLoop;
                    }
                }
            }
        }
        return result;
    }

    private void createNewRegatta(final RegattaDTO newRegatta, final List<EventDTO> existingEvents) {
        LinkedHashMap<String, SeriesCreationParametersDTO> seriesStructure = new LinkedHashMap<String, SeriesCreationParametersDTO>();
        for (SeriesDTO seriesDTO : newRegatta.series) {
            SeriesCreationParametersDTO seriesPair = new SeriesCreationParametersDTO(seriesDTO.getFleets(),
                    seriesDTO.isMedal(), seriesDTO.isStartsWithZeroScore(),
                    seriesDTO.isFirstColumnIsNonDiscardableCarryForward(), seriesDTO.getDiscardThresholds(),
                    seriesDTO.hasSplitFleetContiguousScoring());
            seriesStructure.put(seriesDTO.getName(), seriesPair);
        }
        sailingService.createRegatta(newRegatta.getName(), newRegatta.boatClass==null?null:newRegatta.boatClass.getName(),
                newRegatta.startDate, newRegatta.endDate, 
                new RegattaCreationParametersDTO(seriesStructure), true,
                newRegatta.scoringScheme, newRegatta.defaultCourseAreaUuid, newRegatta.useStartTimeInference,
                newRegatta.rankingMetricType, new AsyncCallback<RegattaDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to create new regatta " + newRegatta.getName() + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(RegattaDTO regatta) {
                regattaRefresher.fillRegattas();
                openCreateDefaultRegattaLeaderboardDialog(regatta, existingEvents);
            }
        });
    }

    @Override
    public void fillRegattas(Iterable<RegattaDTO> regattas) {
        regattaListComposite.fillRegattas(regattas);
    }
}
