package com.sap.sse.datamining.test.domain.impl;

import com.sap.sse.datamining.test.domain.Test_Boat;

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
