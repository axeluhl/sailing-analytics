package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.Sensor;

public class SensorImpl implements Sensor {
    private final String deviceSerialNumber;
    private final String subDeviceId;
    protected SensorImpl(String eviceSerialNumber, String subDeviceId) {
        super();
        this.deviceSerialNumber = eviceSerialNumber;
        this.subDeviceId = subDeviceId;
    }
    
    @Override
    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }
    
    @Override
    public String getSubDeviceId() {
        return subDeviceId;
    }
    
    @Override
    public String toString() {
        return "Transmitter "+getDeviceSerialNumber()+(getSubDeviceId()==null?"":(", device "+getSubDeviceId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((subDeviceId == null) ? 0 : subDeviceId.hashCode());
        result = prime * result + ((deviceSerialNumber == null) ? 0 : deviceSerialNumber.hashCode());
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
        SensorImpl other = (SensorImpl) obj;
        if (subDeviceId == null) {
            if (other.subDeviceId != null)
                return false;
        } else if (!subDeviceId.equals(other.subDeviceId))
            return false;
        if (deviceSerialNumber == null) {
            if (other.deviceSerialNumber != null)
                return false;
        } else if (!deviceSerialNumber.equals(other.deviceSerialNumber))
            return false;
        return true;
    }

}
