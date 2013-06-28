package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import slash.navigation.base.BaseNavigationPosition;
import slash.navigation.base.BaseRoute;
import slash.navigation.base.NavigationFormatParser;
import slash.navigation.base.Wgs84Position;

import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.server.trackfiles.Import;

public class ImportImpl implements Import {
    @Override
    @SuppressWarnings("rawtypes")
    public List<String> getTrackNames(InputStream in) throws IOException {
        List<BaseRoute> routes;
        try {
            routes = new NavigationFormatParser().read(in).getAllRoutes();
        } catch (NullPointerException e) {
            throw new IOException("Couldn't parse file");
        }
        List<String> trackNames = new ArrayList<String>();

        for (BaseRoute route : routes)
            trackNames.add(route.getName());

        return trackNames;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map<String, List<GPSFixMoving>> extractTracks(InputStream in) throws IOException {
        List<BaseRoute> routes = new NavigationFormatParser().read(in).getAllRoutes();
        Map<String, List<GPSFixMoving>> map = new HashMap<String, List<GPSFixMoving>>();

        for (BaseRoute route : routes) {
            List<GPSFixMoving> list = new ArrayList<GPSFixMoving>();

            List<BaseNavigationPosition> positions = (List<BaseNavigationPosition>) route.getPositions();
            for (int i = 0; i < positions.size(); i++) {
                BaseNavigationPosition p = positions.get(i);
                try {
                    Date t = p.getTime().getTime();
                    Double heading = null;
                    if (p instanceof Wgs84Position) {
                        heading = ((Wgs84Position) p).getHeading();
                    }
                    Double speed = p.getSpeed();

                    Position pos = new DegreePosition(p.getLatitude(), p.getLongitude());
                    TimePoint timePoint = new MillisecondsTimePoint(t);

                    SpeedWithBearing tmpSpeedAndBearing = null;

                    if (speed == null && i != 0) {
                        GPSFix tmpFix = new GPSFixImpl(pos, timePoint);
                        tmpSpeedAndBearing = list.get(i - 1).getSpeedAndBearingRequiredToReach(tmpFix);
                        speed = tmpSpeedAndBearing.getKnots();
                    }

                    if (heading == null && i != 0) {
                        if (tmpSpeedAndBearing == null) {
                            GPSFix tmpFix = new GPSFixImpl(pos, timePoint);
                            tmpSpeedAndBearing = list.get(i - 1).getSpeedAndBearingRequiredToReach(tmpFix);
                        }
                        heading = tmpSpeedAndBearing.getBearing().getDegrees();
                    }

                    GPSFixMoving fix = new GPSFixMovingImpl(pos, timePoint, new KnotSpeedWithBearingImpl(
                            speed == null ? 0 : speed, new DegreeBearingImpl(heading == null ? 0 : heading)));

                    list.add(fix);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            map.put(route.getName(), list);
        }

        return map;
    }
}
