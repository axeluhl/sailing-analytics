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

    public static boolean isFullscreenSupported() {
        return nativeIsFullscreenSupported();
    };
    
    private static native void nativeRequestFullScreen() /*-{
        var element = $doc.documentElement;
        if (element.requestFullscreen) {
            element.requestFullscreen();
        } else if (element.mozRequestFullScreen) {
            element.mozRequestFullScreen();
        } else if (element.webkitRequestFullscreen) {
            element.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
        } else if (element.msRequestFullscreen) {
            element.msRequestFullscreen();
        } else {
            console.log("Fullscreen API not available (e.g. on mobile devices).");
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
            } else {
                console.log("Fullscreen API not available (e.g. on mobile devices).");
            }
        } else { 
            if (element.requestFullscreen) {
                element.requestFullscreen();
            } else if (element.mozRequestFullScreen) {
                element.mozRequestFullScreen();
            } else if (element.webkitRequestFullscreen) {
                element.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
            } else if (element.msRequestFullscreen) {
                element.msRequestFullscreen();
            } else {
                console.log("Fullscreen API not available (e.g. on mobile devices).");
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
            } else {
                console.log("Fullscreen API not available (e.g. on mobile devices).");
            }
    }-*/;
    
    private static native boolean nativeIsFullscreenSupported() /*-{
        var supported;
        var element = $doc.documentElement;
        if (element.requestFullscreen) {
            supported = true;
            console.log("Fullscreen API available.");
        } else if (element.mozRequestFullScreen) {
            supported = true;
            console.log("Fullscreen API (moz) available.");
        } else if (element.webkitRequestFullscreen) {
            console.log("Fullscreen API (webkit) available.");
            supported = true;
        } else if (element.msRequestFullscreen) {
            console.log("Fullscreen API (ie) available.");
            supported = true;
        } else {
            supported = false;
            console.log("Fullscreen API not available (e.g. on mobile devices).");
        }
        return supported;
    }-*/;

}
