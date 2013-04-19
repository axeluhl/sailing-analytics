package com.sap.sailing.nmeaconnector;

import com.sap.sailing.nmeaconnector.impl.NmeaFactoryImpl;

public interface NmeaFactory {
    static NmeaFactory INSTANCE = new NmeaFactoryImpl();
    
    NmeaUtil getUtil();
}
