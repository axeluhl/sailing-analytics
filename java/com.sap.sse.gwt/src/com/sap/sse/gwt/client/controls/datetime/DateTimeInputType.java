package com.sap.sse.gwt.client.controls.datetime;

/**
 * Enumeration of date and/or time related input {@link #getType() types} whose {@link #isSupported() support} in the
 * currently used browser will be checked at runtime.
 */
public enum DateTimeInputType {
    
    DATETIME_LOCAL("datetime-local"), DATE("date"), TIME("time");
    
    private final String type;
    private final boolean supported;

    private DateTimeInputType(String type) {
        this.type = type;
        this.supported = checkSupport(type);
    }

    /**
     * @return the type string for this {@link DateTimeInputType}
     */
    public String getType() {
        return type;
    }

    /**
     * @return <code>true</code> if this {@link DateTimeInputType} is supported, <code>false</code> otherwise
     */
    public boolean isSupported() {
        return supported;
    }

    private static final native boolean checkSupport(String type) /*-{
		var input = document.createElement("input");
		input.setAttribute("type", type);
		var desiredType = input.getAttribute('type');
		var supported = false;
		if (input.type === desiredType) {
			supported = true;
		}
		input.value = 'Hello world';
		var helloWorldAccepted = (input.value === 'Hello world');
		if (helloWorldAccepted) {
			supported = false;
		}
		input.value = '';
		return supported;
    }-*/;

}
