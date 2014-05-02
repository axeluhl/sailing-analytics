package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import slash.common.type.CompactCalendar;
import slash.navigation.base.BaseNavigationPosition;
import slash.navigation.base.BaseRoute;
import slash.navigation.base.NavigationFormatParser;
import slash.navigation.base.ParserResult;
import slash.navigation.base.Wgs84Position;
import slash.navigation.gpx.Gpx11Format;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.TimeRange;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.impl.TimeRangeImpl;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.server.trackfiles.Import;

/**
 * Currently can only import GPX v1.1 files. See bug 1933 for a detailed discussion.
 * @author Fredrik Teschke
 *
 */
public class ImportImpl implements Import {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void importFixes(InputStream inputStream, FixCallback callback, boolean inferSpeedAndBearing) throws IOException {
        //TODO for now we can't use this, as this invokes GPSBabel -> license problem (bug 1933)
//        @SuppressWarnings("rawtypes")
//        List<BaseRoute> routes = new NavigationFormatParser().read(inputStream).getAllRoutes();
        
        //TODO dirty hack, because no public read method for inputstream and custom list of formats
        NavigationFormatParser parser = new NavigationFormatParser();
        List<BaseRoute> routes;
        try {
            Method m = NavigationFormatParser.class.getDeclaredMethod("read", InputStream.class, Integer.TYPE,
                    CompactCalendar.class, List.class);
            m.setAccessible(true);
            ParserResult result = (ParserResult) m.invoke(parser, inputStream, 1024 * 1024, null,
                    Arrays.asList(new Gpx11Format()));
            if (result == null) {
                throw new IOException("Could not find routes in file - only understand GPX v1.1");
            }
            routes = result.getAllRoutes();
        } catch (Exception e) {
            throw new IOException(e);
        }
        
        for (BaseRoute route : routes) {
            List<? extends BaseNavigationPosition> positions = (List<? extends BaseNavigationPosition>) route.getPositions();
            String routeName = route.getName();
            int numberOfFixes = route.getPositionCount();
            route.ensureIncreasingTime();
            TimeRange timeRange = null;
            if (numberOfFixes > 0) {
                long fromMillis = route.getPosition(0).getTime().getTimeInMillis();
                long toMillis = route.getPosition(numberOfFixes-1).getTime().getTimeInMillis();
                timeRange = TimeRangeImpl.create(fromMillis, toMillis);
            }
            GPSFix previousFix = null;
            for (BaseNavigationPosition p : positions) {
                try {
                    Date t = p.getTime().getTime();
                    Double heading = null;
                    if (p instanceof Wgs84Position) {
                        heading = ((Wgs84Position) p).getHeading();
                    }
                    Double speed = p.getSpeed();

                    Position pos = new DegreePosition(p.getLatitude(), p.getLongitude());
                    TimePoint timePoint = new MillisecondsTimePoint(t);

                    SpeedWithBearing speedWithBearing = null;
                    
                    if (speed != null && heading != null) {
                        speedWithBearing = new KnotSpeedWithBearingImpl(speed, new DegreeBearingImpl(heading));
                    } else if (inferSpeedAndBearing && previousFix != null) {
                        GPSFix tmpFix = new GPSFixImpl(pos, timePoint);
                        speedWithBearing = previousFix.getSpeedAndBearingRequiredToReach(tmpFix);
                    }
                    
                    GPSFix fix = null;
                    if (speedWithBearing != null) {
                        fix = new GPSFixMovingImpl(pos, timePoint, speedWithBearing);
                    } else {
                        fix = new GPSFixImpl(pos, timePoint);
                    }
                    
                    callback.addFix(fix, numberOfFixes, timeRange, routeName);
                    previousFix = fix;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
