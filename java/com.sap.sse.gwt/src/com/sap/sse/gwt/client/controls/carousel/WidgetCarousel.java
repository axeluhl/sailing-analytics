package com.sap.sse.gwt.client.controls.carousel;

import java.util.Iterator;
import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
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
public class WidgetCarousel extends Composite {

    private static SlickSliderUiBinder ourUiBinder = GWT.create(SlickSliderUiBinder.class);

    @UiField
    ButtonElement slickPrev;

    @UiField
    ButtonElement slickNext;

    @UiField
    HTMLPanel sliderMainUi;

    /**
     * The height of the images
     */
    private int imagesHeight = 400;

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
    private boolean infiniteScrolling = false;
    /**
     * Image margin (effectively sets spacing between images)
     */
    private int contentMarginInPixels = 10;
    /**
     * Lazy load mechanism to use
     */
    private String lazyload = LAZYLOAD.ONDEMAND.getPropertyValue();

    private final String uniqueId;

    private LinkedList<Widget> items = new LinkedList<Widget>();

    /**
     * Widget constructor
     */
    public WidgetCarousel() {
        initWidget(ourUiBinder.createAndBindUi(this));

        uniqueId = Document.get().createUniqueId();

        sliderMainUi.addStyleName(uniqueId);

        final WidgetCarousel reference = this;
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                try {
                    setupSlider(reference, slickPrev, slickNext);
                    GWT.log("Finished to init slider");
                } catch (Exception e) {
                    GWT.log("Catched Exception on slider init", e);
                }
            }
        });

    }

    public void onAfterChange() {

        for (Iterator<Widget> iterator = items.iterator(); iterator.hasNext();) {
            Widget widget = (Widget) iterator.next();
            final Element parent = widget.getElement();
            GWT.log("checking widget...." + parent);
            if (parent != null && parent.getClassName().contains("slick-active")) {
                GWT.log("active widget....");
                if (iterator.hasNext()) {
                    Widget nextOne = iterator.next();
                    if (nextOne instanceof LazyLoadable) {
                        LazyLoadable lazyLoadable = (LazyLoadable) nextOne;
                        GWT.log("Widgetcarousel lazy load trigger for: " + nextOne.getClass().getName());
                        lazyLoadable.doInitializeLazyComponents();
                    } else {
                        GWT.log("Next component ist not lazy loadable: " + nextOne.getClass().getName());
                    }
                    return;
                }
            }
        }
    }

    /**
     * JSNI wrapper that does setup the slider
     *
     * @param uniqueId
     */
    native void setupSlider(WidgetCarousel sliderReference, ButtonElement prevArrowEl, ButtonElement nextArrowEl) /*-{

	$wnd
		.$(document)
		.ready(
			function() {
			    $wnd
				    .$(
					    '.'
						    + (sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::uniqueId))
				    .slick(
					    {
						dots : (sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::showDots),
						infinite : (sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::infiniteScrolling),
						swipeToSlide : false,
						arrows : true,
						responsive : false,
						prevArrow : prevArrowEl,
						nextArrow : nextArrowEl,
						onAfterChange : function() {
						    sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::onAfterChange()();
						},
						onInit : function() {
						    sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::onAfterChange()();
						}
					    });
			});

    }-*/;

    /**
     * ƒ Add new image to carousel.
     *
     * @param urlƒ
     */
    public void addWidget(Widget slide) {
        if (items.isEmpty()) {
            if (slide instanceof LazyLoadable) {
                LazyLoadable lazyLoadable = (LazyLoadable) slide;
                lazyLoadable.doInitializeLazyComponents();
            }
        }
        items.add(slide);
        sliderMainUi.add(slide);

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
    interface SlickSliderUiBinder extends UiBinder<Widget, WidgetCarousel> {
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
    public void init() {
        final WidgetCarousel reference = this;
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
                    GWT.log("Finished to init slider");
                } catch (Exception e) {
                    GWT.log("Catched Exception on slider init", e);
                }
            }
        });
    }

}