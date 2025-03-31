package com.sap.sse.gwt.dispatch.client.system;

import java.util.Date;

/**
 * Interface that defines the contract for dispatch systems that do provide the current server time.
 *
 * @param <CTX>
 *            the context that will be provided to the actions during execution
 */
public interface ProvidesServerTime {
    Date getCurrentServerTime();
}
