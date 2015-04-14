package com.sap.sailing.domain.common.settings;

/**
 * The settings for a <code>Component</code>. Such settings may be initialized with defaults. Usually they can be edited by a
 * user using a <code>SettingsDialogComponent</code> displayed by a <code>SettingsDialog</code>. The interface is homed in
 * this server-side, shared bundle because settings may as well be handled on the server side, e.g., in order to store them
 * in the user store or apply them to a server-side component.
 * <p>
 * 
 * Settings need to be serialized and de-serialized to/from a {@link String}. This way they can easily be marshaled into a
 * URL or into a cookie as well as the user-specific preferences store. In order to keep URLs with such settings parameters
 * human readable, each setting needs to have a name and a string-based value. Furthermore, the component to which the settings
 * apply needs to provide a String identifier. This way, a generic marshaling method can take over the serialization.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Settings {
//    String serialize();
//    
//    String getType();
}
