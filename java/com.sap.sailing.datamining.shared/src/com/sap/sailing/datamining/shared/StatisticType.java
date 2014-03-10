package com.sap.sailing.datamining.shared;

import com.sap.sse.datamining.components.ElementType;
import com.sap.sse.datamining.shared.Message;
import com.sap.sse.datamining.shared.Unit;

public enum StatisticType {

    Speed(ElementType.Double, Unit.Knots, 2),
    Distance(ElementType.Double, Unit.Meters, 2);

    private final ElementType valueType;
    private final Unit unit;
    private final int valueDecimals;

    private StatisticType(ElementType valueType, Unit unit, int valueDecimals) {
        this.valueType = valueType;
        this.unit = unit;
        this.valueDecimals = valueDecimals;
    }

    public ElementType getValueType() {
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