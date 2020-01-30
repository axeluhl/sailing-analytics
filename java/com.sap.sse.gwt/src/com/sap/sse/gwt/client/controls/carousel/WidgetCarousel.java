package com.sap.sse.gwt.client.controls.carousel;

import java.util.Iterator;
import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget carousel that uses the open source "slick carousel".
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
 *      WidgetCarousel slider = new WidgetCarousel();
 *
 *      - Add widgets
 *
 *      slider.addWidget(someWidget); slider.addWidget(anotherWidget); slider.addWidget(oneMoreWidget);
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
public class WidgetCarousel extends Composite {

    private static SlickSliderUiBinder ourUiBinder = GWT.create(SlickSliderUiBinder.class);

    @UiField
    HTMLPanel sliderMainUi;

    /**
     * slick slider property: dots
     */
    private boolean showDots = true;
    /**
     * slick slider property: infiniteScrolling
     */
    private boolean infiniteScrolling = true;

    private final String uniqueId;
    private boolean showArrows = true;

    private LinkedList<Widget> items = new LinkedList<Widget>();
    
    private boolean initialized;

    /**
     * Widget constructor
     */
    public WidgetCarousel() {
        initWidget(ourUiBinder.createAndBindUi(this));
        uniqueId = Document.get().createUniqueId();
        sliderMainUi.addStyleName(uniqueId);
    }
    
    private void initializeIfRequired() {
        // Slick slider may only get initialized when it is attached to DOM, visible and items are added
        // Rendering bugs may occur otherwise
        if (!initialized && !items.isEmpty() && isAttached() && isVisible()) {
            initialized = true;
            initAfterReflow();
        }
    }
    
    private void initAfterReflow() {
        // Slick slider initialization will produce layout problems when the content widget sizes aren't calculated yet.
        // Until the initial reflow happened, widget size are reported as 0. If that's the case,
        // initialization is deferred after the reflow.
        if (items.getFirst().getElement().getOffsetWidth() > 0) {
            init();
        } else {
            Scheduler.get().scheduleDeferred(this::initAfterReflow);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        initializeIfRequired();
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        // onLoad is called before the element is completely attached to the DOM.
        // Using scheduleFinally ensures, this is finished before continuing with the initialization.
        Scheduler.get().scheduleFinally(this::initializeIfRequired);
    }

    public void onAfterChange() {
        for (Iterator<Widget> iterator = items.iterator(); iterator.hasNext();) {
            Widget widget = iterator.next();
            final Element parent = widget.getElement();

            if (parent != null) {
                if (parent.getClassName().contains("slick-active")) {
                    if (iterator.hasNext()) {
                        Widget nextOne = iterator.next();
                        if (nextOne instanceof LazyLoadable) {
                            LazyLoadable lazyLoadable = (LazyLoadable) nextOne;
                            lazyLoadable.doInitializeLazyComponents();
                        }
                    }
                    int previousIdx = items.indexOf(widget) - 1 < 0 ? items.size() - 1 : items.indexOf(widget) - 1;
                    Widget previousOne = items.get(previousIdx);

                    if (previousOne instanceof LazyLoadable) {
                        LazyLoadable lazyLoadable = (LazyLoadable) previousOne;
                        lazyLoadable.doInitializeLazyComponents();
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
    native void setupSlider(WidgetCarousel sliderReference) /*-{
        $wnd
                .$(
                        '.'
                                + (sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::uniqueId))
                .on(
                        'afterChange',
                        function() {
                            sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::onAfterChange()();
                        });
        $wnd
                .$(
                        '.'
                                + (sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::uniqueId))
                .on(
                        'init',
                        function() {
                            sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::onAfterChange()();
                        });

        $wnd
                .$(
                        '.'
                                + (sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::uniqueId))
                .slick(
                        {
                            dots : (sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::showDots),
                            infinite : (sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::infiniteScrolling),
                            swipeToSlide : false,
                            responsive : false,
                            arrows : sliderReference.@com.sap.sse.gwt.client.controls.carousel.WidgetCarousel::showArrows,
                            prevArrow : "<div class='slick-prev'/>",
                            nextArrow : "<div class='slick-next'/>"

                        });

    }-*/;

    /**
     * Add new image to carousel.
     *
     * @param url
     * 
     */
    public void setWidgets(Iterable<Widget> slides) {
        for (Widget slide : slides) {
            items.add(slide);
            sliderMainUi.add(slide);
        }
        if (!items.isEmpty()) {
            ensureInitialized(items.getFirst());
            if (items.size() > 1) {
                ensureInitialized(items.get(1));
            }
            ensureInitialized(items.getLast());
        }
        initializeIfRequired();
    }
    
    private void ensureInitialized(Widget slide) {
        if (slide instanceof LazyLoadable) {
            LazyLoadable lazyLoadable = (LazyLoadable) slide;
            lazyLoadable.doInitializeLazyComponents();
        }
    }

    /**
     * Enable infinite loop sliding
     */
    public void setInfiniteScrolling(boolean infiniteScrolling) {
        this.infiniteScrolling = infiniteScrolling;
    }

    /**
     * Disable/ enable left/right arrows
     * 
     * @param showArrows
     */
    public void setShowArrows(boolean showArrows) {
        this.showArrows = showArrows;
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
     * Apply slick js to constructed dom tree.
     */
    private void init() {
        final WidgetCarousel reference = this;
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