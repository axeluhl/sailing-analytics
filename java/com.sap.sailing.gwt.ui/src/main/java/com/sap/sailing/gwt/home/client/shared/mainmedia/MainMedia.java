package com.sap.sailing.gwt.home.client.shared.mainmedia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class MainMedia extends Composite {
    @UiField HTMLPanel videosPanel;
    @UiField DivElement videoLightBoxData;

    @UiField DivElement photoUrlsData;
    @UiField DivElement currentPhotoUrl;
    
    interface MainMediaUiBinder extends UiBinder<Widget, MainMedia> {
    }
    
    private static MainMediaUiBinder uiBinder = GWT.create(MainMediaUiBinder.class);

    public MainMedia(PlaceNavigator navigator) {
        
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
    }

    public void setRecentEvents(List<EventDTO> recentEvents) {
        List<String> photoGalleryUrls = new ArrayList<String>();
        String lightBoxData = "";
        
        int videoCounter = 0;
        for(EventDTO event: recentEvents) {
            photoGalleryUrls.addAll(event.getPhotoGalleryImageURLs());
            
            if(event.getVideoURLs().size() > 0 && videoCounter < 3) {
                MainMediaVideo video = new MainMediaVideo(event);
                lightBoxData += video.getDataForLightBox();  
                videosPanel.add(video);
                videoCounter++;
            }
        }

        videoLightBoxData.setInnerText(lightBoxData);
        
        // shuffle the image url list (Remark: Collections.shuffle() is not implemented in GWT)
        int gallerySize = photoGalleryUrls.size();
        Random random = new Random(gallerySize);  
        for(int i = 0; i < gallerySize; i++) {  
            Collections.swap(photoGalleryUrls, i, random.nextInt(gallerySize));  
        }

        currentPhotoUrl.setAttribute("data-url", "#1");
        String imagePath = "url(" + photoGalleryUrls.get(0) + ")";
        currentPhotoUrl.getStyle().setBackgroundImage(imagePath);
        
        String slideShowData = "";
        for(int i = 0; i < gallerySize; i++) {
            slideShowData += photoGalleryUrls.get(0) + ",#" + (i+1) + ";";
        }
        photoUrlsData.setInnerText(slideShowData);
    }
}
