package com.sap.sse.common.settings.base;

import com.sap.sse.common.settings.AbstractSetting;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.CollectionSetting;

public abstract class AbstractListSetting<T> extends AbstractSetting implements CollectionSetting<T> {
    
    public AbstractListSetting(String name, AbstractSettings settings) {
        super(name, settings);
    }
    
    public AbstractListSetting() {
    }
}
