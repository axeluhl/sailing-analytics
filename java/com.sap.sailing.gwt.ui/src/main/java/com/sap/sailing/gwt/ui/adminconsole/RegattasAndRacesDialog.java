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
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class RegattasAndRacesDialog extends DataEntryDialog<Set<RegattaAndRaceIdentifier>> implements RegattasDisplayer {

    protected StringMessages stringMessages;
    protected final TrackedRacesListComposite trackedRacesListComposite;
    protected final MediaTrack mediaTrack;
    public boolean empty = true;
    public boolean started = false;
    protected List<EventDTO> existingEvents;

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
                    empty = isLife(race);
                    return isLife(race);
                } else if (mediaTrackIsInTimerangeOf(race)) {
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

    }

    @Override
    protected Widget getAdditionalWidget() {

        final VerticalPanel panel = new VerticalPanel();
        Widget additionalWidget = super.getAdditionalWidget();
        if (additionalWidget != null) {

            panel.add(additionalWidget);
        }
        panel.clear();
        Grid formGrid = new Grid(1, 1);
        panel.add(formGrid);
        if (this.empty) {
            Label message = new Label();
            message.setText("No Races available");
            formGrid.setWidget(0, 0, message);
            this.getCancelButton().setVisible(false);
        } else {

            formGrid.setWidget(0, 0, trackedRacesListComposite);
        }
        return panel;

    }

    @Override
    protected Set<RegattaAndRaceIdentifier> getResult() {
        return getSelectedRegattasAndRaces();
    }

    @Override
    public void fillRegattas(List<RegattaDTO> result) {
        this.trackedRacesListComposite.fillRegattas(result);
        if (!started) {
            started = true;
            this.show();
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

    private boolean mediaTrackIsInTimerangeOf(RaceDTO race) {
        if (race.endOfRace != null && mediaTrack.beginsAfter(race.endOfRace) || race.startOfRace != null
                && mediaTrack.endsBefore(race.startOfRace)) {
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

    private boolean isLife(RaceDTO race) {
        if (race.endOfRace == null)
            return true;
        else
            return false;
    }

}
