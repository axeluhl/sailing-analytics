package com.sap.sse.common.settings;



public abstract class AbstractSetting implements Setting {
    
    public AbstractSetting(String name, AbstractSettings settings) {
        settings.addSetting(name, this);
    }
    
    public AbstractSetting() {
    }

}
