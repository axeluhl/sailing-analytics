package com.sap.sailing.datamining.shared;

public final class Components {

    public enum GrouperType {
        Dimensions, Custom
    }

    public enum AggregatorType {
        Sum("Sum"), Average("Average"), Median("Median");
        
        private final String name;

        private AggregatorType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static enum ValueType {
        Integer, Double
    }

    public enum StatisticType {

        Speed(ValueType.Double, "speed in knots", Unit.Knots, 2),
        Distance_TrackedLegOfCompetitor(ValueType.Double, "distance in meters", Unit.Meters, 2);

        private final ValueType valueType;
        private final String signifier;
        private final Unit unit;
        private final int valueDecimals;

        private StatisticType(ValueType valueType, String signifier, Unit unit, int valueDecimals) {
            this.valueType = valueType;
            this.signifier = signifier;
            this.unit = unit;
            this.valueDecimals = valueDecimals;
        }

        public ValueType getValueType() {
            return valueType;
        }
        
        public String getSignifier() {
            return signifier;
        }
        
        public Unit getUnit() {
            return unit;
        }
        
        public int getValueDecimals() {
            return valueDecimals;
        }

    }

    private Components() {
    }

}
