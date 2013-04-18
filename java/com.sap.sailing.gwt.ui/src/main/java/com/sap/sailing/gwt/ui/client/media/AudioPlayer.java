package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.media.client.Audio;
import com.google.gwt.media.client.MediaBase;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class AudioPlayer extends AbstractEmbeddedMediaPlayer {
    
    public AudioPlayer(MediaTrack mediaTrack, MediaEventHandler mediaEventHandler) {
        super(mediaTrack, mediaEventHandler);
    }

    @Override
    protected MediaBase createMediaControl() {
        return Audio.createIfSupported();
    }
    
}
