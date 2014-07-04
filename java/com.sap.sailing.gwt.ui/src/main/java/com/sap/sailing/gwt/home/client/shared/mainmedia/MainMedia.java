package com.sap.sailing.gwt.home.client.shared.mainmedia;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class MainMedia extends Composite {

    @UiField DivElement photoImage;

    @UiField(provided=true) RecentEventVideo eventVideo1;
    @UiField(provided=true) RecentEventVideo eventVideo2;
    @UiField(provided=true) RecentEventVideo eventVideo3;

    private List<EventDTO> recentEvents;

    private final String basePathToImages = "http://static.sapsailing.com/newhome/media/";

    private String[] images = { "main-media-photo-1.jpg" };
    
    interface MainMediaUiBinder extends UiBinder<Widget, MainMedia> {
    }
    
    private static MainMediaUiBinder uiBinder = GWT.create(MainMediaUiBinder.class);

    public MainMedia(PlaceNavigator navigator) {
        eventVideo1 = new RecentEventVideo(navigator);
        eventVideo2 = new RecentEventVideo(navigator);
        eventVideo3 = new RecentEventVideo(navigator);
        
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        String imagePath = "url(" + basePathToImages + images[0] + ")";
        photoImage.getStyle().setBackgroundImage(imagePath);

        recentEvents = new ArrayList<EventDTO>();
    }

    public void setRecentEvents(List<EventDTO> theRecentEvents) {
        recentEvents.clear();
        recentEvents.addAll(theRecentEvents);
        
        int size = recentEvents.size();
        if(size > 0) {
            eventVideo1.setEvent(recentEvents.get(0));
        } else {
            eventVideo1.setVisible(false);
        }
        if(size > 1) {
            eventVideo2.setEvent(recentEvents.get(1));
        } else {
            eventVideo2.setVisible(false);
        }
        if(size > 2) {
            eventVideo3.setEvent(recentEvents.get(2));
        } else {
            eventVideo3.setVisible(false);
        }
    }
}
