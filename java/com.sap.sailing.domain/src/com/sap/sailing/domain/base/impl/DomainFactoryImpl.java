package com.sap.sailing.domain.base.impl;

import java.awt.TrayIcon.MessageType;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Gate;
import com.sap.sailing.domain.base.Nationality;

public class DomainFactoryImpl implements DomainFactory {
    /**
     * Ensure that the <em>same</em> string is used as key that is also used to set the {@link Nationality}
     * object's {@link Nationality#getName() name}.
     */
    private final Map<String, Nationality> nationalityCache;
    
    private final Map<String, Buoy> buoyCache;

    public DomainFactoryImpl() {
        nationalityCache = new HashMap<String, Nationality>();
        buoyCache = new HashMap<String, Buoy>();
    }
    
    @Override
    public Nationality getOrCreateNationality(String nationalityName) {
        synchronized (nationalityCache) {
            Nationality result = nationalityCache.get(nationalityName);
            if (result == null) {
                result = new NationalityImpl(nationalityName, nationalityName);
                nationalityCache.put(nationalityName, result);
            }
            return result;
        }
    }
    
    /**
     * @param id
     *            the ID which is probably also used as the "device name" and the "sail number" in case of an
     *            {@link MessageType#RPD RPD} message
     */
    @Override
    public Buoy getOrCreateBuoy(String id) {
        Buoy result = buoyCache.get(id);
        if (result == null) {
            result = new BuoyImpl(id);
            buoyCache.put(id, result);
        }
        return result;
    }

    @Override
    public Gate createGate(Buoy left, Buoy right, String name) {
       return new GateImpl(left, right, name);
    }

}
