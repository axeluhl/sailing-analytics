package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;

/**
 * Shows the currently tracked events/races in a table. Updated if subscribed as an {@link RegattasDisplayer}, e.g., with
 * the {@link AdminConsoleEntryPoint}.
 */
public class TrackedRacesListComposite extends AbstractTrackedRacesListComposite {
    private final Set<TrackedRaceChangedListener> raceIsTrackedRaceChangeListener;
    private Button btnUntrack;
    private Button btnRemoveRace;
    private Button btnSetDelayToLive;
    private Button btnExport;
    private ExportPopup exportPopup;

    public TrackedRacesListComposite(final SailingServiceAsync sailingService, final ErrorReporter errorReporter,
            final RegattaRefresher regattaRefresher, RaceSelectionProvider raceSelectionProvider,
            final StringMessages stringMessages, boolean hasMultiSelection) {
        super(sailingService, errorReporter, regattaRefresher, raceSelectionProvider, stringMessages, hasMultiSelection);
        this.raceIsTrackedRaceChangeListener = new HashSet<TrackedRaceChangedListener>();
    }

    private void showSetDelayToLiveDialog() {
        TrackedRacesSettings settings = new TrackedRacesSettings();
        settings.setDelayToLiveInSeconds(DEFAULT_LIVE_DELAY_IN_MILLISECONDS);
        
        SettingsDialog<TrackedRacesSettings> settingsDialog = new SettingsDialog<TrackedRacesSettings>(this, stringMessages);
        settingsDialog.show();
    }
    
    public void addTrackedRaceChangeListener(TrackedRaceChangedListener listener) {
        this.raceIsTrackedRaceChangeListener.add(listener);
    }

    private void stopTrackingRaces(final Iterable<RaceDTO> races) {
        final List<RegattaAndRaceIdentifier> racesToStopTracking = new ArrayList<RegattaAndRaceIdentifier>();
        for (RaceDTO race : races) {
            if (race.isTracked) {
                racesToStopTracking.add(race.getRaceIdentifier());
            }
        }
        sailingService.stopTrackingRaces(racesToStopTracking, new MarkedAsyncCallback<Void>(
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Exception trying to stop tracking races " + races + ": " + caught.getMessage());
                    }
        
                    @Override
                    public void onSuccess(Void result) {
                        regattaRefresher.fillRegattas();
                        for (TrackedRaceChangedListener listener : raceIsTrackedRaceChangeListener) {
                            listener.changeTrackingRace(racesToStopTracking, false);
                        }
                    }
                }));
    }

    private void removeAndUntrackRaces(final Iterable<RaceDTO> races) {
        final List<RegattaNameAndRaceName> regattaNamesAndRaceNames = new ArrayList<RegattaNameAndRaceName>();
        for (RaceDTO race : races) {
            regattaNamesAndRaceNames.add((RegattaNameAndRaceName) race.getRaceIdentifier());
        }
        sailingService.removeAndUntrackRaces(regattaNamesAndRaceNames, new MarkedAsyncCallback<Void>(
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Exception trying to remove races " + regattaNamesAndRaceNames +
                                ": " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        regattaRefresher.fillRegattas();
                        for (TrackedRaceChangedListener listener : raceIsTrackedRaceChangeListener) {
                            listener.changeTrackingRace(regattaNamesAndRaceNames, false);
                        }
                    }
                }));
    }

    @Override
    protected void addControlButtons(HorizontalPanel trackedRacesButtonPanel) {
        btnRemoveRace = new Button(stringMessages.remove());
        btnRemoveRace.ensureDebugId("RemoveRaceButton");
        btnRemoveRace.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                removeAndUntrackRaces(getSelectedRaces());
            }
        });
        btnRemoveRace.setEnabled(false);
        trackedRacesButtonPanel.add(btnRemoveRace);
        
        btnUntrack = new Button(stringMessages.stopTracking());
        btnUntrack.ensureDebugId("StopTrackingButton");
        btnUntrack.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent click) {
                stopTrackingRaces(getSelectedRaces());
            }
        });
        btnUntrack.setEnabled(false);
        trackedRacesButtonPanel.add(btnUntrack);
        
        btnSetDelayToLive = new Button(stringMessages.setDelayToLive() + "...");
        btnSetDelayToLive.ensureDebugId("SetDelayToLiveButton");
        btnSetDelayToLive.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showSetDelayToLiveDialog();
            }
        });
        trackedRacesButtonPanel.add(btnSetDelayToLive);

        exportPopup = new ExportPopup(stringMessages);
        btnExport = new Button(stringMessages.export());
        btnExport.ensureDebugId("ExportButton");
        btnExport.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                exportPopup.center(getSelectedRaces());
            }
        });
        btnExport.setEnabled(false);
        trackedRacesButtonPanel.add(btnExport);
    }

    @Override
    protected void makeControlsReactToSelectionChange(List<RaceDTO> selectedRaces) {
        if (selectedRaces.isEmpty()) {
            btnRemoveRace.setEnabled(false);
            btnUntrack.setEnabled(false);
            btnExport.setEnabled(false);
        } else {
            btnRemoveRace.setEnabled(true);
            btnUntrack.setEnabled(true);
            btnExport.setEnabled(true);
        }
    }

    @Override
    protected void makeControlsReactToFillRegattas(List<RegattaDTO> regattas) {
        if (regattas.isEmpty()) {
            btnUntrack.setVisible(false);
            btnRemoveRace.setVisible(false);
            btnSetDelayToLive.setVisible(false);
            btnExport.setVisible(false);
        } else {
            btnUntrack.setVisible(true);
            btnUntrack.setEnabled(false);
            btnRemoveRace.setVisible(true);
            btnRemoveRace.setEnabled(false);
            btnSetDelayToLive.setVisible(true);
            btnExport.setVisible(true);
        }
    }

    @Override
    public String getDependentCssClassName() {
        return "trackedRacesListComposite";
    }

    
}
