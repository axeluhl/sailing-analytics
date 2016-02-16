package com.sap.sse.security.ui.authentication.decorator;

import com.google.gwt.user.client.ui.Widget;

/**
 * Factory used to create a widget deferred.
 *
 */
public interface WidgetFactory {

    /**
     * @return the created widget instance
     */
    Widget get();

}
