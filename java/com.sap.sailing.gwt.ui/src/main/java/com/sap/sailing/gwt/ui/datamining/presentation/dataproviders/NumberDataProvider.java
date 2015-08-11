package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.ArrayList;
import java.util.Collection;

public class NumberDataProvider extends AbstractResultDataProvider<Number> {

    private final Collection<Object> dataKeys;

    public NumberDataProvider() {
        super(Number.class);
        dataKeys = new ArrayList<>();
        dataKeys.add("Double");
        dataKeys.add("Integer");
    }

    @Override
    public Collection<? extends Object> getDataKeys() {
        return dataKeys;
    }

    @Override
    protected Number getData(Number number, Object dataKey) {
        if (!(dataKey instanceof String)) {
            throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
        }
        String dataKeyString = (String) dataKey;
        switch (dataKeyString) {
        case "Double":
            return number.doubleValue();
        case "Integer":
            return number.intValue();
        }
        throw new IllegalArgumentException("The given data key '" + dataKey + "' isn't valid");
    }

}
