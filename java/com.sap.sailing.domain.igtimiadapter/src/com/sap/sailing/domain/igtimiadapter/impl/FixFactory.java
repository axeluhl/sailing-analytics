package com.sap.sailing.domain.igtimiadapter.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.igtimiadapter.Sensor;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;

public class FixFactory {
    private static final Logger logger = Logger.getLogger(FixFactory.class.getName());
    
    public Iterable<Fix> createFixes(JSONObject sensorsJson) {
        List<Fix> result = new ArrayList<>();
        for (Entry<Object, Object> e : sensorsJson.entrySet()) {
            String deviceSerialNumber = (String) e.getKey();
            JSONObject typesJson = (JSONObject) e.getValue();
            for (Entry<Object, Object> fixTypeAndFixesJson : typesJson.entrySet()) {
                final String fixTypeAndOptionalSensorId = (String) fixTypeAndFixesJson.getKey();
                final String[] fixTypeAndOptionalColonSeparatedSensorsSubId = fixTypeAndOptionalSensorId.split(":");
                int fixType = Integer.valueOf(fixTypeAndOptionalColonSeparatedSensorsSubId[0]);
                JSONObject fixesJson = (JSONObject) fixTypeAndFixesJson.getValue();
                JSONArray timePointsMillis = (JSONArray) fixesJson.get("t");
                int fixIndex = 0;
                for (Object timePointMillis : timePointsMillis) {
                    TimePoint timePoint = new MillisecondsTimePoint(((Number) timePointMillis).longValue());
                    Map<Integer, Object> valuesPerSubindex = new HashMap<>();
                    int i=1;
                    JSONArray values;
                    while ((values=(JSONArray) fixesJson.get(""+i)) != null) {
                        valuesPerSubindex.put(i, (Number) values.get(fixIndex));
                        i++;
                    }
                    Sensor sensor = new SensorImpl(deviceSerialNumber,
                            fixTypeAndOptionalColonSeparatedSensorsSubId.length < 2 ? 0
                                    : Long.valueOf(fixTypeAndOptionalColonSeparatedSensorsSubId[1]));
                    Fix fix = createFix(sensor, Type.valueOf(fixType), timePoint, valuesPerSubindex);
                    result.add(fix);
                    fixIndex++;
                }
            }
        }
        return result;
    }
    
    /**
     * @param lastDataJson expected to use "c1", "c2", ... instead of "1" and "2" for the sensor parameters, and
     * "timestamp" instead of "t" for the timestamp. The values are not provided as arrays but as single values.
     */
    public Iterable<Fix> createFixesFromLastDatum(JSONObject lastDataJson, Type type) {
        List<Fix> result = new ArrayList<>();
        JSONArray latestDataArray = (JSONArray) lastDataJson.get("latest_data");
        for (Object d : latestDataArray) {
            JSONObject latestDatum = (JSONObject) ((JSONObject) d).get("latest_datum");
            String serialNumber = (String) latestDatum.get("serial_number");
            // there is a field called "resource_id" but we're currently ignoring it because not all fixes have one and we currently
            // don't have a use for it
            Map<Integer, Object> valuesPerSubindex = new HashMap<>();
            int i=1;
            Object c_i;
            while ((c_i=latestDatum.get("c"+i)) != null) {
                valuesPerSubindex.put(i, c_i);
                i++;
            }
            Sensor sensor = new SensorImpl(serialNumber, /* sensor ID */ 0); // Note: the sensor ID isn't obvious from neither the URL nor the response; using 0 as default
            TimePoint timestamp = new MillisecondsTimePoint(((Number) latestDatum.get("timestamp")).longValue());
            result.add(createFix(sensor, type, timestamp, valuesPerSubindex));
        }
        return result;
    }

    private Fix createFix(Sensor sensor, Type fixType, TimePoint timePoint, Map<Integer, Object> valuesPerSubindex) {
        try {
            Constructor<? extends Fix> constructor = fixType.getFixClass().getConstructor(TimePoint.class, Sensor.class, Map.class);
            Fix fix = constructor.newInstance(timePoint, sensor, valuesPerSubindex);
            return fix;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.log(Level.SEVERE, "Internal error trying to find fix constructor for fix type "+fixType+" with class "+fixType.getFixClass());
            throw new RuntimeException(e);
        }
    }
}
