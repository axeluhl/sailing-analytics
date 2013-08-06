package com.sap.sailing.datamining.shared;

public enum WindStrength {
    
    VeryLight(0, 2.5), Light(2.5, 5), Medium(5, 7.5), Strong(7.5, 10), VeryStrong(10, 12);
    
    private double lowerRange;
    private double upperRange;
    
    private WindStrength(double lowerRange, double upperRange) {
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
    }

    public boolean isInRange(double windStrengthInBeaufort) {
        return lowerRange <= windStrengthInBeaufort && upperRange > windStrengthInBeaufort;
    }

    public static WindStrength valueOf(double windStrengthInBeaufort) {
        for (WindStrength windStrength : WindStrength.values()) {
            if (windStrength.isInRange(windStrengthInBeaufort)) {
                return windStrength;
            }
        }
        //Since a speed less than 0 shouldn't be possible, the speed has to be >= 12 and is very strong.
        return VeryStrong;
    }

}
