package com.sap.sailing.gwt.home.client.shared.mainmedia;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.client.shared.LongNamesUtil;
import com.sap.sailing.gwt.ui.client.media.VideoJSPlayer;
import com.sap.sailing.gwt.ui.shared.media.SailingVideoDTO;

public class MainMediaVideo extends Composite {

    
    interface MainMediaVideoUiBinder extends UiBinder<Widget, MainMediaVideo> {
    }
    
    private static MainMediaVideoUiBinder uiBinder = GWT.create(MainMediaVideoUiBinder.class);

    @UiField SpanElement videoTitle;
    @UiField Element videoTitleWrapper;
    @UiField
    HTMLPanel videoHolderUi;
    
    final VideoJSPlayer vJs = new VideoJSPlayer();

    public MainMediaVideo(SailingVideoDTO video) {
        this(video, false);
    }
    
    public MainMediaVideo(SailingVideoDTO video, boolean showInfo) {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        

        // String youtubeUrl = "//www.youtube.com/embed/" + youtubeId + "?modestbranding=1&rel=0&autohide=1&fs=1";
        // if(!showInfo) {
        // youtubeUrl += "&showinfo=0";
        // }

        vJs.setVideo(video.getMimeType(), video.getSourceRef());
        videoHolderUi.clear();
        videoHolderUi.add(vJs);
        String eventName = video.getTitle();
        if(eventName == null || eventName.isEmpty()) {
            videoTitleWrapper.removeFromParent();
        } else {
            SafeHtml safeHtmlEventName = LongNamesUtil.breakLongName(eventName);
            videoTitle.setInnerSafeHtml(safeHtmlEventName); 
        }

        // addDomHandler(new MouseOverHandler() {
        //
        // @Override
        // public void onMouseOver(MouseOverEvent event) {
        // GWT.log("Duration: " + vJs.getDuration());
        // vJs.setCurrentTime(vJs.getDuration() / 2);
        // }
        // }, MouseOverEvent.getType());

    }
}
