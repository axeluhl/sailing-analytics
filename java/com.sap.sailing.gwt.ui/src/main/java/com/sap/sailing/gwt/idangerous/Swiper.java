package com.sap.sailing.gwt.idangerous;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wrapping  http://www.idangero.us/sliders/swiper/
 *
 */
public final class Swiper extends JavaScriptObject {
    
    protected Swiper() {};
    
    public static native Swiper createWithDefaultOptions(String containerClass, String wrapperClass, String slideClass, boolean loop) /*-{
        var options = {
          loop: loop,
          wrapperClass: wrapperClass,
          slideClass: slideClass
        };
        return new $wnd.Swiper('.'+containerClass, options);
    }-*/;
    
    public native void swipeNext() /*-{
        this.swipeNext();
    }-*/;

    public native void swipePrev() /*-{
        this.swipePrev();
    }-*/;

}
