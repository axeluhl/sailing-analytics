package com.sap.sailing.gwt.home.client.shared.mainmedia;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.app.PlaceNavigator;
import com.sap.sailing.gwt.home.client.place.event.EventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventPlace.Tokenizer;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class RecentEventVideo extends Composite {
    
    @UiField SpanElement eventVideoLength;
    @UiField SpanElement eventName;
    @UiField ImageElement eventVideoPreviewImage;
    @UiField Anchor eventMediaPageLink;

    private final String defaultVideoImageUrl = "http://static.sapsailing.com/newhome/default_video_preview.jpg";

    private EventDTO event;

    interface RecentEventVideoUiBinder extends UiBinder<Widget, RecentEventVideo> {
    }
    
    private static RecentEventVideoUiBinder uiBinder = GWT.create(RecentEventVideoUiBinder.class);

    private final PlaceNavigator navigator;
    
    public RecentEventVideo(PlaceNavigator navigator) {
        this.navigator = navigator;
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public void setEvent(EventDTO event) {
        this.event = event;
        updateUI();
    }

    @UiHandler("eventMediaPageLink")
    public void goToEventMediaPage(ClickEvent e) {
        if(event.getBaseURL().contains("localhost") || event.getBaseURL().contains("127.0.0.1")) {
            navigator.goToEvent(event.id.toString());
        } else {
            EventPlace eventPlace = new EventPlace(event.id.toString());
            EventPlace.Tokenizer t = new Tokenizer();
            String remoteEventUrl = event.getBaseURL() + "/gwt/Home.html#" + EventPlace.class.getSimpleName() + ":" + t.getToken(eventPlace);
            Window.Location.replace(remoteEventUrl);
        }
    }

    private void updateUI() {
        eventName.setInnerText(event.getName());
        
        if(event.getVideoURLs().size() == 0) {
            eventVideoPreviewImage.setSrc(defaultVideoImageUrl);
        } else {
            eventVideoPreviewImage.setSrc(event.getVideoURLs().get(0));
        }
    }
}
