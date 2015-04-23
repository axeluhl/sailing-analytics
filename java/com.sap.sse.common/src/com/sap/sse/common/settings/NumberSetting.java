package com.sap.sse.common.settings;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberSetting implements Setting {
    private final BigDecimal number;

    public NumberSetting(BigDecimal number) {
        this.number = number;
    }
    
    public NumberSetting(double number) {
        this.number = new BigDecimal(number);
    }

    public NumberSetting(long number) {
        this.number = new BigDecimal(number);
    }
    
    public NumberSetting(int number) {
        this.number = new BigDecimal(number);
    }
    
    public NumberSetting(BigInteger number) {
        this.number = new BigDecimal(number);
    }

    public NumberSetting(Number obj) {
        if (obj instanceof BigDecimal) {
            this.number = (BigDecimal) obj;
        } else if (obj instanceof BigInteger) {
            this.number = new BigDecimal((BigInteger) obj);
        } else if (obj instanceof Double || obj instanceof Float) {
            this.number = new BigDecimal(((Number) obj).doubleValue());
        } else {
            this.number = new BigDecimal(obj.longValue());
        }
    }

    public BigDecimal getNumber() {
        return number;
    }

    @Override
    public SettingType getType() {
        return SettingType.NUMBER;
    }

    @Override
    public String toString() {
        return ""+number;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((number == null) ? 0 : number.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NumberSetting other = (NumberSetting) obj;
        if (number == null) {
            if (other.number != null)
                return false;
        } else if (!number.equals(other.number))
            return false;
        return true;
    }
}
