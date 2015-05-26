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
import com.sap.sailing.domain.common.media.MimeType;
import com.sap.sailing.gwt.home.client.shared.LongNamesUtil;
import com.sap.sailing.gwt.ui.client.media.VideoJSPlayer;
import com.sap.sailing.gwt.ui.shared.media.VideoMetadataDTO;

public class MainMediaVideo extends Composite {

    
    interface MainMediaVideoUiBinder extends UiBinder<Widget, MainMediaVideo> {
    }
    
    private static MainMediaVideoUiBinder uiBinder = GWT.create(MainMediaVideoUiBinder.class);

    @UiField SpanElement videoTitle;
    @UiField Element videoTitleWrapper;
    @UiField
    HTMLPanel videoHolderUi;
    
    final VideoJSPlayer vJs = new VideoJSPlayer();

    public MainMediaVideo(String eventName, String sourceRef, MimeType type) {
        this(eventName, sourceRef, type, false);
    }
    
    public MainMediaVideo(String eventName, String sourceRef, MimeType type, boolean showInfo) {
        MainMediaResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        

        // String youtubeUrl = "//www.youtube.com/embed/" + youtubeId + "?modestbranding=1&rel=0&autohide=1&fs=1";
        // if(!showInfo) {
        // youtubeUrl += "&showinfo=0";
        // }

        vJs.setSource(sourceRef, type);
        videoHolderUi.clear();
        videoHolderUi.add(vJs);
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

    public void show(VideoMetadataDTO videoCandidateInfo) {
        vJs.setSource(videoCandidateInfo.getSourceRef(), videoCandidateInfo.getMimeType());

    }
    
}
