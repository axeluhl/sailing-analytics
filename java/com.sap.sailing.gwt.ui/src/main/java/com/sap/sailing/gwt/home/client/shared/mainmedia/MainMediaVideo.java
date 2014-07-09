package com.sap.sailing.gwt.home.client.shared.mainmedia;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class MainMediaVideo extends Composite {

    
    interface MainMediaVideoUiBinder extends UiBinder<Widget, MainMediaVideo> {
    }
    
    private static MainMediaVideoUiBinder uiBinder = GWT.create(MainMediaVideoUiBinder.class);

    private final static String DEFAULT_VIDEO_THUMB_IMAGE_URL = "http://static.sapsailing.com/ubilabsimages/default/default_video_preview.jpg"; 

    @UiField SpanElement videoTime;
    @UiField SpanElement videoTitle;
    
    @UiField ImageElement videoThumbImage;
    @UiField Element videoFile;
    
    // Format: {video.thumb},{video.file},{video.title},video;
    private String lightBoxData;
    
    public MainMediaVideo(EventDTO event) {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        String videoThumbImageUrl;
        if(event.getPhotoGalleryImageURLs().size() > 0) {
            videoThumbImageUrl = event.getPhotoGalleryImageURLs().get(0);
        } else {
            videoThumbImageUrl = DEFAULT_VIDEO_THUMB_IMAGE_URL;
        }
        videoThumbImage.setSrc(videoThumbImageUrl);
        lightBoxData = videoThumbImageUrl + "," + event.getVideoURLs().get(0) + ";";
        
        videoTime.setInnerHTML("3:40 min");
        videoTitle.setInnerHTML(event.getName());
        videoFile.setAttribute("data-src", event.getVideoURLs().get(0));
    }
    
    public String getDataForLightBox() {
        return lightBoxData;
    }

//    @UiHandler("eventMediaPageLink")
//    public void goToEventMediaPage(ClickEvent e) {
//        if(event.getBaseURL().contains("localhost") || event.getBaseURL().contains("127.0.0.1")) {
//            navigator.goToEvent(event.id.toString());
//        } else {
//            EventPlace eventPlace = new EventPlace(event.id.toString());
//            EventPlace.Tokenizer t = new Tokenizer();
//            String remoteEventUrl = event.getBaseURL() + "/gwt/Home.html#" + EventPlace.class.getSimpleName() + ":" + t.getToken(eventPlace);
//            Window.Location.replace(remoteEventUrl);
//        }
//    }

}
