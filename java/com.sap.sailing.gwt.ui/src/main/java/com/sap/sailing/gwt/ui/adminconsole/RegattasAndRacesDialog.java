package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegattasAndRacesDialog extends DataEntryDialog<Set<RegattaAndRaceIdentifier>> implements RegattasDisplayer {

    protected StringMessages stringMessages;
    protected final TrackedRacesListComposite trackedRacesListComposite;
    protected final MediaTrack mediaTrack;
    public boolean empty = true;
    public boolean started = false;
    private final VerticalPanel panel;

    public RegattasAndRacesDialog(SailingServiceAsync sailingService, final MediaTrack mediaTrack,
            ErrorReporter errorReporter, RegattaRefresher regattaRefresher, StringMessages stringMessages,
            Validator<Set<RegattaAndRaceIdentifier>> validator, DialogCallback<Set<RegattaAndRaceIdentifier>> callback) {
        super(stringMessages.regattaAndRace(), null, stringMessages.ok(), stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        this.mediaTrack = mediaTrack;
        trackedRacesListComposite = new TrackedRacesListComposite(sailingService, errorReporter, regattaRefresher,
                new RaceSelectionModel(), stringMessages, /* multiselection */true) {
            protected boolean raceIsToBeAddedToList(RaceDTO race) {
                if (mediaTrack.duration == null) {
                    empty = isLife(race.trackedRace);
                    return isLife(race.trackedRace);
                } else if (mediaTrackIsInTimerangeOf(race.trackedRace)) {
                    empty = false;
                    return true;
                }
                return false;
            }

            protected void addControlButtons(HorizontalPanel trackedRacesButtonPanel) {
            }

            protected void makeControlsReactToSelectionChange(List<RaceDTO> selectedRaces) {
            }

            protected void makeControlsReactToFillRegattas(List<RegattaDTO> regattas) {
            }
        };
        trackedRacesListComposite.ensureDebugId("TrackedRacesListComposite");
        regattaRefresher.fillRegattas();
        panel = new VerticalPanel();
        Grid formGrid = new Grid(2, 2);
        panel.add(formGrid);
        Label message = new Label();
        message.setText("Loading Regattas and Races");
        formGrid.setWidget(0, 0, message);    
        formGrid.setWidget(1, 1, trackedRacesListComposite);
        formGrid.getWidget(1, 1).setVisible(false);
        this.getOkButton().setVisible(false);

    }

    @Override
    protected Widget getAdditionalWidget() {
        return panel;

    }

    @Override
    protected Set<RegattaAndRaceIdentifier> getResult() {
        return getSelectedRegattasAndRaces();
    }

    @Override
    public void fillRegattas(List<RegattaDTO> result) {
        this.trackedRacesListComposite.fillRegattas(result);
        for (RegattaAndRaceIdentifier regattaAndRace : mediaTrack.regattasAndRaces) {
            this.trackedRacesListComposite.selectRaceByIdentifier(regattaAndRace);
        }
        updateUI();


    }

    public void updateUI() {
        Grid grid = (Grid) panel.getWidget(0);
        if (empty) {
            Label label = (Label)grid.getWidget(0, 0);
            label.setText("No Races available");
            grid.getWidget(0, 0).setVisible(true);
            grid.getWidget(1, 1).setVisible(false);
            this.getOkButton().setVisible(false);
            
        } else {
            grid.getWidget(0, 0).setVisible(false);
            grid.getWidget(1, 1).setVisible(true);
            this.getOkButton().setVisible(true);
        }
    }

    public Set<RegattaAndRaceIdentifier> getSelectedRegattasAndRaces() {
        List<RaceDTO> races = trackedRacesListComposite.getSelectedRaces();
        Set<RegattaAndRaceIdentifier> regattasAndRaces = new HashSet<RegattaAndRaceIdentifier>();
        for (RaceDTO race : races) {
            regattasAndRaces.add(race.getRaceIdentifier());
        }
        return regattasAndRaces;
    }

    private boolean mediaTrackIsInTimerangeOf(TrackedRaceDTO race) {
        if (race.endOfTracking != null && mediaTrack.beginsAfter(race.endOfTracking) || race.startOfTracking != null
                && mediaTrack.endsBefore(race.startOfTracking)) {
            return false;
        } else
            return true;

    }

    // noch brauchbar??
    // private boolean eventIsNotConnectedToAnyRaces(EventDTO event) {
    // if (regattasAreNotConnectedToAnyRaces(event.regattas)) {
    // return true;
    // }
    // return false;
    // }
    //
    // private boolean regattasAreNotConnectedToAnyRaces(List<RegattaDTO> regattas) {
    // for (RegattaDTO regatta : regattas) {
    // if (regattaIsConnectedToAnyRaces(regatta)) {
    // return false;
    // }
    // }
    // return true;
    // }
    //
    // private boolean regattaIsConnectedToAnyRaces(RegattaDTO regatta) {
    // if (regatta.races.size() > 0) {
    // return true;
    // }
    // return false;
    // }

    private boolean isLife(TrackedRaceDTO race) {
        if (race.endOfTracking == null)
            return true;
        else
            return false;
    }

}
