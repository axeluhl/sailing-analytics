package com.sap.sailing.gwt.ui.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface PendingAjaxCallBundle extends ClientBundle {
    @Source("ajax-semaphore.js")
    TextResource ajaxSemaphoreJS();
}
