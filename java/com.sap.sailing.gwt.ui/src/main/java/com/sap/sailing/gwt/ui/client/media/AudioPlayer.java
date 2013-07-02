package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.MediaBase;
import com.sap.sailing.domain.common.media.MediaTrack;

public class AudioPlayer extends AbstractEmbeddedMediaPlayer {
    
    public AudioPlayer(MediaTrack mediaTrack) {
        super(mediaTrack);
    }

    @Override
    protected MediaBase createMediaControl() {
        return Audio.createIfSupported();
    }

}
