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

import com.igtimi.IgtimiData.ApparentWindAngle;
import com.igtimi.IgtimiData.ApparentWindSpeed;
import com.igtimi.IgtimiData.CourseOverGround;
import com.igtimi.IgtimiData.Data;
import com.igtimi.IgtimiData.DataMsg;
import com.igtimi.IgtimiData.DataPoint;
import com.igtimi.IgtimiData.GNSS_Position;
import com.igtimi.IgtimiData.GNSS_Quality;
import com.igtimi.IgtimiData.GNSS_Sat_Count;
import com.igtimi.IgtimiData.Heading;
import com.igtimi.IgtimiData.HeadingMagnetic;
import com.igtimi.IgtimiData.SpeedOverGround;
import com.igtimi.IgtimiStream.Msg;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KilometersPerHourSpeedImpl;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.igtimiadapter.DataPointVisitor;
import com.sap.sailing.domain.igtimiadapter.MsgVisitor;
import com.sap.sailing.domain.igtimiadapter.Sensor;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWA;
import com.sap.sailing.domain.igtimiadapter.datatypes.AWS;
import com.sap.sailing.domain.igtimiadapter.datatypes.BatteryLevel;
import com.sap.sailing.domain.igtimiadapter.datatypes.COG;
import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsAltitude;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsLatLong;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsQualityHdop;
import com.sap.sailing.domain.igtimiadapter.datatypes.GpsQualitySatCount;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDG;
import com.sap.sailing.domain.igtimiadapter.datatypes.HDGM;
import com.sap.sailing.domain.igtimiadapter.datatypes.Log;
import com.sap.sailing.domain.igtimiadapter.datatypes.SOG;
import com.sap.sailing.domain.igtimiadapter.datatypes.Type;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.DegreeBearingImpl;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * Can convert JSON messages from the original Riot API, such as resource data or web socket
 * text messages, as well as binary Protocol Buffer (protobuf) messages in {@link Msg} format
 * into {@link Fix} objects.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class FixFactory {
    private static final Logger logger = Logger.getLogger(FixFactory.class.getName());
    
    public Iterable<Fix> createFixes(JSONObject sensorsJson) {
        final List<Fix> result = new ArrayList<>();
        for (final Entry<Object, Object> e : sensorsJson.entrySet()) {
            final String deviceSerialNumber = (String) e.getKey();
            final JSONObject typesJson = (JSONObject) e.getValue();
            Util.addAll(createFixesForTypes(deviceSerialNumber, typesJson), result);
        }
        return result;
    }

    private Iterable<Fix> createFixesForTypes(String deviceSerialNumber, JSONObject typesJson) {
        final List<Fix> result = new ArrayList<>();
        for (Entry<Object, Object> fixTypeAndFixesJson : typesJson.entrySet()) {
            final String fixTypeAndOptionalSensorId = (String) fixTypeAndFixesJson.getKey();
            final String[] fixTypeAndOptionalColonSeparatedSensorsSubId = fixTypeAndOptionalSensorId.split(":");
            try {
                int fixType = Integer.valueOf(fixTypeAndOptionalColonSeparatedSensorsSubId[0]);
                JSONObject fixesJson = (JSONObject) fixTypeAndFixesJson.getValue();
                JSONArray timePointsMillis = (JSONArray) fixesJson.get("t");
                int fixIndex = 0;
                for (Object timePointMillis : timePointsMillis) {
                    if (timePointMillis != null) {
                        TimePoint timePoint = new MillisecondsTimePoint(((Number) timePointMillis).longValue());
                        Map<Integer, Object> valuesPerSubindex = new HashMap<>();
                        int i=1;
                        JSONArray values;
                        while ((values=(JSONArray) fixesJson.get(""+i)) != null) {
                            valuesPerSubindex.put(i, values.get(fixIndex));
                            i++;
                        }
                        Sensor sensor = new SensorImpl(deviceSerialNumber,
                                fixTypeAndOptionalColonSeparatedSensorsSubId.length < 2 ? 0
                                        : Long.valueOf(fixTypeAndOptionalColonSeparatedSensorsSubId[1]));
                        final Type ft = Type.valueOf(fixType);
                        if (ft != null) {
                            final Fix fix = createFix(sensor, ft, timePoint, valuesPerSubindex);
                            if (fix != null) {
                                result.add(fix);
                                fixIndex++;
                            }
                        }
                    }
                }
            } catch (NumberFormatException e) {
                // maybe archived_resources?
                logger.warning("Couldn't parse "+fixTypeAndOptionalColonSeparatedSensorsSubId[0]+
                        " into a number; ignoring this record");
            }
        }
        return result;
    }
    
    /**
     * @return {@code null} in case the fix parser cannot make sense of the {@code valuesPerSubindex}, e.g., because
     *         it's empty.
     */
    private Fix createFix(Sensor sensor, Type fixType, TimePoint timePoint, Map<Integer, Object> valuesPerSubindex) {
        try {
            Constructor<? extends Fix> constructor = fixType.getFixClass().getConstructor(TimePoint.class, Sensor.class, Map.class);
            Fix fix = constructor.newInstance(timePoint, sensor, valuesPerSubindex);
            return fix;
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.log(Level.SEVERE, "Internal error trying to find fix constructor for fix type "+
                    fixType+" with class "+fixType.getFixClass()+" or problem creating fix from data "+valuesPerSubindex+": "+
                    e.getMessage());
            return null;
        }
    }
    
    public Iterable<Fix> createFixes(Msg message) {
        final List<Fix> fixes = new ArrayList<>(); // the fixes extracted from the message
        MsgVisitor.accept(message, new MsgVisitor() {
            @Override
            public void handleData(Data data) {
                for (final DataMsg dataMsg : data.getDataList()) {
                    final String serialNumber = dataMsg.getSerialNumber();
                    for (final DataPoint dataPoint : dataMsg.getDataList()) {
                        DataPointVisitor.accept(dataPoint, new DataPointVisitor<Void>() {
                            @Override
                            public Void handleAwa(ApparentWindAngle awa) {
                                fixes.add(new AWA(TimePoint.of(awa.getTimestamp()), getSensor(serialNumber), new DegreeBearingImpl(awa.getValue())));
                                return null;
                            }
    
                            @Override
                            public Void handleAws(ApparentWindSpeed aws) {
                                fixes.add(new AWS(TimePoint.of(aws.getTimestamp()), getSensor(serialNumber), new KilometersPerHourSpeedImpl(aws.getValue())));
                                return null;
                            }
    
                            @Override
                            public Void handleCog(CourseOverGround cog) {
                                fixes.add(new COG(TimePoint.of(cog.getTimestamp()), getSensor(serialNumber), new DegreeBearingImpl(cog.getValue())));
                                return null;
                            }
    
                            @Override
                            public Void handleHdg(Heading hdg) {
                                fixes.add(new HDG(TimePoint.of(hdg.getTimestamp()), getSensor(serialNumber), new DegreeBearingImpl(hdg.getValue())));
                                return null;
                            }
    
                            @Override
                            public Void handleHdgm(HeadingMagnetic hdgm) {
                                fixes.add(new HDGM(TimePoint.of(hdgm.getTimestamp()), getSensor(serialNumber), new DegreeBearingImpl(hdgm.getValue())));
                                return null;
                            }
    
                            @Override
                            public Void handlePos(GNSS_Position pos) {
                                final Sensor sensor = getSensor(serialNumber);
                                fixes.add(new GpsLatLong(TimePoint.of(pos.getTimestamp()), sensor, new DegreePosition(pos.getLatitude(), pos.getLongitude())));
                                fixes.add(new GpsAltitude(TimePoint.of(pos.getTimestamp()), sensor, new MeterDistance(pos.getAltitude())));
                                return null;
                            }
    
                            @Override
                            public Void handleSatq(GNSS_Quality hdop) {
                                fixes.add(new GpsQualityHdop(TimePoint.of(hdop.getTimestamp()), getSensor(serialNumber), new MeterDistance(hdop.getValue())));
                                return null;
                            }
    
                            @Override
                            public Void handleSatc(GNSS_Sat_Count satCount) {
                                fixes.add(new GpsQualitySatCount(TimePoint.of(satCount.getTimestamp()), getSensor(serialNumber), satCount.getValue()));
                                return null;
                            }
    
                            @Override
                            public Void handleNum(com.igtimi.IgtimiData.Number num) {
                                // This is expected to represent the battery state of charge (SOC) in percent
                                fixes.add(new BatteryLevel(TimePoint.of(num.getTimestamp()), getSensor(serialNumber), num.getValue()));
                                return null;
                            }
    
                            @Override
                            public Void handleSog(SpeedOverGround sog) {
                                fixes.add(new SOG(TimePoint.of(sog.getTimestamp()), getSensor(serialNumber), new KilometersPerHourSpeedImpl(sog.getValue())));
                                return null;
                            }
                            
                            @Override
                            public Void handleLog(com.igtimi.IgtimiData.Log log) {
                                fixes.add(new Log(TimePoint.of(log.getTimestamp()), getSensor(serialNumber), log.getMessage(), log.getPriority()));
                                return null;
                            }
                        });
                    }
                }
            }
        });
        return fixes;
    }
    
    private Sensor getSensor(String serialNumber) {
        return Sensor.create(serialNumber, /* sub-device */ 0);
    }

}
