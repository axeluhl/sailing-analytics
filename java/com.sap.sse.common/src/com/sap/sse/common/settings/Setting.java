package com.sap.sse.common.settings;

/**
 * A setting as contained as values in the map returned by {@link AbstractSettings#getSettings()}. This generic
 * view on a {@link AbstractSettings} object can then be used by one or more serializer / de-serializer combos
 * for different serialized formats such as URLs, human-readable strings, obfuscated strings, JSON, or
 * a binary format.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface Setting {
    SettingType getType();
}
