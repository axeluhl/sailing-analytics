package com.sap.sailing.gwt.home.client.shared.media;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.media.ImageMetadataDTO;
import com.sap.sse.gwt.client.controls.carousel.ImageCarousel;

public class GalleryPlayer extends ResizeComposite {
    private static GalleryPlayerUiBinder uiBinder = GWT.create(GalleryPlayerUiBinder.class);
    private Command closeCommand;

    interface GalleryPlayerUiBinder extends UiBinder<Widget, GalleryPlayer> {
    }

    @UiField
    DivElement mainSliderUi;
    @UiField
    DivElement subSliderUi;

    ImageCarousel carousel = new ImageCarousel();
    private int selectedIdx;

    public GalleryPlayer(ImageMetadataDTO selected, List<ImageMetadataDTO> images) {
        initWidget(uiBinder.createAndBindUi(this));
        selectedIdx = Math.max(selectedIdx, images.indexOf(selected));
        for (ImageMetadataDTO i : images) {
            mainSliderUi.appendChild(createMainImgElement(i));
            subSliderUi.appendChild(createThumbImgElement(i));
        }
    }

    private ImageElement createThumbImgElement(ImageMetadataDTO i ) {
        ImageElement img = Document.get().createImageElement();
        img.setAttribute("src", i.getSourceRef());
        img.setHeight(100);
        img.setWidth(i.getWidthInPx() * 100 / i.getHeightInPx());
        return img;
    }
    
    private DivElement createMainImgElement(ImageMetadataDTO i) {
        DivElement img = Document.get().createDivElement();
        img.getStyle().setBackgroundImage("url(\"" + i.getSourceRef() + "\")");
        img.getStyle().setProperty("backgroundSize", "contain");
        img.getStyle().setProperty("backgroundRepeat", "no-repeat");
        img.getStyle().setProperty("backgroundPosition", "center");
        return img;
    }

    @Override
    protected void onLoad() {
        _onLoad();
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                gotoSlider(selectedIdx);
            }
        });
    }

    @Override
    public void onResize() {
        refreshSlider();
    }

    native void gotoSlider(int index) /*-{
	$wnd.$('.mainSlider').slick('slickGoTo', index);
	$wnd.$('.subSlider').slick('slickGoTo', index);
	$wnd.$('.mainSlider').slick('setPosition');
	$wnd.$('.subSlider').slick('setPosition');
    }-*/;

    native void refreshSlider() /*-{

	$wnd.$('.mainSlider').slick('setOption', null, null, true);
	$wnd.$('.subSlider').slick('setOption', null, null, true);

    }-*/;

    /**
     * JSNI wrapper that does setup the sliders
     *
     * @param uniqueId
     */
    native void _onLoad() /*-{

	$wnd.$('.mainSlider').slick({
	    lazyLoad : 'ondemand',
	    slidesToShow : 1,
	    slidesToScroll : 1,
	    arrows : false,
	    centerMode : false,
	    variableWidth : false,
	    adaptiveHeight : false,
	    asNavFor : '.subSlider'
	});
	$wnd.$('.subSlider').slick({
	    lazyLoad : 'ondemand',
	    infinite : true,
	    slidesToShow : 1,
	    slidesToScroll : 1,
	    swipeToSlide : true,
	    centerMode : true,
	    asNavFor : '.mainSlider',
	    arrows : false,
	    variableWidth : true,
	    focusOnSelect : true,
	    draggable : false
	});

    }-*/;

    public void setCloseCommand(Command closeCommand) {
        this.closeCommand = closeCommand;
    }

    @UiHandler("closeUi")
    public void didClose(ClickEvent e) {
        if (closeCommand != null) {
            closeCommand.execute();
        }
    }
}
