package com.sap.sailing.gwt.home.client.shared.mainmedia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.media.ImageReferenceDTO;
import com.sap.sailing.gwt.ui.shared.media.VideoMetadataDTO;
import com.sap.sse.gwt.client.controls.carousel.ImageCarousel;

public class MainMedia extends Composite {

    private static final int MAX_VIDEO_COUNT = 3;

    @UiField
    HTMLPanel videosPanel;

    @UiField
    DivElement videoLightBoxData;

    @UiField
    ImageCarousel imageCarousel;

    interface MainMediaUiBinder extends UiBinder<Widget, MainMedia> {
    }

    private static MainMediaUiBinder uiBinder = GWT.create(MainMediaUiBinder.class);

    public MainMedia(PlaceNavigator navigator) {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }

    public void setData(Collection<VideoMetadataDTO> videos, ArrayList<ImageReferenceDTO> photos) {
        Iterator<VideoMetadataDTO> videoIterator = videos.iterator();
        int videoCount = 0;
        while(videoCount < MAX_VIDEO_COUNT && videoIterator.hasNext()) {
            VideoMetadataDTO videoDTO = videoIterator.next();
            MainMediaVideo video = new MainMediaVideo(videoDTO.getTitle(), videoDTO.getRef());
            videosPanel.add(video);
            videoCount++;
        }
        
        for (ImageReferenceDTO image : photos) {
            imageCarousel.addImage(image.getImageURL(), image.getHeightInPx(), image.getWidthInPx());
        }
    }
}
