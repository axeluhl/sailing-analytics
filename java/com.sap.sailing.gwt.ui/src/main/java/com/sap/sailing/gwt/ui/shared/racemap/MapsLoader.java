package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.user.client.Window;
import com.sap.sse.common.Util;

/**
 * The {@link #load(Runnable, String)} method can be used by clients to request the loading of the Google Maps API.
 * The callback passed will be invoked immediately if the API has already been loaded (e.g., by another
 * client call to the {@link #load(Runnable, String)} method within the same frame / document); it will be queued
 * for invocation by a Google Maps API callback function registered otherwise. This callback function
 * is injected at most once when the {@link #load(Runnable, String)} method is invoked for the first time and
 * will trigger all callbacks registered through the {@link #load(Runnable, String)} method until the maps API
 * invokes the callback registered.
 */
public class MapsLoader {
    /**
     * Note: If you use 3, it will take the newest stable available. We want that, although we didn't test with that yet!
     * Google Release notes: https://developers.google.com/maps/documentation/javascript/releases.
     * Subscribe to https://groups.google.com/forum/#!forum/google-maps-js-api-v3-notify for change notifications.
     */
    public final static String API_VERSION = "3";
    
    /**
     * The required Google Maps libraries; a comma-separated list. See https://developers.google.com/maps/documentation/javascript/libraries
     * for more details. Examples: <tt>drawing,geometry,places,visualization</tt>
     */
    public final static String LIBRARIES = "drawing,geometry";

    /**
     * The version of the vendored MapLibre GL JS distribution shipped under {@code js/maps/vendor/maplibre-gl/}.
     */
    private final static String MAPLIBRE_VERSION = "5.9.0";

    /**
     * The relative URL of the Google-Maps-compatibility facade ES module. The query parameter is a cache-busting
     * marker that is bumped whenever the facade changes.
     */
    private final static String MAPS_COMPAT_MODULE_URL = "./js/maps/gwt-maps-maplibre-compat.js?v=race-map-feedback-17";

    /**
     * The name of the {@code window} global through which both the Google Maps API and the compatibility ES module
     * invoke {@link #callback()}. For the Google path it is passed as the {@code &callback=} URL parameter; for the
     * MapLibre path it is referenced from the injected module text. Only one path runs per page load, so a single
     * shared name suffices; it is installed via {@link #installGlobalCallback()} and removed via
     * {@link #clearGlobalCallback()}.
     */
    private final static String MAP_LOADED_CALLBACK_GLOBAL = "mapLoadedCallback";

    private static boolean loading = false;
    private static boolean loaded = false;
    private static final Set<Runnable> callbacks = new HashSet<>();
    
    private MapsLoader() {
    }

    /**
     * @param callback must not be {@code null}.
     */
    public static void load(Runnable callback, String authenticationParams) {
        if (loaded) {
            Scheduler.get().scheduleDeferred(() -> callback.run());
        } else {
            callbacks.add(callback);
            if (!loading) {
                loading = true;
                final boolean mapLibreRequested = isMapLibreRequested();
                if (mapLibreRequested) {
                    loadMapLibre();
                } else {
                    installGlobalCallback();
                    final ScriptElement scriptElement = Document.get().createScriptElement();
                    scriptElement.setSrc("https://maps.googleapis.com/maps/api/js?v="+API_VERSION+"&" + authenticationParams
                            + "&libraries="+LIBRARIES+"&callback="+MAP_LOADED_CALLBACK_GLOBAL);
                    Document.get().getHead().appendChild(scriptElement);
                }
            }
        }
    }

    public static boolean isMapLibreRequested() {
        return Util.equalsWithNull("maplibre", Window.Location.getParameter("maps"));
    }

