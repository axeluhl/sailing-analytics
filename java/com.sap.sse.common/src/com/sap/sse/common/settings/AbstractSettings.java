package com.sap.sse.common.settings;

/**
 * Common superclass for custom Settings used by Components. This kind of Settings is not serializable at all. There are
 * more specific super classes for different serialization needs:
 * <ul>
 * <li>{@link SerializableSettings} can be used for {@link Settings} that need to be Java-serializable and/or
 * GWT-RPC-compatible</li>
 * <li>{@link com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings} support a wide variety of serialization mechanisms by using a generic
 * mechanism to declare the settings that requires that implementers comply to some specific rules</li>
 * </ul>
 *
 */
public abstract class AbstractSettings implements Settings {
}
