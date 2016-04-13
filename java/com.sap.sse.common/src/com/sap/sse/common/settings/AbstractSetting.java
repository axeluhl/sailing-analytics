package com.sap.sse.common.settings;



public abstract class AbstractSetting implements Setting {
    
    protected final AbstractSettings settings;
    protected final String settingName;

    public AbstractSetting(String settingName, AbstractSettings settings) {
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
