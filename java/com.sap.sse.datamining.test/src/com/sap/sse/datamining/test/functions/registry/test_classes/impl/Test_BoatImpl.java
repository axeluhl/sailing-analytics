package com.sap.sse.datamining.test.functions.registry.test_classes.impl;

import com.sap.sse.datamining.test.functions.registry.test_classes.Test_Boat;

public class Test_BoatImpl implements Test_Boat {

    private String sailID;

    public Test_BoatImpl(String sailID) {
        this.sailID = sailID;
    }

    @Override
    public String getSailID() {
        return sailID;
    }

}
