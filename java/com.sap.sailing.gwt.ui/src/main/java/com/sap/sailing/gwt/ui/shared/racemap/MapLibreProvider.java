package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.ScriptElement;

/**
 * {@link MapProvider} backed by MapLibre GL JS plus a Google-Maps-compatible facade. Unlike Google Maps, MapLibre can
 * rotate satellite imagery, so it supports satellite in a rotated (wind-up) view, and it provides nautical chart /
 * sea-mark overlays.
 * <p>
 * Loading needs no authentication. If MapLibre and the Google-Maps facade are already present in the window, the shared
 * {@link MapsLoader#callback()} fires right away; otherwise the MapLibre stylesheet and script are injected, followed by
 * the compatibility ES module which invokes the shared {@link MapsLoader#MAP_LOADED_CALLBACK_GLOBAL} global installed by
 * {@link MapsLoader}. Because {@link #isMapLibreReady()}, {@link #loadScript(String, Runnable)} and its
 * {@link #setOnLoad(ScriptElement, Runnable)} JSNI helper are used only for the MapLibre load sequence, they live here
 * as private members.
 */
public class MapLibreProvider implements MapProvider {
    private static final MapCapabilities CAPABILITIES = new MapCapabilities() {
        @Override
        public boolean supportsSatelliteInRotatedView() {
            return true;
        }

        @Override
        public boolean supportsNauticalChartOverlay() {
            return true;
        }
    };

    @Override
    public String getName() {
        return "MapLibre";
    }

    @Override
    public MapCapabilities getCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public void load() {
        if (isMapLibreReady()) {
            MapsLoader.callback();
        } else {
            final LinkElement css = Document.get().createLinkElement();
            css.setRel("stylesheet");
            css.setHref("./js/maps/vendor/maplibre-gl/" + MapsLoader.MAPLIBRE_VERSION + "/maplibre-gl.css");
            Document.get().getHead().appendChild(css);
            loadScript("./js/maps/vendor/maplibre-gl/" + MapsLoader.MAPLIBRE_VERSION + "/maplibre-gl.js", () -> {
                final ScriptElement module = Document.get().createScriptElement();
                module.setType("module");
                module.setText("import { installGwtMapsCompat } from '" + MapsLoader.MAPS_COMPAT_MODULE_URL + "';\n"
                        + "installGwtMapsCompat();\n" + "window." + MapsLoader.MAP_LOADED_CALLBACK_GLOBAL + "();\n");
                Document.get().getHead().appendChild(module);
            });
        }
    }

    /**
     * @return {@code true} if both MapLibre GL JS and the Google-Maps-compatible facade are already available on the
     * window, meaning no further script injection is required. Private because the readiness probe is specific to this
     * provider's load sequence.
     */
    private static native boolean isMapLibreReady() /*-{
        return !!($wnd.maplibregl && $wnd.maplibregl.Map &&
                $wnd.google && $wnd.google.maps && $wnd.google.maps.Map);
    }-*/;

    /**
     * Creates a {@link ScriptElement} for the given {@code src}, wires {@code onLoad} to its {@code onload} event and a
     * hard-failing handler to its {@code onerror} event, and appends it to the document head so that loading starts.
     */
    private static void loadScript(final String src, final Runnable onLoad) {
        final ScriptElement scriptElement = Document.get().createScriptElement();
        scriptElement.setSrc(src);
        setOnLoad(scriptElement, onLoad);
        Document.get().getHead().appendChild(scriptElement);
    }

    /**
     * Wires {@code onLoad} to the {@code onload} event of the given script element and installs an {@code onerror}
     * handler that throws so failures surface instead of being swallowed silently.
     */
    private static native void setOnLoad(ScriptElement scriptElement, Runnable onLoad) /*-{
        scriptElement.onload = $entry(function() {
            onLoad.@java.lang.Runnable::run()();
        });
        scriptElement.onerror = function() {
            throw new Error('Failed to load ' + scriptElement.src);
        };
    }-*/;
}
