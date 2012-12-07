package com.sap.sailing.gwt.ui.raceboard;

import java.util.Date;

import com.google.gwt.dom.client.MediaElement;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.MediaPlayer;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class VideoPopup extends DialogBox implements MediaPlayer {

    private static final int TOLERATED_LAG_IN_MILLISECONDS = 2000;
    private Video videoControl;
    private MediaTrack videoTrack;

    public VideoPopup() {
        super(false, false);
        VerticalPanel popUpPanelContents = new VerticalPanel();
        SimplePanel videoHolder = new SimplePanel();
        videoControl = Video.createIfSupported();
        if (videoControl != null) {
            videoControl.setControls(true);
            videoControl.setAutoplay(false);
            videoHolder.add(videoControl);

            // HTML videoFrame = new
            // HTML("<iframe class=\"youtube-player\" type=\"text/html\" width=\"640\" height=\"385\" src=\"http://www.youtube.com/embed/dP15zlyra3c?html5=1\" frameborder=\"0\"></iframe>");
            //
            // SimplePanel videoFrameHolder = new SimplePanel();
            // videoFrameHolder.add(videoFrame);

            popUpPanelContents.add(videoHolder);
            // popUpPanelContents.add(videoFrameHolder);
            setWidget(popUpPanelContents);
        }
    }

    public void setMediaTrack(MediaTrack videoTrack) {
        this.videoTrack = videoTrack;
        setText(this.videoTrack.title);
        if (videoControl != null) {
            videoControl.setSrc(this.videoTrack.url);
        }
    }

    public void alignTime(Date raceTime) {
        if (videoControl != null) {
            switch (videoControl.getReadyState()) {
            case MediaElement.HAVE_NOTHING:
                pause();
                break;
            default:
                long videoStartTimeInMillis = videoTrack.startTime.getTime();
                long videoTimeInMillis = videoStartTimeInMillis + Math.round(videoControl.getCurrentTime() * 1000);
                long raceTimeInMillis = raceTime.getTime();
                long videoLaggingBehindRaceInMillis = raceTimeInMillis - videoTimeInMillis;
                if (Math.abs(videoLaggingBehindRaceInMillis) > TOLERATED_LAG_IN_MILLISECONDS) {
                    double videoTime = (raceTimeInMillis - videoStartTimeInMillis) / 1000;
                    if (videoTime < 0) {
                        pause();
                    } else if (videoTime > videoControl.getDuration()) {
                        pause();
                    } else {
                        videoControl.setCurrentTime(videoTime);
                    }
                }
            }
        }
    }

    @Override
    public boolean isPaused() {
        return (videoControl == null) || videoControl.isPaused();
    }

    @Override
    public void pause() {
        if (videoControl != null) {
            videoControl.pause();
        }
    }

    @Override
    public void play() {
        if (videoControl != null) {
            videoControl.play();
        }
    }
}
