package com.sap.sailing.gwt.ui.client.media;

import java.util.Date;
import java.util.Set;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.adminconsole.AssignRacesToMediaDialog;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.security.ui.client.UserService;

public class NewMediaWithRaceSelectionDialog extends NewMediaDialog {

    private SailingServiceAsync sailingService;

    private ErrorReporter errorReporter;

    private RegattaRefresher regattaRefresher;

    private Set<RegattasDisplayer> regattasDisplayers;

    private Widget listOfRacesForMedia;

    private AssignRacesToMediaDialog racesForMediaDialog;

    private UserService userService;

    public NewMediaWithRaceSelectionDialog(MediaServiceAsync mediaService, TimePoint defaultStartTime,
            StringMessages stringMessages, SailingServiceAsync sailingService, UserService userService,
            ErrorReporter errorReporter,
            RegattaRefresher regattaRefresher, Set<RegattasDisplayer> regattasDisplayers,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<MediaTrack> dialogCallback) {
        super(mediaService, defaultStartTime, stringMessages, null, dialogCallback);
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;
        this.regattasDisplayers = regattasDisplayers;
        this.userService = userService;
    }

    @Override
    protected void connectMediaWithRace() {
        mediaTrack.assignedRaces = racesForMediaDialog.getAssignedRaces();
    }

    private Widget racesForMedia() {
        racesForMediaDialog = new AssignRacesToMediaDialog(sailingService, userService, mediaTrack, errorReporter,
                regattaRefresher,
                stringMessages, null, new DialogCallback<Set<RegattaAndRaceIdentifier>>() {

                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(Set<RegattaAndRaceIdentifier> regattas) {
                        if (regattas.size() >= 0) {
                            mediaTrack.assignedRaces.clear();
                            mediaTrack.assignedRaces.addAll(regattas);
                        }
                    }
                });
        racesForMediaDialog.ensureDebugId("AssignedRacesDialog");
        racesForMediaDialog.hideRefreshButton();
        regattasDisplayers.add(racesForMediaDialog);

        return listOfRacesForMedia = racesForMediaDialog.getAdditionalWidget();
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel mainPanel = (VerticalPanel) super.getAdditionalWidget();
        mainPanel.add(racesForMedia());
        listOfRacesForMedia.setVisible(false);
        return mainPanel;
    }

    @Override
    protected void refreshUI() {
        super.refreshUI();
        try {
            Date value = startTimeBox.getValue();
            if (value != null) {
                regattaRefresher.fillRegattas();
                listOfRacesForMedia.setVisible(true);
            }
        } catch (Exception e) {
            listOfRacesForMedia.setVisible(false);
        }
    }
}
