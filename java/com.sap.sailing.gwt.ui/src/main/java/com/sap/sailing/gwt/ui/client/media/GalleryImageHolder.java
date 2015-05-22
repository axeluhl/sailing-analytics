package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.media.ImageMetadataDTO;

public class GalleryImageHolder extends Widget implements HasClickHandlers {

    private static VideoThumbnailUiBinder uiBinder = GWT.create(VideoThumbnailUiBinder.class);

    interface VideoThumbnailUiBinder extends UiBinder<Element, GalleryImageHolder> {
    }

    @UiField
    DivElement imageHolderUi;

    public GalleryImageHolder(ImageMetadataDTO video) {
        setElement(uiBinder.createAndBindUi(this));
        imageHolderUi.getStyle().setBackgroundImage("url(\"" + video.getSourceRef() + "\")");
        
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

}
