package com.sap.sailing.datamining.shared;

public enum StatisticType {
    
    DataAmount(ValueType.Integer),
    Speed(ValueType.Double);
    
    public static enum ValueType {Integer, Double}
    
    private ValueType valueType;

    private StatisticType(ValueType valueType) {
        this.valueType = valueType;
    }

    public ValueType getValueType() {
        return valueType;
    }

}
