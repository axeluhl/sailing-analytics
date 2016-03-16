package com.sap.sse.common.settings.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.ValueConverter;

public abstract class AbstractValueListSetting<T> extends AbstractValueCollectionSetting<T> {
    
    private List<T> values = new ArrayList<>();
    
    public AbstractValueListSetting(String name, AbstractSettings settings, ValueConverter<T> valueConverter) {
        super(name, settings, valueConverter);
    }
    
    @Override
    protected Collection<T> getInnerCollection() {
        return values;
    }
}
