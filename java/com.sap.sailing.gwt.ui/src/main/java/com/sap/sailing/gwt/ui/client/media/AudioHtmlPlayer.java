package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.MediaBase;
import com.sap.sailing.domain.common.media.MediaTrack;
import com.sap.sailing.gwt.ui.client.media.shared.AudioPlayer;

public class AudioHtmlPlayer extends AbstractHtmlMediaPlayer implements AudioPlayer {
    
    public AudioHtmlPlayer(MediaTrack mediaTrack) {
        super(mediaTrack);
    }

    @Override
    protected MediaBase createMediaElement() {
        return Audio.createIfSupported();
    }

}
