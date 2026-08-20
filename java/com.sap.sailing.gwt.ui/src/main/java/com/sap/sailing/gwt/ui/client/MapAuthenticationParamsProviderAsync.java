package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Shared async GWT-RPC super-interface for services that can supply the authentication parameters needed to load a
 * map provider. It is deliberately named without reference to a specific provider so that future providers requiring
 * authentication can share (and extend) it. Both {@link SailingServiceAsync} and {@link SimulatorServiceAsync} extend
 * this interface, which lets
 * {@code com.sap.sailing.gwt.ui.shared.racemap.MapsLoader#load(Runnable, MapAuthenticationParamsProviderAsync,
 * com.sap.sailing.gwt.ui.client.ErrorReporter, StringMessages)} accept either service.
 * <p>
 * The method name is retained from the original {@link SailingService}/{@link SimulatorService} contract to avoid a
 * wider GWT-RPC serialization-policy change.
 */
public interface MapAuthenticationParamsProviderAsync {
    void getGoogleMapsLoaderAuthenticationParams(AsyncCallback<String> callback);
}
