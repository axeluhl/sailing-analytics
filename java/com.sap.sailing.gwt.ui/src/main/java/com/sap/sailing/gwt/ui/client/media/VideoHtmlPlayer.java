package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.media.client.MediaBase;
import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.media.shared.VideoSynchPlayer;
import com.sap.sailing.gwt.ui.client.media.shared.WithWidget;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.gwt.client.player.Timer;

public class VideoHtmlPlayer extends AbstractHtmlMediaPlayer implements VideoSynchPlayer, MediaSynchAdapter, WithWidget {

    private final TimePoint raceStartTime;
    private final Timer raceTimer;
    private EditFlag editFlag;
    
    public VideoHtmlPlayer(final MediaTrack videoTrack, TimePoint raceStartTime, boolean showSynchControls, Timer raceTimer) {
        super(videoTrack);
        this.raceTimer = raceTimer;
        this.raceStartTime = raceStartTime;
    }
    
    @Override
    protected MediaBase createMediaElement() {
        return Video.createIfSupported();
    }

    @Override
    public long getOffset() {
        return getMediaTrack().startTime.asMillis() - raceStartTime.asMillis();
    }

    @Override
    public void changeOffsetBy(long delta) {
        getMediaTrack().startTime = getMediaTrack().startTime.plus(delta);
        forceAlign();
    }

    @Override
    public void updateOffset() {
        getMediaTrack().startTime = new MillisecondsTimePoint(raceTimer.getTime().getTime() - getCurrentMediaTimeMillis());
    }

    @Override
    public void setControlsVisible(boolean isVisible) {
        mediaElement.setControls(isVisible);
    }

    @Override
    public void pauseRace() {
        raceTimer.pause();
    }
    
    @Override
    public void playMedia() {
        if (!isEditing()) {
            super.playMedia();
        }
    }
    
    @Override
    public void pauseMedia() {
        if (!isEditing()) {
            super.pauseMedia();
        }
    }

    @Override
    protected void alignTime() {
        if (!isEditing()) {
            super.alignTime();
        } 
    }

    @Override
    public Widget getWidget() {
        return mediaElement;
    }

    private boolean isEditing() {
        return editFlag != null && editFlag.isEditing();
    }
    
    public void setEditFlag(EditFlag editFlag) {
        this.editFlag = editFlag;
    }
    
}
