package com.sap.sailing.gwt.home.client.shared.mainmedia;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.media.SailingFullscreenViewer;
import com.sap.sailing.gwt.home.desktop.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;
import com.sap.sailing.gwt.ui.shared.media.SailingVideoDTO;
import com.sap.sse.gwt.client.controls.carousel.ImageCarousel;

public class MainMedia extends Composite {

    private static final int MAX_VIDEO_COUNT = 3;

    @UiField
    HTMLPanel videosPanel;

    @UiField
    ImageCarousel<SailingImageDTO> imageCarousel;

    interface MainMediaUiBinder extends UiBinder<Widget, MainMedia> {
    }

    private static MainMediaUiBinder uiBinder = GWT.create(MainMediaUiBinder.class);

    public MainMedia(PlaceNavigator navigator) {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        imageCarousel.registerFullscreenViewer(new SailingFullscreenViewer());
    }

    public void setData(Collection<SailingVideoDTO> videos, ArrayList<SailingImageDTO> photos) {
        Iterator<SailingVideoDTO> videoIterator = videos.iterator();
        int videoCount = 0;
        while(videoCount < MAX_VIDEO_COUNT && videoIterator.hasNext()) {
            SailingVideoDTO videoDTO = videoIterator.next();
            MainMediaVideo video = new MainMediaVideo(videoDTO.getTitle(), videoDTO.getSourceRef(),
                    videoDTO.getMimeType());
            videosPanel.add(video);
            videoCount++;
        }
        for (SailingImageDTO image : photos) {
            imageCarousel.addImage(image);
        }
    }
}
