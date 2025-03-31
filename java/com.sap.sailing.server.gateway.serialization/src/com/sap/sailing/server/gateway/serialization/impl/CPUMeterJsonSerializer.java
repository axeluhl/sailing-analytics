package com.sap.sailing.server.gateway.serialization.impl;

import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.Duration;
import com.sap.sse.metering.CPUMeter;
import com.sap.sse.shared.json.JsonSerializer;

public class CPUMeterJsonSerializer implements JsonSerializer<CPUMeter> {
    private static final String CPU_USER_MILLIS = "cpuUserMillis";
    private static final String CPU_SYSTEM_MILLIS = "cpuSystemMillis";
    private static final String CPU_TOTAL_MILLIS = "cpuTotalMillis";
    private static final String CPU_KEY = "key";
    private static final String CPU_BY_KEY = "byKey";
    private static final String CPU_TOTALS = "totals";
    
    @Override
    public JSONObject serialize(CPUMeter cpuMeter) {
        final JSONObject result = new JSONObject();
        final JSONArray byKey = new JSONArray();
        result.put(CPU_BY_KEY, byKey);
        for (final Entry<String, Duration> e : cpuMeter.getTotalCPUTimesInUserModeByKey().entrySet()) {
            final JSONObject usage = new JSONObject();
            byKey.add(usage);
            usage.put(CPU_KEY, e.getKey());
            putUserSystemAndTotal(usage, e.getValue(), cpuMeter.getTotalCPUTimeInSystemMode(e.getKey()), cpuMeter.getTotalCPUTime(e.getKey()));
        }
        final JSONObject totals = new JSONObject();
        result.put(CPU_TOTALS, totals);
        putUserSystemAndTotal(totals, cpuMeter.getTotalCPUTimeInUserMode(), cpuMeter.getTotalCPUTimeInSystemMode(), cpuMeter.getTotalCPUTime());
        return result;
    }

    private void putUserSystemAndTotal(JSONObject into, Duration totalCPUTimeInUserMode, Duration totalCPUTimeInSystemMode, Duration totalCPUTime) {
        into.put(CPU_USER_MILLIS, totalCPUTimeInUserMode.asMillis());
        into.put(CPU_SYSTEM_MILLIS, totalCPUTimeInSystemMode.asMillis());
        into.put(CPU_TOTAL_MILLIS, totalCPUTime.asMillis());
    }
}
