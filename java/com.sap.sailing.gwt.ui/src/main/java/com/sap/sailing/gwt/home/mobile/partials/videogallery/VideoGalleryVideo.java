package com.sap.sailing.gwt.home.mobile.partials.videogallery;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.shared.SharedHomeResources;
import com.sap.sailing.gwt.home.shared.partials.videoplayer.VideoPlayer;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sse.gwt.client.media.VideoDTO;

public class VideoGalleryVideo extends Composite {

    private static VideoGalleryVideoUiBinder uiBinder = GWT.create(VideoGalleryVideoUiBinder.class);
    
    private boolean managed;
    
    interface VideoGalleryVideoUiBinder extends UiBinder<Widget, VideoGalleryVideo> {
    }
    
    @UiField(provided = true) VideoPlayer videoPlayerUi = new VideoPlayer(true, false);
    @UiField DivElement videoTitleUi;
    @UiField DivElement videoCreateDateUi;
    @UiField DivElement overlayUi;
    @UiField Button editButtonUi;
    @UiField Button deleteButtonUi;
    
    public VideoGalleryVideo(VideoDTO video) {
        initWidget(uiBinder.createAndBindUi(this));
        SharedHomeResources.INSTANCE.sharedHomeCss().ensureInjected();
        videoPlayerUi.setVideo(video);
        setTextOrRemove(videoTitleUi, video.getTitle());
        setTextOrRemove(videoCreateDateUi, DateAndTimeFormatterUtil.formatDateAndTime(video.getCreatedAtDate()));
        overlayUi.getStyle().setVisibility(Visibility.HIDDEN);
        editButtonUi.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
            }
        });
        deleteButtonUi.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
            }
        });
    }
    
    private void setTextOrRemove(DivElement element, String text) {
        if (text == null || text.isEmpty()) {
            element.removeFromParent();
        } else {
            element.setInnerText(text);
        }
    }
    
    public void manageMedia(boolean manage) {
        this.managed = manage;
        if (this.managed) {
            overlayUi.getStyle().setVisibility(Visibility.VISIBLE);            
        } else {
            overlayUi.getStyle().setVisibility(Visibility.HIDDEN);
        }
    }
}
