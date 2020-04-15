package com.sap.sailing.gwt.ui.client.media;

import com.sap.sailing.gwt.ui.client.media.shared.MediaPlayer;

/**
 * Interface to decouple container lifecycle from VideoPlayer functionality.
 * @author D047974
 *
 */
public interface MediaPlayerContainer {

    /**
     * Terminate all playing, release resources and make the container disappear from the UI.
     */
    void shutDown();
    
    MediaPlayer getMediaPlayer();

}
