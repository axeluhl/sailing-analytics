package com.sap.sse.datamining.test.domain.impl;

import com.sap.sse.datamining.test.domain.Test_Named;

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
