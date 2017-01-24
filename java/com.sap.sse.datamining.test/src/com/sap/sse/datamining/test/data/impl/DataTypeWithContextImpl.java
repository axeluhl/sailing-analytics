package com.sap.sse.datamining.test.data.impl;

//This class needs to extend SuperType to increase the test coverage
public class DataTypeWithContextImpl extends SuperType implements DataTypeWithContext, ExtendingInterface {

    private String regattaName;
    private String raceName;
    private int legNumber;

    public DataTypeWithContextImpl(String regattaName, String raceName, int legNumber) {
        this.regattaName = regattaName;
        this.raceName = raceName;
        this.legNumber = legNumber;
    }

    @Override
    public String getRegattaName() {
        return regattaName;
    }
    
    @Override
    public int getRaceNameLength() {
        return getRaceName().length();
    }

    @Override
    public String getRaceName() {
        return raceName;
    }

    @Override
    public int getLegNumber() {
        return legNumber;
    }

    @Override
    public int getSpeedInKnots() {
        return 0;
    }
}
