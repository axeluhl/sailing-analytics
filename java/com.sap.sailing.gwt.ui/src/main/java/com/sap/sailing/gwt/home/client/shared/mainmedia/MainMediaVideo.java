package com.sap.sailing.gwt.home.client.shared.mainmedia;

import com.google.gwt.core.client.GWT;
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
    
    public MainMediaVideo(String eventName, String youtubeId) {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));

        youtubeEmbed.setSrc("//www.youtube.com/embed/" + youtubeId);

        youtubeEmbed.setAttribute("modestbranding", "modestbranding");
        youtubeEmbed.setAttribute("autohide", "1");
        youtubeEmbed.setAttribute("allowfullscreen", "allowfullscreen");
        youtubeEmbed.setAttribute("showinfo", "false");
        youtubeEmbed.setAttribute("frameborder", "0");
        
        SafeHtml safeHtmlEventName = LongNamesUtil.breakLongName(eventName);
        videoTitle.setInnerSafeHtml(safeHtmlEventName); 
    }
    
}
