package com.sap.sailing.gwt.ui.shared.racemap;

/**
 * Describes the feature set of a {@link MapProvider}. UI code queries these capabilities instead of checking the
 * provider identity, so that adding a new provider does not require touching every conditional. Capabilities are
 * constant per provider.
 */
public interface MapCapabilities {
    /**
     * @return {@code true} if the provider can display satellite imagery while the map is shown in a rotated
     * ("wind-up") view. Google Maps cannot rotate satellite imagery and therefore returns {@code false}, whereas
     * MapLibre supports satellite in any orientation.
     */
    boolean supportsSatelliteInRotatedView();

    /**
     * @return {@code true} if the provider can render a nautical chart / sea-mark overlay. Google Maps has no such
     * support and returns {@code false}; MapLibre provides it via its map options.
     */
    boolean supportsNauticalChartOverlay();
}
