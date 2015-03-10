package com.sap.sailing.gwt.home.client.shared.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.UIObject;
import com.sap.sailing.gwt.home.client.shared.LongNamesUtil;

public class MediaPageVideo extends UIObject {

    
    interface MainMediaVideoUiBinder extends UiBinder<Element, MediaPageVideo> {
    }
    
    private static MainMediaVideoUiBinder uiBinder = GWT.create(MainMediaVideoUiBinder.class);

    @UiField IFrameElement youtubeEmbed;
    @UiField SpanElement videoTitle;
    
    public MediaPageVideo(String eventName, String youtubeId) {
        MediaPageResources.INSTANCE.css().ensureInjected();
        setElement(uiBinder.createAndBindUi(this));

        youtubeEmbed.setSrc("//www.youtube.com/embed/" + youtubeId + "?modestbranding=1&rel=0&showinfo=0&autohide=1&fs=1");
        
        SafeHtml safeHtmlEventName = LongNamesUtil.breakLongName(eventName);
        videoTitle.setInnerSafeHtml(safeHtmlEventName); 
    }
    
}
