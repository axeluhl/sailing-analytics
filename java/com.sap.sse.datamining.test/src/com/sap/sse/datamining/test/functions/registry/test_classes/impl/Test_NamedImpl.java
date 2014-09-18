package com.sap.sse.datamining.test.functions.registry.test_classes.impl;

import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Named;

public abstract class Test_NamedImpl implements Test_Named {

    private String name;

    public Test_NamedImpl(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
