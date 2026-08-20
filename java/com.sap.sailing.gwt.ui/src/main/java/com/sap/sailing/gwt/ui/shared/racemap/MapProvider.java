package com.sap.sailing.gwt.ui.shared.racemap;

/**
 * A map provider strategy: it knows its display {@link #getName() name}, the {@link #getCapabilities() capabilities}
 * it offers, and how to {@link #load() load} its underlying map API. A provider is fully self-contained: everything it
 * needs (for example authentication parameters) is injected into its constructor, so {@link #load()} takes no
 * arguments. Loading completion is signalled centrally through {@code MapsLoader}'s shared callback machinery, so
 * {@link #load()} returns nothing.
 */
public interface MapProvider {
    /**
     * @return a short human-readable provider name such as {@code "Google Maps"} or {@code "MapLibre"}.
     */
    String getName();

    /**
     * @return the constant capabilities offered by this provider.
     */
    MapCapabilities getCapabilities();

    /**
     * Loads the provider's map API. The implementation is responsible only for arranging that, once its API is ready,
     * the shared completion callback is triggered exactly once. That callback is a JavaScript global function installed
     * on {@code window} by {@code MapsLoader} under the name {@link MapsLoader#MAP_LOADED_CALLBACK_GLOBAL} <em>before</em>
     * this method is invoked; invoking it runs every queued {@code load()} callback and tears the global down. An
     * implementation therefore never calls back into {@code MapsLoader} directly (except for MapLibre's already-loaded
     * fast path); it merely wires its injected script to fire that global once the API is usable. Google Maps does this
     * via the {@code &callback=} parameter on its API URL, MapLibre via a {@code window.<global>()} call in the injected
     * compatibility module. Because completion is signalled through that global rather than a return value,
     * {@code load()} returns nothing.
     */
    void load();
}
