package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Chargebee {
    public static native ChargebeeInstance init(InitOption options);
    public static native ChargebeeInstance getInstance();
    
    public static class InitOption extends JavaScriptObject {
        protected InitOption() {}
        
        public static native InitOption create(String site) /*-{
            return {
                site: site
            };
        }-*/;
    }
}
