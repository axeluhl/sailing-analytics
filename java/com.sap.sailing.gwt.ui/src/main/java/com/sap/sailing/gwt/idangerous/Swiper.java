package com.sap.sailing.gwt.idangerous;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wrapping  http://www.idangero.us/sliders/swiper/
 *
 */
public final class Swiper extends JavaScriptObject {
    
    /**
     * Event notification about slide change.
     * Concrete use case this listener is tailored for: hide/show prev/next buttons when there a no slides left in a given direction.
     *
     */
    public static interface PageChangeListener {
        void pageChanged(int newPageIndex, int pageCount);
    }
    
    protected Swiper() {};
    
    public static Swiper createWithLoopOption(String containerClass, String wrapperClass, String slideClass) {
        return Swiper.createWithDefaultOptions(containerClass, wrapperClass, slideClass, null, true);
    }
    
    public static Swiper createWithoutLoopOption(String containerClass, String wrapperClass, String slideClass, PageChangeListener pageChangeListener) {
        return Swiper.createWithDefaultOptions(containerClass, wrapperClass, slideClass, pageChangeListener, false);
    }
    
    private static native Swiper createWithDefaultOptions(String containerClass, String wrapperClass, String slideClass, PageChangeListener pageChangeListener, boolean loop) /*-{
        var options = {
          loop: loop,
          wrapperClass: wrapperClass,
          slideClass: slideClass
        };
        if (pageChangeListener) {
            options.onSlideChangeEnd = function(swiper) {
                //var slideCount = swiper.slides.length - swiper.loopedSlides*2; //see https://github.com/nolimits4web/Swiper/issues/797
                var slideCount = swiper.slides.length;
                pageChangeListener.@com.sap.sailing.gwt.idangerous.Swiper.PageChangeListener::pageChanged(II)(swiper.activeIndex, slideCount);
            };
        }
        return new $wnd.Swiper('.'+containerClass, options);
    }-*/;
    
    public native void swipeNext() /*-{
        this.swipeNext();
    }-*/;

    public native void swipePrev() /*-{
        this.swipePrev();
    }-*/;

}
