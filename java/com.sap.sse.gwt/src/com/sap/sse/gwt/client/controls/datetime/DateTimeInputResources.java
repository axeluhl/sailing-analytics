package com.sap.sse.gwt.client.controls.datetime;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

interface DateTimeInputResources extends ClientBundle {

    static final DateTimeInputResources INSTANCE = GWT.create(DateTimeInputResources.class);

    @Source("dateTimeInput.gss")
    Style css();

    interface Style extends CssResource {
        String dateTimeInput();
    }

}
