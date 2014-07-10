package com.sap.sailing.gwt.home.client.shared.mainmedia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.idangerous.Swiper;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class MainMedia extends Composite {
    
    private static final MainMediaResources.LocalCss STYLES = MainMediaResources.INSTANCE.css(); 

    @UiField HTMLPanel videosPanel;
    @UiField DivElement videoLightBoxData;
    
    @UiField HTMLPanel mediaSlides;

    private Swiper swiper;
    
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

        for(String url : photoGalleryUrls) {
            Image imageSlide = new Image(url);
            imageSlide.addStyleName(STYLES.media_photo());
            imageSlide.addStyleName(STYLES.media_swiperslide());
            mediaSlides.add(imageSlide);
        }
        
        this.swiper = Swiper.createWithDefaultOptions(STYLES.media_swipecontainer(), STYLES.media_swipewrapper(), STYLES.media_swiperslide());

    }

    @UiHandler("nextPictureLink")
    public void nextStageTeaserLinkClicked(ClickEvent e) {
        if (this.swiper != null) {
            this.swiper.swipeNext();
        }
    }

    @UiHandler("prevPictureLink")
    public void prevStageTeaserLinkClicked(ClickEvent e) {
        if (this.swiper != null) {
            this.swiper.swipePrev();
        }
    }

}
