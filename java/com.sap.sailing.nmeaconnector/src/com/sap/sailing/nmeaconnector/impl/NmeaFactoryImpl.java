package com.sap.sailing.nmeaconnector.impl;

import com.sap.sailing.nmeaconnector.NmeaFactory;
import com.sap.sailing.nmeaconnector.NmeaUtil;

public class NmeaFactoryImpl implements NmeaFactory {

    @Override
    public NmeaUtil getUtil() {
        return new NmeaUtilImpl();
    }

}
