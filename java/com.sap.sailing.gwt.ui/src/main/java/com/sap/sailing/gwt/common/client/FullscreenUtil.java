package com.sap.sailing.gwt.common.client;

/**
 * A utility class to set the browser to fullscreen mode
 * @author c5163874 (Frank)
 *
 */
public class FullscreenUtil {

    public static void requestFullscreen() {
        nativeRequestFullScreen();
    };
    
    private static native void nativeRequestFullScreen() /*-{
        $doc.fullscreenEnabled = $doc.fullscreenEnabled || $doc.mozFullScreenEnabled || $doc.documentElement.webkitRequestFullScreen;
        if($doc.fullscreenEnabled) {
            var element = $doc.documentElement;
            if(element.requestFullscreen) {
                element.requestFullscreen();
            } else if(element.mozRequestFullScreen) {
                element.mozRequestFullScreen();
            } else if(element.webkitRequestFullscreen) {
                element.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
            } else if(element.msRequestFullscreen) {
                element.msRequestFullscreen();
            }
        }
    }-*/;

}
