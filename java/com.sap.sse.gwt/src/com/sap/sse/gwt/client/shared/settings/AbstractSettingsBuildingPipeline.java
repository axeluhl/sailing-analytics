package com.sap.sse.gwt.client.shared.settings;

import com.sap.sse.common.settings.Settings;

/**
 * Basic implementation for {@link SettingsBuildingPipeline} which provides a implementation of few methods.
 * It is highly recommended to inherit all other implementations from this class.
 * 
 * @author Vladislav Chumak
 * 
 * @see SettingsBuildingPipeline
 *
 */
public abstract class AbstractSettingsBuildingPipeline implements SettingsBuildingPipeline {
    
    /**
     * Conversion helper which is used by this instance for type conversion/serialization
     * between settings objects and JSON Strings.
     */
    protected final SettingsStringConverter settingsStringConverter;
    
    /**
     * Constructs an instance with {@link SettingsStringConverter} as conversion helper between
     * settings objects and its JSON representation.
     */
    public AbstractSettingsBuildingPipeline() {
        this(new SettingsStringConverter());
    }
    
    /**
     * Constructs an instance with a custom conversion helper between
     * settings objects and its JSON representation.
     */
    public AbstractSettingsBuildingPipeline(SettingsStringConverter settingsStringConverter) {
        this.settingsStringConverter = settingsStringConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public<S extends Settings> S getSettingsObject(S defaultSettings, SettingsStrings settingsStrings) {
        SettingsJsons settingsJsons = settingsStringConverter.convertToSettingsJson(settingsStrings);
        return getSettingsObject(defaultSettings, settingsJsons);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends Settings> S getSettingsObject(S defaultSettings) {
        return getSettingsObject(defaultSettings, new SettingsJsons(null, null));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SettingsStringConverter getSettingsStringConverter() {
        return settingsStringConverter;
    }

}
