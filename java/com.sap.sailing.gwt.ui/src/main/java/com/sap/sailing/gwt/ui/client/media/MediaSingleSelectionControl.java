package com.sap.sailing.gwt.ui.client.media;

import java.util.List;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sse.gwt.client.useragent.UserAgentDetails.AgentTypes;

public class MediaSingleSelectionControl extends AbstractMediaSelectionControl {

    private final ListBox selectMedia = new ListBox();
    private List<MediaTrack> videoTracks;

    public MediaSingleSelectionControl(MediaPlayerManager mediaPlayerManager, AgentTypes userAgent) {
        super(mediaPlayerManager, userAgent);
        updateUi();
        selectMedia.addChangeHandler(new ChangeHandler() {
            
            @Override
            public void onChange(ChangeEvent event) {
                int selectedIndex = selectMedia.getSelectedIndex();
                if (selectedIndex >= 0) {
                    MediaSingleSelectionControl.this.mediaPlayerManager.playDockedVideo(videoTracks.get(selectedIndex));
                } else {
                    MediaSingleSelectionControl.this.mediaPlayerManager.closeDockedVideo();
                }
            }
        });
    }

    private void setWidgetsVisible(boolean isVisible) {
        selectMedia.setVisible(isVisible);
    }

    protected void updateUi() {
        videoTracks = mediaPlayerManager.getVideoTracks();
        setWidgetsVisible(videoTracks.size() > 1);
        selectMedia.clear();
        if (mediaPlayerManager.getVideoTracks().size() > 1) {
            int playingVideoIndex = -1;
            for (int i = 0; i < videoTracks.size(); i++) {
                MediaTrack videoTrack = videoTracks.get(i);
                selectMedia.addItem(videoTrack.title);
                if (videoTrack == mediaPlayerManager.getDockedVideoTrack()) {
                    playingVideoIndex = i;
                }
            }
            selectMedia.setSelectedIndex(playingVideoIndex);
        }
    }

    public Widget widget() {
        return selectMedia;
    }

}
