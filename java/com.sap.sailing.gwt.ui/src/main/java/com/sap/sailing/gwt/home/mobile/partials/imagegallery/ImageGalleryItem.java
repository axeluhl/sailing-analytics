package com.sap.sailing.gwt.home.mobile.partials.imagegallery;

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
import com.sap.sse.gwt.client.media.ImageDTO;

public class ImageGalleryItem extends Widget implements HasClickHandlers {

    private static ImageGalleryItemUiBinder uiBinder = GWT.create(ImageGalleryItemUiBinder.class);
    
    interface ImageGalleryItemUiBinder extends UiBinder<Element, ImageGalleryItem> {
    }
    
    @UiField DivElement imageUi;
    
    public ImageGalleryItem(ImageDTO image) {
        setElement(uiBinder.createAndBindUi(this));
        imageUi.getStyle().setBackgroundImage("url('" + image.getSourceRef() + "')");
    }
    
    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

}
