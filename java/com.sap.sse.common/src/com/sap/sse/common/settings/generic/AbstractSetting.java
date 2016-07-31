package com.sap.sse.common.settings.generic;



public abstract class AbstractSetting implements Setting {
    
    protected transient final AbstractGenericSerializableSettings settings;
    protected transient final String settingName;

    public AbstractSetting(String settingName, AbstractGenericSerializableSettings settings) {
        this.settingName = settingName;
        this.settings = settings;
        if(settings != null) {
            settings.addSetting(settingName, this);
        }
    }
    
    public AbstractSetting() {
        this(null, null);
    }
}
