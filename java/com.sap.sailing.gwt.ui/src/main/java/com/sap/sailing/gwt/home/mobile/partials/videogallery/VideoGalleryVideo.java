package com.sap.sailing.gwt.home.mobile.partials.videogallery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.partials.videoplayer.VideoPlayer;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sse.gwt.client.media.VideoDTO;

public class VideoGalleryVideo extends Composite {

    private static VideoGalleryVideoUiBinder uiBinder = GWT.create(VideoGalleryVideoUiBinder.class);
    
    interface VideoGalleryVideoUiBinder extends UiBinder<Widget, VideoGalleryVideo> {
    }
    
    @UiField(provided = true) VideoPlayer videoPlayerUi = new VideoPlayer(true, false);
    @UiField DivElement videoTitleUi;
    @UiField DivElement videoCreateDateUi;
    
    public VideoGalleryVideo(VideoDTO video) {
        initWidget(uiBinder.createAndBindUi(this));
        videoPlayerUi.setVideo(video);
        setTextOrRemove(videoTitleUi, video.getTitle());
        setTextOrRemove(videoCreateDateUi, DateAndTimeFormatterUtil.formatDateAndTime(video.getCreatedAtDate()));
    }
    
    private void setTextOrRemove(DivElement element, String text) {
        if (text == null || text.isEmpty()) {
            element.removeFromParent();
        } else {
            element.setInnerText(text);
        }
    }
}
