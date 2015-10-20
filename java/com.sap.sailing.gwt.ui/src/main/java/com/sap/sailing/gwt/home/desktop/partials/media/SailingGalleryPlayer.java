package com.sap.sailing.gwt.home.desktop.partials.media;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.media.SailingImageDTO;
import com.sap.sse.common.Util;

public class SailingGalleryPlayer extends ResizeComposite {
   
    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    interface MyBinder extends UiBinder<Widget, SailingGalleryPlayer> {
    }

    @UiField DivElement mainSliderUi;
    @UiField DivElement subSliderUi;

    private boolean autoplay;
    private int selectedIdx;

    public SailingGalleryPlayer(SailingImageDTO selected, Collection<SailingImageDTO> images) {
        initWidget(uiBinder.createAndBindUi(this));
        selectedIdx = Math.max(0, Util.indexOf(images, selected));
        for (SailingImageDTO i : images) {
            mainSliderUi.appendChild(createMainImgElement(i));
            subSliderUi.appendChild(createThumbImgElement(i));
        }
    }

    private ImageElement createThumbImgElement(SailingImageDTO i ) {
        ImageElement img = Document.get().createImageElement();
        img.setAttribute("src", i.getSourceRef());
        img.getStyle().setHeight(103, Unit.PX);
        return img;
    }
    
    private DivElement createMainImgElement(SailingImageDTO i) {
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
        mainSliderUi.getChild(0).<Element>cast().setTabIndex(0);
        mainSliderUi.getChild(0).<Element>cast().focus();
    }

    @Override
    public void onResize() {
        refreshSlider();
    }

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
	$wnd
		.$('.mainSlider')
		.slick(
			{
			    lazyLoad : 'ondemand',
			    slidesToShow : 1,
			    slidesToScroll : 1,
			    arrows : false,
			    centerMode : false,
			    speed : 2500,
			    autoplaySpeed : 10000,
			    pauseOnHover : true,
			    variableWidth : false,
			    adaptiveHeight : false,
			    asNavFor : '.subSlider',
			    initialSlide : this.@com.sap.sailing.gwt.home.desktop.partials.media.SailingGalleryPlayer::selectedIdx
			});
	$wnd
		.$('.subSlider')
		.slick(
			{
			    lazyLoad : 'ondemand',
			    infinite : true,
			    slidesToShow : 1,
			    slidesToScroll : 1,

			    speed : 500,
			    swipeToSlide : true,
			    centerMode : true,
			    asNavFor : '.mainSlider',
			    arrows : false,
			    variableWidth : true,
			    focusOnSelect : true,
			    draggable : false,
			    initialSlide : this.@com.sap.sailing.gwt.home.desktop.partials.media.SailingGalleryPlayer::selectedIdx
			});
    }-*/;

    private native void _slickPlay() /*-{
	$wnd.$('.mainSlider').slick('slickPlay').slick('slickNext').slick(
		'setOption', 'autoplay', true); // workaround for bug https://github.com/kenwheeler/slick/issues/1446
    }-*/;

    private native void _slickPause() /*-{
	$wnd.$('.mainSlider').slick('slickPause').slick('setOption',
		'autoplay', false); // workaround for bug https://github.com/kenwheeler/slick/issues/1446
    }-*/;

    public void toggleAutoplay() {
        autoplay = !autoplay;
        if (autoplay) {
            _slickPlay();
        } else {
            _slickPause();
        }
    }

    public boolean isAutoplaying() {
        return autoplay;
    }
}
