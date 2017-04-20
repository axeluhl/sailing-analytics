package com.sap.sailing.nmeaconnector.impl;

import java.io.Reader;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.nmeaconnector.NmeaFactory;
import com.sap.sailing.nmeaconnector.NmeaUtil;

public class NmeaFactoryImpl implements NmeaFactory {

    @Override
    public NmeaUtil getUtil() {
        return new NmeaUtilImpl();
    }

    @Override
    public Iterable<Wind> readWind(Reader reader) {
        // TODO Auto-generated method stub
        return null;
    }

}
