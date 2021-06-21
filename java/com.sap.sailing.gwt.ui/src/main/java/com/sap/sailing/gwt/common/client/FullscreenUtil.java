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

    public static void requestFullScreenToggle(String elementId) {
        nativeRequestFullScreenToggle(elementId);
    };

    public static void exitFullscreen() {
        nativeExitFullscreen();
    };
    
    private static native void nativeRequestFullScreen() /*-{
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
    }-*/;
    
    private static native void nativeRequestFullScreenToggle(String elementId) /*-{
        var element = $doc.getElementById(elementId);
        if (window.matchMedia('(display-mode: fullscreen)').matches) {
            if ($doc.exitFullscreen) {
                    $doc.exitFullscreen();
            } else if ($doc.msExitFullscreen) {
                    $doc.msExitFullscreen();
            } else if ($doc.mozCancelFullScreen) {
                    $doc.mozCancelFullScreen();
            } else if ($doc.webkitCancelFullScreen) {
                    $doc.webkitCancelFullScreen();
            }
        } else { 
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

    public static native void nativeExitFullscreen()
    /*-{
            if ($doc.exitFullscreen) {
                    $doc.exitFullscreen();
            } else if ($doc.msExitFullscreen) {
                    $doc.msExitFullscreen();
            } else if ($doc.mozCancelFullScreen) {
                    $doc.mozCancelFullScreen();
            } else if ($doc.webkitCancelFullScreen) {
                    $doc.webkitCancelFullScreen();
            }
    }-*/;

}
