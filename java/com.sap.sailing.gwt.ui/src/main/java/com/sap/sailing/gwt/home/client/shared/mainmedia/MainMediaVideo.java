package com.sap.sailing.gwt.home.client.shared.mainmedia;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.LongNamesUtil;

public class MainMediaVideo extends Composite {

    
    interface MainMediaVideoUiBinder extends UiBinder<Widget, MainMediaVideo> {
    }
    
    private static MainMediaVideoUiBinder uiBinder = GWT.create(MainMediaVideoUiBinder.class);

    @UiField IFrameElement youtubeEmbed;
    @UiField SpanElement videoTitle;
    @UiField Element videoTitleWrapper;
    
    public MainMediaVideo(String eventName, String youtubeId) {
        this(eventName, youtubeId, false);
    }
    
    public MainMediaVideo(String eventName, String youtubeId, boolean showInfo) {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        
        String youtubeUrl = "//www.youtube.com/embed/" + youtubeId + "?modestbranding=1&rel=0&autohide=1&fs=1";
        if(!showInfo) {
            youtubeUrl += "&showinfo=0";
        }
        youtubeEmbed.setSrc(youtubeUrl);
        
        if(eventName == null || eventName.isEmpty()) {
            videoTitleWrapper.removeFromParent();
        } else {
            SafeHtml safeHtmlEventName = LongNamesUtil.breakLongName(eventName);
            videoTitle.setInnerSafeHtml(safeHtmlEventName); 
        }
    }
    
}
