package com.sap.sailing.gwt.home.client.shared.mainmedia;

import java.util.List;

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
    @UiField HTMLPanel videoThumbsPanel;
    @UiField HTMLPanel videoPanel;
    @UiField HTMLPanel photoThumbsPanel;

    @UiField DivElement photoUrl;
    
    interface MainMediaUiBinder extends UiBinder<Widget, MainMedia> {
    }
    
    private static MainMediaUiBinder uiBinder = GWT.create(MainMediaUiBinder.class);

    public MainMedia(PlaceNavigator navigator) {
        
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
    }

    public void setRecentEvents(List<EventDTO> recentEvents) {
        int videoCounter = 0;
        for(EventDTO event: recentEvents) {
            if(event.getVideoURLs().size() > 0) {
                MainMediaVideoThumb videoThumb = new MainMediaVideoThumb();
                if(videoCounter <= 3) {
                    videoThumbsPanel.add(videoThumb);
                    videoCounter++;
                }
            }
        }
        
        photoUrl.setAttribute("data-url", "#1");
//        String imagePath = "url(" + imageX + ")";
//      photoUrl.getStyle().setBackgroundImage(imagePath);
        
    }
}
