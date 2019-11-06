package com.sap.sse.gwt.client.xdstorage;

import com.google.gwt.core.client.JavaScriptObject;

public class JavaScriptObjectWithID extends JavaScriptObject {
    static final String ID = "id";

    protected JavaScriptObjectWithID() {
        super();
    }
    
    public final native String getId() /*-{
                return this[@com.sap.sse.gwt.client.xdstorage.JavaScriptObjectWithID::ID];
    }-*/;
}
