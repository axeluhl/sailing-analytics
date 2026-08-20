package com.sap.sailing.gwt.ui.client;

/**
 * Shared synchronous GWT-RPC super-interface for services that can supply the authentication parameters needed to
 * load a map provider. It is the synchronous twin of {@link MapAuthenticationParamsProviderAsync} and, like it, is
 * deliberately named without reference to a specific provider so that future providers requiring authentication can
 * share (and extend) it. Both {@link SailingService} and {@link SimulatorService} extend this interface instead of
 * declaring the method themselves.
 * <p>
 * Extracting the declaration into this common super-interface also lets the server-only
 * {@link com.sap.sailing.gwt.ui.server.MapAuthenticationParamsSupport} mixin extend it and supply the shared
 * {@code default} body: because that mixin's declaration is more specific than the one inherited via
 * {@link SailingService}/{@link SimulatorService}, the two impls resolve to the single {@code default} implementation
 * without an inheritance conflict.
 * <p>
 * The method name is retained from the original {@link SailingService}/{@link SimulatorService} contract to avoid a
 * wider GWT-RPC serialization-policy change.
 */
public interface MapAuthenticationParamsProvider {
    String getGoogleMapsLoaderAuthenticationParams();
}
