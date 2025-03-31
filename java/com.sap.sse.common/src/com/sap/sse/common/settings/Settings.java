package com.sap.sse.common.settings;

/**
 * A generic format to keep the settings for a <code>Component</code>. Such settings may be initialized with defaults.
 * Usually they can be edited by a user using a <code>SettingsDialogComponent</code> displayed by a
 * <code>SettingsDialog</code>. The interface is homed in this server-side, shared bundle because settings may as well
 * be handled on the server side, e.g., in order to store them in the user store or to apply them to a server-side
 * component.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Settings {
}
