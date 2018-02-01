package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.dto.TrackedRaceDTO;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class AssignRacesToMediaDialog extends DataEntryDialog<Set<RegattaAndRaceIdentifier>> implements RegattasDisplayer {

    protected StringMessages stringMessages;
    protected final TrackedRacesListComposite trackedRacesListComposite;
    protected final MediaTrack mediaTrack;
    public boolean hasRaceCandidates = false;
    public boolean started = false;
    private final VerticalPanel panel;
    private Button btnRefresh;

    public AssignRacesToMediaDialog(SailingServiceAsync sailingService, final MediaTrack mediaTrack,
            ErrorReporter errorReporter, RegattaRefresher regattaRefresher, StringMessages stringMessages,
            Validator<Set<RegattaAndRaceIdentifier>> validator, DialogCallback<Set<RegattaAndRaceIdentifier>> callback) {
        super(stringMessages.linkedRaces(), stringMessages.selectFromRacesWithOverlappingTimeRange(),
                stringMessages.ok(), stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        this.mediaTrack = mediaTrack;
        trackedRacesListComposite = new TrackedRacesListComposite(null, null, sailingService, errorReporter,
                regattaRefresher,
                stringMessages, /* multiselection */true, /* actionButtonsEnabled */ false) {
            @Override
            protected boolean raceIsToBeAddedToList(RaceDTO race) {
                if (mediaTrackIsInTimerangeOf(race.trackedRace)) {
                    hasRaceCandidates = true;
                    return true;
                }
                return false;
            }

            @Override
            protected void addControlButtons(HorizontalPanel trackedRacesButtonPanel) {
                btnRefresh = (Button)trackedRacesButtonPanel.getWidget(0);
            }
            
            @Override
            protected void makeControlsReactToFillRegattas(Iterable<RegattaDTO> regattas) {
            }
        };
        trackedRacesListComposite.ensureDebugId("TrackedRacesListComposite");
        regattaRefresher.fillRegattas();
        panel = new VerticalPanel();
        Grid formGrid = new Grid(2, 2);
        panel.add(formGrid);
        Label message = new Label();
        message.setText(stringMessages.loadingRegattasAndRaces());
        formGrid.setWidget(0, 0, message);    
        formGrid.setWidget(1, 1, trackedRacesListComposite);
        formGrid.getWidget(1, 1).setVisible(false);
        this.getOkButton().setVisible(false);

    }

    @Override
    public Widget getAdditionalWidget() {
        return panel;
    }
    
    @Override
    protected Set<RegattaAndRaceIdentifier> getResult() {
        return getAssignedRaces();
    }

    @Override
    public void fillRegattas(Iterable<RegattaDTO> result) {
        hasRaceCandidates = false;
        this.trackedRacesListComposite.fillRegattas(result);
        for (RegattaAndRaceIdentifier assignedRace : mediaTrack.assignedRaces) {
            this.trackedRacesListComposite.selectRaceByIdentifier(assignedRace);
        }
        updateUI();
    }

    private void updateUI() {
        Grid grid = (Grid) panel.getWidget(0);
        if (hasRaceCandidates) {
            grid.getWidget(0, 0).setVisible(false);
            grid.getWidget(1, 1).setVisible(true);
            this.getOkButton().setVisible(true);
        } else {
            Label label = (Label) grid.getWidget(0, 0);
            label.setText(stringMessages.noRacesAvailable());
            grid.getWidget(0, 0).setVisible(true);
            grid.getWidget(1, 1).setVisible(false);
            this.getOkButton().setVisible(false);
        }
    }

    public Set<RegattaAndRaceIdentifier> getAssignedRaces() {
        Set<RaceDTO> races = trackedRacesListComposite.getSelectionModel().getSelectedSet();
        Set<RegattaAndRaceIdentifier> assignedRaces = new HashSet<RegattaAndRaceIdentifier>();
        for (RaceDTO race : races) {
            assignedRaces.add(race.getRaceIdentifier());
        }
        return assignedRaces;
    }
    
    public void hideRefreshButton(){
        btnRefresh.setVisible(false);
    }

    private boolean mediaTrackIsInTimerangeOf(TrackedRaceDTO race) {
        if ((race.endOfTracking != null && mediaTrack.beginsAfter(race.endOfTracking)) || (race.startOfTracking != null
                && mediaTrack.endsBefore(race.startOfTracking))) {
            return false;
        } else
            return true;

    }

}
