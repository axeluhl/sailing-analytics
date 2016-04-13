package com.sap.sse.common.settings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sse.common.settings.value.SettingsValue;
import com.sap.sse.common.settings.value.Value;

/**
 * Common superclass for custom Settings used by Components. The following example shows how to implement custom
 * Settings:
 * 
 * <pre>
 * <code>
 * public final class ExampleSettings extends AbstractSettings {
 *     private final StringSetting myString = new UUIDSetting("myString", this);
 *     private final BooleanSetting myBoolean = new BooleanSetting("myBoolean", this);
 *     
 *     // Used to create instances for a component
 *     public RegattaOverviewBaseSettings() {
 *     }
 *     
 *     // Used to create nested settings
 *     public RegattaOverviewBaseSettings(String name, AbstractSettings settings) {
 *         this.event.setValue(event);
 *         this.ignoreLocalSettings.setValue(ignoreLocalSettings);
 *     }
 *     
 *     public String getMyString() {
 *         return this.myString.getValue();
 *     }
 *     
 *     public void setMyString(String myString) {
 *         return this.myString.setValue(myString);
 *     }
 *     
 *     public boolean isMyBoolean() {
 *         return this.myBoolean.getValue();
 *     }
 *     
 *     public void setMyBoolean(boolean myBoolean) {
 *         return this.myBoolean.setValue(myBoolean);
 *     }
 * }
 * </code>
 * </pre>
 * 
 * If a custom Settings class only has {@link Setting} oder {@link Settings} children that are correctly attached by
 * calling their constructor with a name and the parent {@link Settings} you do not need to implement
 * {@link #hashCode()}, {@link #equals(Object)} and {@link #toString()}. There are generic implementations of those methods that are
 * based on the child {@link Setting}s which should be sufficient for most cases.
 *
 */
public abstract class AbstractSettings extends AbstractSetting implements Settings {

    private SettingsValue value;
    private transient Map<String, Setting> childSettings = new HashMap<>();

    public AbstractSettings() {
        value = new SettingsValue();
        addChildSettings();
    }

    public AbstractSettings(String name, AbstractSettings settings) {
        super(name, settings);
        value = (SettingsValue) settings.getValue(name);
        if(value == null) {
            value = new SettingsValue();
            settings.setValue(name, value);
        }
        settings.setValue(name, value);
        addChildSettings();
    }
    
    protected void adoptValue(SettingsValue value) {
        this.value = value;
    }
    
    protected SettingsValue getInnerValueObject() {
        return value;
    }
    
    @GwtIncompatible
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        addChildSettings();
    }
    
    // TODO make abstract when all Settings are ported to the new system
    protected void addChildSettings() {
    }
    
    // TODO make protected
    public Value getValue(String settingName) {
        return value.getValue(settingName);
    }
    
    // TODO make protected
    public void setValue(String settingName, Value value) {
        this.value.setValue(settingName, value);
    }

    protected void addSetting(String name, Setting setting) {
        if (name.contains(Settings.PATH_SEPARATOR)) {
            throw new IllegalArgumentException("\"" + Settings.PATH_SEPARATOR
                    + "\" is currently not allowed for setting names");
        }
        if (childSettings.containsKey(name)) {
            throw new IllegalArgumentException("setting with name " + name + " already added to setting type "
                    + getClass().getSimpleName());
        }
        childSettings.put(name, setting);
    }

    @Override
    public boolean isDefaultValue() {
        for (Setting setting : childSettings.values()) {
            if (!setting.isDefaultValue()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void resetToDefault() {
        for (Setting setting : childSettings.values()) {
            setting.resetToDefault();
        }
    }

    @Override
    public Map<String, Setting> getChildSettings() {
        return Collections.unmodifiableMap(childSettings);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("{(").append(this.getClass().getSimpleName()).append(")");
        boolean first = true;
        for (Map.Entry<String, Setting> childSetting : childSettings.entrySet()) {
            if (!first) {
                sb.append("; ");
            }
            sb.append(childSetting.getKey() + "=" + childSetting.getValue());

            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((childSettings == null) ? 0 : childSettings.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractSettings other = (AbstractSettings) obj;
        if (childSettings == null) {
            if (other.childSettings != null)
                return false;
        } else if (!childSettings.equals(other.childSettings))
            return false;
        return true;
    }
}
