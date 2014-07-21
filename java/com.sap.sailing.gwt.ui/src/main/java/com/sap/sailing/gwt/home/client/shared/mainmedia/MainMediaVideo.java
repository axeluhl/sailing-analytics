package com.sap.sailing.gwt.home.client.shared.mainmedia;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MainMediaVideo extends Composite {

    
    interface MainMediaVideoUiBinder extends UiBinder<Widget, MainMediaVideo> {
    }
    
    private static MainMediaVideoUiBinder uiBinder = GWT.create(MainMediaVideoUiBinder.class);

    @UiField IFrameElement youtubeEmbed;
    @UiField SpanElement videoTitle;
    
    public MainMediaVideo(String eventName, String youtubeId) {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        youtubeEmbed.setSrc("//www.youtube.com/embed/" + youtubeId + "?modestbranding=1&rel=0&showinfo=0&autohide=1&fs=1");
        videoTitle.setInnerHTML(eventName);
    }
    
}
