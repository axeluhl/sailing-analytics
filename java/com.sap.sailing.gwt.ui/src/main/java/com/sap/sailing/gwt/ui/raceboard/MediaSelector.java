package com.sap.sailing.gwt.ui.raceboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.MediaPlayer;
import com.sap.sailing.gwt.ui.client.PlayStateListener;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.Timer.PlayStates;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class MediaSelector implements RaceTimesInfoProviderListener, PlayStateListener, TimeListener,
        AsyncCallback<Collection<MediaTrack>> {

    private final CheckBox toggleVideoPanel;
    private final Collection<MediaPlayer> activePlayers = new ArrayList<MediaPlayer>();
    private final Collection<MediaTrack> audioTracks = new ArrayList<MediaTrack>();
    private final Collection<MediaTrack> videoTracks = new ArrayList<MediaTrack>();
    private final ErrorReporter errorReporter;

    public MediaSelector(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;

        final VideoPopup videoPopup = new VideoPopup();
        activePlayers.add(videoPopup);

        toggleVideoPanel = new CheckBox("Show Video");
        toggleVideoPanel.setEnabled(false);
        toggleVideoPanel.addStyleName("raceBoardNavigation-innerElement");
        toggleVideoPanel.getElement().getStyle().setFloat(Style.Float.LEFT);
        toggleVideoPanel.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                if (toggleVideoPanel.getValue()) {
                    videoPopup.show();
                } else {
                    videoPopup.hide();
                }

            }
        });
    }

    @Override
    public void playStateChanged(PlayStates playState, PlayModes playMode) {
        if (PlayModes.Replay.equals(playMode)) {
            switch (playState) {
            case Playing:
                startPlaying();
                break;
            case Paused:
            case Stopped:
                pausePlaying();
            default:
                break;
            }
        } else {
            // TODO
        }
    }

    private void pausePlaying() {
        for (MediaPlayer player : activePlayers) {
            if (!player.isPaused()) {
                player.pause();
            }
        }
    }

    private void startPlaying() {
        for (MediaPlayer player : activePlayers) {
            if (player.isPaused()) {
                player.play();
            }
        }
    }

    @Override
    public void timeChanged(Date raceTime) {
        for (MediaPlayer player : activePlayers) {
            player.alignTime(raceTime);
        }
    }

    public Widget widget() {
        return toggleVideoPanel;
    }

    @Override
    public void onFailure(Throwable caught) {
        errorReporter
                .reportError("Remote Procedure Call getPreviousConfigurations() - Failure: " + caught.getMessage());
    }

    @Override
    public void onSuccess(Collection<MediaTrack> mediaTracks) {
        toggleVideoPanel.setEnabled(mediaTracks.size() > 0);
        for (MediaTrack mediaTrack : mediaTracks) {
            switch (mediaTrack.type) {
            case AUDIO:
                audioTracks.add(mediaTrack);
                break;
            case VIDEO:
                videoTracks.add(mediaTrack);
                activePlayers.iterator().next().setMediaTrack(mediaTrack);
                break;
            }
        }
    }

    @Override
    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo) {
        // TODO Auto-generated method stub
        
    }

}
