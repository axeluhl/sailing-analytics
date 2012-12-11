package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.media.client.Video;
import com.google.gwt.user.client.ui.DialogBox;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public class VideoPopup extends AbstractMediaPlayer {

    private final DialogBox dialogBox; 

    public VideoPopup() {
        super(Video.createIfSupported());
        this.dialogBox = new DialogBox(false, false);
        if (mediaControl != null) {

            // HTML videoFrame = new
            // HTML("<iframe class=\"youtube-player\" type=\"text/html\" width=\"640\" height=\"385\" src=\"http://www.youtube.com/embed/dP15zlyra3c?html5=1\" frameborder=\"0\"></iframe>");
            //
            // SimplePanel videoFrameHolder = new SimplePanel();
            // videoFrameHolder.add(videoFrame);

            dialogBox.setWidget(mediaControl);
        }
    }
    
    public void show() {
        dialogBox.show();
    }
    
    public void hide() {
        dialogBox.hide();
    }

    @Override
    public void setMediaTrack(MediaTrack videoTrack) {
        super.setMediaTrack(videoTrack);
        dialogBox.setText(videoTrack.title);        
        dialogBox.setTitle(videoTrack.toString());        
    }
    
}
