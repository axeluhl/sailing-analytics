package com.sap.sailing.gwt.common.client;

import com.google.gwt.core.client.GWT;

public class NavigatorUtil {
    /**
     * Note that this method will check whether the browser does support the copyToClipboard feature first 
     * and will do nothing if it does not to prevent JS error messages in the console.
     * @param String text: Text to share.
     */
    public static void copyToClipboard(String text) {
        if(nativeClientHasNavigatorCopyToClipboardSupport()) {
            nativeCopyToClipboard(text);
        }else {
            GWT.log("This browser does not support copying to clipboard");
        }
    }
    
    /**
     * Note that this method will check whether the browser does support the sharing feature first 
     * and will do nothing if it does not to prevent JS error messages in the console.
     * @param String url: URL to share.
     * @param String text: Text to share.
     */
    public static void shareUrlAndText(String url, String text) {
        if(nativeClientHasNavigatorShareSupport()) {
            nativeShare(url, text);
        }else {
            GWT.log("This browser does not support native sharing");
        }
    }
    
    public static boolean clientHasNavigatorShareSupport(){
        return nativeClientHasNavigatorShareSupport();
    }
    
    public static boolean clientHasNavigatorCopyToClipboardSupport() {
        return nativeClientHasNavigatorCopyToClipboardSupport();
    }
    
    private static native void nativeCopyToClipboard(String text) /*-{
        window.focus();
        navigator.clipboard.writeText(text);
    }-*/;

    private static native void nativeShare(String url, String text) /*-{
        window.focus();
        navigator.share({
            url : url,
            text : text
        });
    }-*/;

    private static native boolean nativeClientHasNavigatorShareSupport() /*-{
        window.focus();
        if (navigator && navigator.share) {
            return true;
        } else {
            return false;
        }
    }-*/;

    private static native boolean nativeClientHasNavigatorCopyToClipboardSupport() /*-{
        window.focus();
        if (navigator && navigator.clipboard && navigator.clipboard.writeText) {
            return true;
        } else {
            return false;
        }
    }-*/;
}
