package com.sap.sailing.domain.base.configuration.impl;

import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;

public class DeviceConfigurationMapImpl extends ConcurrentHashMap<DeviceConfigurationMatcher, DeviceConfiguration> {

    private static final long serialVersionUID = -8009136964737670452L;
    
    private interface Action {
        DeviceConfiguration call(DeviceConfigurationMatcher matcher);
    }
    
    public DeviceConfiguration getByMatch(DeviceConfigurationIdentifier key) {
        return doByMatch(key, new Action() {
            @Override
            public DeviceConfiguration call(DeviceConfigurationMatcher match) {
                return get(match);
            }
        });
    }

    public DeviceConfiguration removeByMatch(DeviceConfigurationIdentifier key) {
        return doByMatch(key, new Action() {
            @Override
            public DeviceConfiguration call(DeviceConfigurationMatcher match) {
                return remove(match);
            }
        });
    }
    
    private DeviceConfiguration doByMatch(DeviceConfigurationIdentifier key, Action action) {
        DeviceConfigurationMatcher match = null;
        for (Entry<DeviceConfigurationMatcher, DeviceConfiguration> entry : entrySet()) {
            if (entry.getKey().matches(key)) {
                if (match == null || match.getMatchingRank() > entry.getKey().getMatchingRank()) {
                    match = entry.getKey();
                }
            }
        }
        if (match != null) {
            return action.call(match);
        } else {
            return null;
        }
    }

}
