package com.sap.sse.gwt.client.controls.carousel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;

/**
 * Image carousel that uses the open source "slick carousel".
 * <p/>
 * Documentation/ download of slick js and css from homepate/ github page.
 *
 * @see <a href="http://kenwheeler.github.io/slick/">Slick homepage</a>.
 * @see <a href="https://github.com/kenwheeler/slick/">Slick github page</a>.
 * 
 * 
 *      <p/>
 *
 *      This class uses JSNI to wrap the JS slick carousel.
 *
 *      Usage:
 *
 *      - Create widget
 *
 *      ImageCarousel slider = new ImageCarousel();
 *
 *      - Add images
 *
 *      slider.addImage("http://demotivators.despair.com/demotivational/foresightdemotivator.jpg");
 *      slider.addImage("http://demotivators.despair.com/demotivational/commitmentdemotivator.jpg");
 *      slider.addImage("http://demotivators.despair.com/demotivational/platitudesdemotivator.jpg");
 *      slider.addImage("http://demotivators.despair.com/demotivational/destinyrockdemotivator.jpg");
 *      slider.addImage("http://demotivators.despair.com/demotivational/aspirationdemotivator.jpg");
 *
 *      - Start the carousel
 *
 *      slider.init();
 * 
 *      The widget depends on css and js resources, as well as jquery 1.7+.
 *
 *
 *      Created by pgtaboada on 10.11.14.
 */
public class ImageCarousel extends Widget {

    private static SlickSliderUiBinder ourUiBinder = GWT.create(SlickSliderUiBinder.class);

    /**
     * slick slider property: dots
     */
    private boolean showDots = true;
    /**
     * slick slider property: centerMode
     */
    private boolean centerMode = true;
    /**
     * slick slider property: variableWidth
     */
    private boolean variableWidth = true;
    /**
     * slick slider property: infiniteScrolling
     */
    private boolean infiniteScrolling = true;
    /**
     * Image margin (effectively sets spacing between images)
     */
    private int contentMarginInPixels = 10;
    /**
     * Lazy load mechanism to use
     */
    private String lazyload = LAZYLOAD.ONDEMAND.getPropertyValue();

    /**
     * The height of the images
     */
    private int imagesHeight = 300;

    private final String uniqueId;

    /**
     * Widget constructor
     */
    public ImageCarousel() {
        setElement(ourUiBinder.createAndBindUi(this));

        uniqueId = "slider_" + Document.get().createUniqueId();
        getElement().addClassName(uniqueId);
        init();
    }

    /**
     * JSNI wrapper that does setup the slider
     *
     * @param uniqueId
     */
    native void setupSlider(ImageCarousel sliderReference) /*-{

	$wnd
		.$(
			'.'
				+ (sliderReference.@com.sap.sse.gwt.client.controls.carousel.ImageCarousel::uniqueId))
		.slick(
			{
			    dots : (sliderReference.@com.sap.sse.gwt.client.controls.carousel.ImageCarousel::showDots),
			    infinite : (sliderReference.@com.sap.sse.gwt.client.controls.carousel.ImageCarousel::infiniteScrolling),
			    centerMode : (sliderReference.@com.sap.sse.gwt.client.controls.carousel.ImageCarousel::centerMode),
			    variableWidth : (sliderReference.@com.sap.sse.gwt.client.controls.carousel.ImageCarousel::variableWidth),
			    lazyLoad : (sliderReference.@com.sap.sse.gwt.client.controls.carousel.ImageCarousel::lazyload),
			    swipeToSlide : false,
			    arrows : true,
			    responsive : true,
			    slidesToShow : 2

			});

    }-*/;

    /**
     * Add new image to carousel.
     *
     * @param url
     */
    public void addImage(String url, int height, int width) {

        DivElement imageHolder = Document.get().createDivElement();
        ImageElement imageElement = Document.get().createImageElement();
        imageElement.setAttribute("data-lazy", UriUtils.fromString(url).asString());

        imageHolder.getStyle().setHeight(imagesHeight, Unit.PX);
        imageHolder.getStyle().setWidth(Math.round(width * (imagesHeight / (double) height)), Unit.PX);
        imageHolder.appendChild(imageElement);

        getElement().appendChild(imageHolder);

        if (getElement().getChildCount() > 20) {
            setInfiniteScrolling(false);
            setShowDots(false);
        }

    }

    /**
     * Define spacing between images in carousel. In fact, it is setting the img margins in pixels.
     *
     * @param contentMarginInPixels
     */
    public void setContentMarginInPixels(int contentMarginInPixels) {
        this.contentMarginInPixels = contentMarginInPixels;
    }

    /**
     * Set lazy loading technique. Accepts 'ondemand' or 'progressive'.
     *
     * @param lazyload
     */
    public void setLazyload(LAZYLOAD lazyload) {
        this.lazyload = lazyload.getPropertyValue();
    }

    /**
     * Enable infinite loop sliding
     */
    public void setInfiniteScrolling(boolean infiniteScrolling) {
        this.infiniteScrolling = infiniteScrolling;
    }

    /**
     * Enable variable width slides.
     *
     * @param variableWidth
     */
    public void setVariableWidth(boolean variableWidth) {
        this.variableWidth = variableWidth;
    }

    /**
     * Enables centered view with partial prev/next slides.
     *
     * @param centerMode
     */
    public void setCenterMode(boolean centerMode) {
        this.centerMode = centerMode;
    }

    /**
     * Enable dot indicators
     *
     * @param showDots
     */
    public void setShowDots(boolean showDots) {
        this.showDots = showDots;
    }

    /**
     * UiBinder interface
     */
    interface SlickSliderUiBinder extends UiBinder<DivElement, ImageCarousel> {
    }

    /**
     * Enum wrapper for lazy load techniques
     */
    public enum LAZYLOAD {

        /**
         * Loads each image "on demand"
         */
        ONDEMAND("ondemand"),

        /**
         * Progressively loads one image after another
         */
        PROGRESSIVE("progressive");

        String propertyValue;

        LAZYLOAD(String propertyValue) {
            this.propertyValue = propertyValue;
        }

        public String getPropertyValue() {
            return propertyValue;
        }
    }

    /**
     * Apply slick js to constructed dom tree.
     */
    private void init() {
        final ImageCarousel reference = this;

        Scheduler.get().scheduleDeferred(new Command() {

            @Override
            public void execute() {
                try {
                    setupSlider(reference);
                } catch (Exception e) {
                    GWT.log("Catched Exception on slider init", e);
                }
            }
        });
    }

}