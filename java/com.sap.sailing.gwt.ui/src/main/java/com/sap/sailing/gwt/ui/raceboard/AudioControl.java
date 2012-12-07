package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.media.client.Audio;

public class AudioControl extends AbstractMediaPlayer {

    public AudioControl() {
        super(Audio.createIfSupported());
    }

}
