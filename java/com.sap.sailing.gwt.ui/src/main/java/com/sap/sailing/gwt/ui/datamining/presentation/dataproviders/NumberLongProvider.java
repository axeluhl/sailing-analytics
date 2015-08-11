package com.sap.sailing.gwt.ui.datamining.presentation.dataproviders;

import java.util.Collection;
import java.util.Collections;

public class NumberLongProvider extends AbstractResultDataProvider<Number> {

    public NumberLongProvider() {
        super(Number.class);
    }

    @Override
    public Collection<? extends Object> getDataKeys() {
        return Collections.emptyList();
    }

    @Override
    protected Number getData(Number number, Object dataKey) {
        return Math.round(number.doubleValue());
    }

}
