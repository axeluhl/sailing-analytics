package com.sap.sse.gwt.client.shared.settings;

/**
 * Simple class which wraps the persistable representations of User Settings and Document Settings.
 * 
 * @author Vladislav Chumak
 * 
 * @param <T>
 *            The type of a settings representation
 *
 */
public class PersistableSettingsRepresentations<T> {

    private final T globalSettingsRepresentation;
    private final T contextSpecificSettingsRepresentation;

    public PersistableSettingsRepresentations(T globalSettingsRepresentation, T contextSpecificSettingsRepresentation) {
        this.globalSettingsRepresentation = globalSettingsRepresentation;
        this.contextSpecificSettingsRepresentation = contextSpecificSettingsRepresentation;
    }

    public T getGlobalSettingsRepresentation() {
        return globalSettingsRepresentation;
    }

    public T getContextSpecificSettingsRepresentation() {
        return contextSpecificSettingsRepresentation;
    }

}
