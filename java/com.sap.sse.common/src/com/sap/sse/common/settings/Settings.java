package com.sap.sse.common.settings;

import java.util.Map;

/**
 * A generic format to keep the non-default settings for a <code>Component</code>. Such settings may be initialized with
 * defaults. Usually they can be edited by a user using a <code>SettingsDialogComponent</code> displayed by a
 * <code>SettingsDialog</code>. The interface is homed in this server-side, shared bundle because settings may as well
 * be handled on the server side, e.g., in order to store them in the user store or to apply them to a server-side
 * component.
 * <p>
 * 
 * Settings need to be serialized and de-serialized to/from different formats. In a URL they should try to follow
 * reasonable URL parameter encoding conventions and be human readable/editable. They may also be stored in a cookie or
 * in the user-specific preferences store where JSON may be a useful format to keep them as a {@link String}.
 * <p>
 * 
 * In order to enable mapping the settings to URL parameters, each setting needs to have a name and a {@link Setting}
 * value that in turn can be serialized accordingly. Furthermore, the component to which the settings apply needs to
 * provide a String identifier. This way, generic serializers/de-serializers can take over the serialization from/to the
 * various formats.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Settings extends Setting {
    /**
     * Obtains all settings that have non-default values and therefore are required for the later re-construction
     * of the settings object when passed the resulting map.
     */
    Map<String, Setting> getNonDefaultSettings();
}