    /**
     * Loads MapLibre GL JS plus the Google-Maps-compatible facade from {@code js/maps/}, then fires the queued
     * callbacks via {@link #callback()}. Triggered by {@code ?maps=maplibre} in the page URL. If MapLibre and the
     * Google-Maps facade are already present in the window, the callbacks are fired right away; otherwise the
     * MapLibre stylesheet and script are injected, followed by the compatibility ES module which finally invokes
     * the callback.
     */
    private static void loadMapLibre() {
        if (isMapLibreReady()) {
            callback();
        } else {
            final LinkElement css = Document.get().createLinkElement();
            css.setRel("stylesheet");
            css.setHref("./js/maps/vendor/maplibre-gl/" + MAPLIBRE_VERSION + "/maplibre-gl.css");
            Document.get().getHead().appendChild(css);
            loadScript("./js/maps/vendor/maplibre-gl/" + MAPLIBRE_VERSION + "/maplibre-gl.js", () -> {
                final ScriptElement module = Document.get().createScriptElement();
                module.setType("module");
                installMapsCompatModule(module);
                Document.get().getHead().appendChild(module);
            });
        }
    }

    /**
     * Creates a {@link ScriptElement} for the given {@code src}, wires {@code onLoad} to its {@code onload} event and
     * a hard-failing handler to its {@code onerror} event, and appends it to the document head so that loading starts.
     */
    private static void loadScript(final String src, final Runnable onLoad) {
        final ScriptElement scriptElement = Document.get().createScriptElement();
        scriptElement.setSrc(src);
        setOnLoad(scriptElement, onLoad);
        Document.get().getHead().appendChild(scriptElement);
    }

    /**
     * @return {@code true} if both MapLibre GL JS and the Google-Maps-compatible facade are already available on the
     * window, meaning no further script injection is required.
     */
    private static native boolean isMapLibreReady() /*-{
        return !!($wnd.maplibregl && $wnd.maplibregl.Map &&
                $wnd.google && $wnd.google.maps && $wnd.google.maps.Map);
    }-*/;

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

    /**
     * Populates the given module script element with the source that imports and installs the Google-Maps-compatible
     * facade and, once installed, invokes {@link #callback()}. The callback is exposed as the
     * {@value #MAP_LOADED_CALLBACK_GLOBAL} global (see {@link #installGlobalCallback()}) that the module body calls
     * after {@code installGwtMapsCompat()} has run.
     */
    private static void installMapsCompatModule(final ScriptElement scriptElement) {
        installGlobalCallback();
        scriptElement.setText(
                "import { installGwtMapsCompat } from '" + MAPS_COMPAT_MODULE_URL + "';\n" +
                "installGwtMapsCompat();\n" +
                "window." + MAP_LOADED_CALLBACK_GLOBAL + "();\n");
    }

    /**
     * Installs {@link #callback()} as the {@value #MAP_LOADED_CALLBACK_GLOBAL} {@code window} global, wrapped via
     * {@code $entry} so that it enters the GWT event loop correctly. Used by both the Google Maps API callback and the
     * compatibility ES module callback, which reach {@link #callback()} through this same global.
     */
    private static native void installGlobalCallback() /*-{
        $wnd[@com.sap.sailing.gwt.ui.shared.racemap.MapsLoader::MAP_LOADED_CALLBACK_GLOBAL] = $entry(function() {
            @com.sap.sailing.gwt.ui.shared.racemap.MapsLoader::callback()();
        });
    }-*/;

    private static void callback() {
        loaded = true;
        loading = false;
        callbacks.forEach(Runnable::run);
        callbacks.clear();
        clearGlobalCallback();
    }

    /**
     * Removes the {@value #MAP_LOADED_CALLBACK_GLOBAL} {@code window} global installed by
     * {@link #installGlobalCallback()}.
     */
    private static native void clearGlobalCallback() /*-{
        $wnd[@com.sap.sailing.gwt.ui.shared.racemap.MapsLoader::MAP_LOADED_CALLBACK_GLOBAL] = null;
    }-*/;
}
