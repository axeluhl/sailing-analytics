package com.sap.sse.common.settings.generic;

/**
 * GWT can't use reflection. When deserializing Settings that contain {@link Enum} values there must be come conversion
 * of raw String values to the actual enum constants. So an instance of {@link StringToEnumConverter} must be provided
 * for every {@link Enum} based {@link Setting} objects.
 *
 * @param <T>
 *            Type of the created enum instances
 */
public interface StringToEnumConverter<T extends Enum<T>> {
    T fromString(String stringValue);
}
