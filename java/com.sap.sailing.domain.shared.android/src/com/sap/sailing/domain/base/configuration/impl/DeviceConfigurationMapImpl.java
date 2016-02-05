package com.sap.sailing.domain.base.configuration.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationIdentifier;
import com.sap.sailing.domain.base.configuration.DeviceConfigurationMatcher;

/**
 * <p>
 * Helper class providing a {@link Map} of {@link DeviceConfigurationMatcher}s associated with their
 * {@link DeviceConfiguration}.
 * </p>
 * 
 * <p>
 * When searching for a match, the highest ranked {@link DeviceConfigurationMatcher} (i.e. lowest number) wins.
 * </p>
 */
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
                if (match == null) {
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
