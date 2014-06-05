package com.sap.sailing.gwt.ui.client;

import com.sap.sse.gwt.client.mvp.ClientFactory;

/**
 * A client factory that can deliver the sailing-specific services.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface SailingClientFactory extends ClientFactory {
    SailingServiceAsync getSailingService();
}
