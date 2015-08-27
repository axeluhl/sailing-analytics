package com.sap.sailing.gwt.home.mobile.partials.videogallery;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.mobile.partials.section.MobileSection;
import com.sap.sailing.gwt.home.mobile.partials.sectionHeader.SectionHeaderContent;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.media.VideoDTO;

public class VideoGallery extends Composite {

    private static VideoGalleryUiBinder uiBinder = GWT.create(VideoGalleryUiBinder.class);
    
    interface VideoGalleryUiBinder extends UiBinder<MobileSection, VideoGallery> {
    }
    
    @UiField SectionHeaderContent sectionHeaderUi;
    private final MobileSection mobileSection;
    
    public VideoGallery() {
        VideoGalleryResources.INSTANCE.css().ensureInjected();
        initWidget(mobileSection = uiBinder.createAndBindUi(this));
        sectionHeaderUi.setSectionTitle(StringMessages.INSTANCE.videos());
    }
    
    public void setVideos(Collection<VideoDTO> videos) {
        sectionHeaderUi.setInfoText(StringMessages.INSTANCE.videosCount(videos.size()));
        mobileSection.clearContent();
        if (videos.isEmpty()) {
            mobileSection.addContent(getNoVideosInfoWidget()); 
        } else {
            for (VideoDTO video : videos) {
                mobileSection.addContent(new VideoGalleryVideo(video));
            }
        }
    }
    
    private Widget getNoVideosInfoWidget() {
        Label label = new Label(StringMessages.INSTANCE.noVideos());
        label.getElement().getStyle().setPadding(1, Unit.EM);
        label.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        label.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
        return label;
    }

}
