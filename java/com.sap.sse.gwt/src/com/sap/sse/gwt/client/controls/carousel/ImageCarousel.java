package com.sap.sse.gwt.client.controls.carousel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
 * 
 *      The widget depends on css and js resources, as well as jquery 1.7+.
 *
 *      To use the CDN resources, include:
 * 
 *      <link rel="stylesheet" type="text/css" href="//cdn.jsdelivr.net/jquery.slick/1.3.15/slick.css"/> <script
 *      type="text/javascript" src="//cdn.jsdelivr.net/jquery.slick/1.3.15/slick.min.js"></script>
 *
 *      Created by pgtaboada on 10.11.14.
 */
public class ImageCarousel extends Widget {

    private static SlickSliderUiBinder ourUiBinder = GWT.create(SlickSliderUiBinder.class);
    private static MyTemplate myTemplate = GWT.create(MyTemplate.class);

    @UiField
    ButtonElement slickPrev;

    @UiField
    ButtonElement slickNext;

    /**
     * slick slider property: dots
     */
    private boolean showDots = false;
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
    private boolean infiniteScrolling = false;
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
    private int imagesHeight = 400;

    private final String uniqueId;

    /**
     * Widget constructor
     */
    public ImageCarousel() {
        setElement(ourUiBinder.createAndBindUi(this));

        uniqueId = "slider_" + Document.get().createUniqueId();
        getElement().addClassName(uniqueId);

    }

    /**
     * JSNI wrapper that does setup the slider
     *
     * @param uniqueId
     */
    native void setupSlider(ImageCarousel sliderReference, ButtonElement prevArrowEl, ButtonElement nextArrowEl) /*-{

	$wnd
		.$(document)
		.ready(
			function() {
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
						slidesToShow : 3,
						prevArrow : prevArrowEl,
						nextArrow : nextArrowEl
					    });
			});

    }-*/;

    native void addImage(ImageCarousel sliderReference, final DivElement el) /*-{

	$wnd
		.$(
			'.'
				+ (sliderReference.@com.sap.sse.gwt.client.controls.carousel.ImageCarousel::uniqueId))
		.slickAdd(el);

    }-*/;

    /**
     * Add new image to carousel.
     *
     * @param url
     */
    public void addImage(String url) {

        DivElement imageHolder = Document.get().createDivElement();

        SafeStylesBuilder ssb = new SafeStylesBuilder();

        ssb.height(imagesHeight, Unit.PX);
        ssb.margin(contentMarginInPixels, Style.Unit.PX);

        imageHolder.setInnerSafeHtml(myTemplate.imageDiv(UriUtils.fromString(url).asString(), ssb.toSafeStyles()));
        getElement().appendChild(imageHolder);

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
     * Template used to create image element
     */
    interface MyTemplate extends SafeHtmlTemplates {
        @Template("<div><img style='{1}' data-lazy='{0}'/></div>")
        SafeHtml imageDiv(String uri, SafeStyles imageStyles);
    }

    /**
     * Apply slick js to constructed dom tree.
     */
    public void init() {
        final ImageCarousel reference = this;
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                try {
                    setupSlider(reference, slickPrev, slickNext);
                    for (int i = 0; i < getElement().getChildCount(); i++) {
                        Element child = (Element) getElement().getChild(i);
                        String className = child.getClassName();
                        if (className != null && className.contains("slick-list")) {
                            child.getStyle().setHeight(imagesHeight, Unit.PX);
                        }
                    }
                } catch (Exception e) {
                    GWT.log("Catched Exception on slider init", e);
                }
            }
        });
    }

}