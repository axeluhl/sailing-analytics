package com.sap.sailing.gwt.ui.client.subscription.chargebee;

import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Wrapped class for native JS module
 * 
 * @author Tu Tran
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Chargebee {
    /**
     * Initialize JS module {@link https://www.chargebee.com/checkout-portal-docs/api.html#chargebee-object}
     * 
     * @param options
     *            Module initial options object
     * @return JS module instance
     */
    public static native ChargebeeInstance init(InitOption options);

    /**
     * Get module instance, which is available only after initialization
     * {@link https://www.chargebee.com/checkout-portal-docs/api.html#getinstance}
     * 
     * @return JS module instance
     */
    public static native ChargebeeInstance getInstance();

    public static class InitOption extends JavaScriptObject {
        protected InitOption() {
        }

        /**
         * Create JS module initial option object
         * 
         * @param site
         *            The configuration site
         * @return Initial option object
         */
        public static native InitOption create(String site) /*-{
            return {
                site : site
            };
        }-*/;
    }
}
