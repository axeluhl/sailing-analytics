package com.sap.sailing.gwt.ui.client.subscription;

import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Wrapped class for native Chargebee JS module
 * 
 * @author tutran
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Chargebee {
    /**
     * Init Chargebee module {@link https://www.chargebee.com/checkout-portal-docs/api.html#chargebee-object}
     * 
     * @param options
     * @return
     */
    public static native ChargebeeInstance init(InitOption options);

    /**
     * Get Chargebee instance which is available only after initialization
     * {@link https://www.chargebee.com/checkout-portal-docs/api.html#getinstance}
     * 
     * @return
     */
    public static native ChargebeeInstance getInstance();

    public static class InitOption extends JavaScriptObject {
        protected InitOption() {
        }

        public static native InitOption create(String site) /*-{
            return {
                site : site
            };
        }-*/;
    }
}
