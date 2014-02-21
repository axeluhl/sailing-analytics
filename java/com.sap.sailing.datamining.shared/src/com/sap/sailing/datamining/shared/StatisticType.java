package com.sap.sailing.datamining.shared;

import com.sap.sse.datamining.components.ValueType;
import com.sap.sse.datamining.shared.Message;
import com.sap.sse.datamining.shared.Unit;

public enum StatisticType {

    Speed(ValueType.Double, Unit.Knots, 2),
    Distance(ValueType.Double, Unit.Meters, 2);

    private final ValueType valueType;
    private final Unit unit;
    private final int valueDecimals;

    private StatisticType(ValueType valueType, Unit unit, int valueDecimals) {
        this.valueType = valueType;
        this.unit = unit;
        this.valueDecimals = valueDecimals;
    }

    public ValueType getValueType() {
        return valueType;
    }
    
    public Message getSignifierMessage() {
        switch (this) {
        case Distance:
            return Message.Distance;
        case Speed:
            return Message.Speed;
        }
        throw new IllegalArgumentException("No message available for the statistic type '" + this + "'");
    }
    
    public Unit getUnit() {
        return unit;
    }
    
    public Message getUnitMessage() {
        switch (unit) {
        case Knots:
            return Message.Knots;
        case Meters:
            return Message.Meters;
        case None:
            return null;
        }
        throw new IllegalArgumentException("No message available for the unit '" + unit + "'");
    }
    
    public int getValueDecimals() {
        return valueDecimals;
    }

}