package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.gwt.client.media.ImageDTO;

public class GalleryImageHolder extends Composite implements HasClickHandlers {

    private static VideoThumbnailUiBinder uiBinder = GWT.create(VideoThumbnailUiBinder.class);

    interface VideoThumbnailUiBinder extends UiBinder<Widget, GalleryImageHolder> {
    }

    @UiField
    DivElement imageHolderUi;
    
    @UiField
    Anchor deleteAnchor;
    @UiField
    Anchor editAnchor;
    @UiField
    DivElement overlay;

    public GalleryImageHolder(ImageDTO video) {
        initWidget(uiBinder.createAndBindUi(this));
        imageHolderUi.getStyle().setBackgroundImage("url(\"" + video.getSourceRef() + "\")");
        
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }
    
    public void setManageable(boolean manageable) {
        if (manageable) {
            overlay.getStyle().setDisplay(Display.BLOCK);
        } else {
            overlay.getStyle().setDisplay(Display.NONE);
        }
    }

}
