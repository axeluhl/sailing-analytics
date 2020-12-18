package com.sap.sailing.gwt.ui.client.media;

import java.util.Date;
import java.util.Set;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.adminconsole.AssignRacesToMediaDialog;
import com.sap.sailing.gwt.ui.adminconsole.FileStorageServiceConnectionTestObservable;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.client.MediaServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.TimePoint;

public class NewMediaWithRaceSelectionDialog extends NewMediaDialog {

    private Widget listOfRacesForMedia;

    private AssignRacesToMediaDialog racesForMediaDialog;

    private final Presenter presenter;

    public NewMediaWithRaceSelectionDialog(MediaServiceAsync mediaService, TimePoint defaultStartTime,
            StringMessages stringMessages, Presenter presenter,
            FileStorageServiceConnectionTestObservable storageServiceConnection,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<MediaTrack> dialogCallback) {
        super(mediaService, defaultStartTime, stringMessages, null, storageServiceConnection, dialogCallback);
        this.presenter = presenter;
    }

    @Override
    protected void connectMediaWithRace() {
        mediaTrack.assignedRaces = racesForMediaDialog.getAssignedRaces();
    }

    private Widget racesForMedia() {
        racesForMediaDialog = new AssignRacesToMediaDialog(presenter, mediaTrack, stringMessages, null, new DialogCallback<Set<RegattaAndRaceIdentifier>>() {
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
        presenter.getRegattasRefresher().addDisplayerAndCallFillOnInit(racesForMediaDialog.getRegattasDisplayer());
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
                presenter.getRegattasRefresher().reloadAndCallFillAll();
                listOfRacesForMedia.setVisible(true);
            }
        } catch (Exception e) {
            listOfRacesForMedia.setVisible(false);
        }
    }
}
