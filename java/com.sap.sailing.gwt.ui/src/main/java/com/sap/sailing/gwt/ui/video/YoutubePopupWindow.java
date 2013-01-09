package com.sap.sailing.gwt.ui.video;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class YoutubePopupWindow implements EntryPoint {

        private static final String VIDEO_CONTAINER_ID = "videoContainer";
        
        private JavaScriptObject youtubePlayer;

        @Override
        public void onModuleLoad() {
                String title = Window.Location.getParameter("title");
                Window.setTitle(title);
                
                RootLayoutPanel.get().getElement().setId(VIDEO_CONTAINER_ID);

                String videoUrl = Window.Location.getParameter("id");
                if (videoUrl != null) {
                    loadYoutube(videoUrl, VIDEO_CONTAINER_ID);
                }
        }
        
        //inspired by https://developers.google.com/youtube/iframe_api_reference#Examples
        private native void loadYoutube(String videoUrl, String videoContainerId) /*-{

                var that = this;
                
                var tag = $doc.createElement('script');

                // This is a protocol-relative URL as described here:
                //     http://paulirish.com/2010/the-protocol-relative-url/
                // If you're testing a local page accessed via a file:/// URL, please set tag.src to
                //     "https://www.youtube.com/iframe_api" instead.
                tag.src = "//www.youtube.com/iframe_api";
                var firstScriptTag = $doc.getElementsByTagName('script')[0];
                firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

                // This function creates an <iframe> containing a YouTube player after the API code downloads.
                var player;
                $wnd.onYouTubeIframeAPIReady = function() {
                        var player = new $wnd.YT.Player(videoContainerId, {
                                videoId : videoUrl,
                                playerVars : {
                                        'autoplay' : 0,
                                        'controls' : 0
                                }
                        });
                        that.@com.sap.sailing.gwt.ui.video.YoutubePopupWindow::setYoutubePlayer(Lcom/google/gwt/core/client/JavaScriptObject;)(player);
                }

        }-*/;
        
        public void setYoutubePlayer(JavaScriptObject youtubePlayer) {
                this.youtubePlayer = youtubePlayer;
                adjustWindowSize();
        }

        private void adjustWindowSize() {
                RootPanel videoContainer = RootPanel.get(VIDEO_CONTAINER_ID);
                int clientWidth = Window.getClientWidth();
                int videoWidth = videoContainer.getOffsetWidth();
                int widthDelta = videoWidth - clientWidth;
                
                int clientHeight = Window.getClientHeight();
                int videoHeight = videoContainer.getOffsetHeight();
                int heightDelta = videoHeight - clientHeight;
                
                Window.resizeBy(widthDelta, heightDelta);
        }

}
