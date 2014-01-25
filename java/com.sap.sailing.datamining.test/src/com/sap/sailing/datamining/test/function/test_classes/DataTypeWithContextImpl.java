package com.sap.sailing.datamining.test.function.test_classes;

public class DataTypeWithContextImpl implements DataTypeWithContext {

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
    public String getRaceName() {
        return raceName;
    }

    @Override
    public int getLegNumber() {
        return legNumber;
    }

}
