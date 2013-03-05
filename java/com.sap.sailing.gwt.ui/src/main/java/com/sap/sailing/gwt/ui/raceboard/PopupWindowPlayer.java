package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;
import com.sap.sailing.gwt.ui.shared.media.MediaTrack;

public abstract class PopupWindowPlayer extends AbstractMediaPlayer implements VideoPlayer {

    interface PopupCloseListener {
        void popupClosed();
    }

    private final JavaScriptObject playerWindow;
    private final PopupCloseListener popupCloseHandler;

    protected PopupWindowPlayer(MediaTrack mediaTrack, MediaEventHandler mediaEventHandler, PopupCloseListener popupCloseListener) {
        super(mediaTrack, mediaEventHandler);
        this.popupCloseHandler = popupCloseListener;
        
        String playerWindowUrl = getPlayerWindowUrl();  
        
        String codesvr = Window.Location.getParameter("gwt.codesvr");
        if (codesvr != null) {
            playerWindowUrl = playerWindowUrl + "&gwt.codesvr=" + codesvr;  
        }
        
        playerWindow = openWindow(playerWindowUrl);
        registerNativeStuff();
    }
    
    protected abstract String getPlayerWindowUrl();

    native JavaScriptObject openWindow(String url) /*-{
                return $wnd.open(url, '_blank', "resizable=yes, scrollbars=no,  toolbar=no,  menubar=no, status=no, location=no, directories=no, personalbar=no, width=660, height=380");
    }-*/;

    @Override
    public native void destroy() /*-{
            var playerWindow = this.@com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer::playerWindow;
            if (!playerWindow.closed) {
                playerWindow.close();
            }
    }-*/;

    native JavaScriptObject registerNativeStuff() /*-{
                var that = this;
                var window = that.@com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer::playerWindow; 
                window.onbeforeunload = function() {
                        that.@com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer::onClosingPopup()();
                }
                window.onbeforeunload = function() {
                        that.@com.sap.sailing.gwt.ui.raceboard.AbstractMediaPlayer::onMediaTimeUpdate()();
                }
                window.parent.deferredPlayState = {
                    isDeferredPlaying: false,
                    isDeferredMuted: true,
                    deferredMediaTime: 0,
                    playbackSpeed: 1
                };
    }-*/;

    private void onClosingPopup() {
        pause();
        popupCloseHandler.popupClosed();
    }

    @Override
    public native void play() /*-{
                var window = this.@com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer::playerWindow; 
                if (!window.parent.videoPlayer) {
                        window.parent.deferredPlayState.isDeferredPlaying = true;
                } else {
                        window.parent.videoPlayer.play();
                }

    }-*/;

    @Override
    public native void pause() /*-{
                var window = this.@com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer::playerWindow; 
                if (!window.parent.videoPlayer) {
                        window.parent.deferredPlayState.isDeferredPlaying = false;
                } else {
                        window.parent.videoPlayer.pause();
                }

    }-*/;

    @Override
    public native void setCurrentMediaTime(double mediaTime) /*-{
                var window = this.@com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer::playerWindow; 
                if (!window.parent.videoPlayer) {
                        window.parent.deferredPlayState.deferredMediaTime = mediaTime;
                } else {
                        window.parent.videoPlayer.setTime(mediaTime);
                }

    }-*/;

    @Override
    public native void setMuted(boolean muted) /*-{
                var window = this.@com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer::playerWindow; 
                if (!window.parent.videoPlayer) {
                        window.parent.deferredPlayState.isDeferredMuted = muted;
                } else {
                        window.parent.videoPlayer.setMuted(muted);
                }
    }-*/;

    @Override
    public native boolean isPaused() /*-{
                var window = this.@com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer::playerWindow;
                if (!window.parent.videoPlayer) {
                        return !window.parent.deferredPlayState.isDeferredPlaying;
                } else {
                        return window.parent.videoPlayer.isPaused();
                }
    }-*/;

    @Override
    public native double getDuration() /*-{
                var window = this.@com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer::playerWindow;
                if (!window.parent.videoPlayer) {
                        return NaN;
                } else {
                        return window.parent.videoPlayer.getDuration();
                }
    }-*/;

    @Override
    public native double getCurrentMediaTime() /*-{
                var window = this.@com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer::playerWindow;
                if (!window.parent.videoPlayer) {
                        return window.parent.deferredPlayState.deferredMediaTime;
                } else {
                        return window.parent.videoPlayer.getTime();
                }
    }-*/;

    @Override
    public native void setPlaybackSpeed(double playbackSpeed) /*-{
                var window = this.@com.sap.sailing.gwt.ui.raceboard.PopupWindowPlayer::playerWindow;
                if (!window.parent.videoPlayer) {
                        window.parent.deferredPlayState.deferredPlaybackSpeed = playbackSpeed;
                } else {
                        window.parent.videoPlayer.setPlaybackSpeed(playbackSpeed);
                }
    }-*/;

}
