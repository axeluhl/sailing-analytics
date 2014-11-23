package com.sap.sailing.server.trackfiles.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import slash.navigation.base.BaseNavigationFormat;
import slash.navigation.base.BaseRoute;
import slash.navigation.base.MultipleRoutesFormat;
import slash.navigation.base.RouteCharacteristics;
import slash.navigation.gpx.Gpx10Format;
import slash.navigation.gpx.Gpx11Format;
import slash.navigation.gpx.GpxPosition;
import slash.navigation.gpx.GpxRoute;
import slash.navigation.kml.Kml20Format;
import slash.navigation.kml.Kml21Format;
import slash.navigation.kml.Kml22Format;
import slash.navigation.kml.Kmz20Format;
import slash.navigation.kml.Kmz21Format;
import slash.navigation.kml.Kmz22Format;
import slash.navigation.mm.MagicMapsIktFormat;
import slash.navigation.ovl.OvlFormat;
import slash.navigation.tcx.Tcx1Format;
import slash.navigation.tcx.Tcx2Format;

import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;

public abstract class AbstractDataRetriever implements DataRetriever {

    public abstract Collection<GpxRoute> getRoutes(TrackFilesFormat format, TrackedRace race, boolean dataBeforeAfter,
            boolean rawFixes) throws FormatNotSupportedException, IOException;

    // Trying to use generics for abstract BaseRoute causes generic type loop
    // (BaseRoute needs BaseNavigationFormat,
    // which needs BaseRoute as type parameter)
    @SuppressWarnings("rawtypes")
    @Override
    public byte[] getData(TrackFilesFormat format, TrackedRace race, boolean dataBeforeAfter, boolean rawFixes)
            throws FormatNotSupportedException, IOException {
        Collection<GpxRoute> routes = getRoutes(format, race, dataBeforeAfter, rawFixes);

        switch (format) {
        case Gpx10:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asGpx10Format();
                }
            }.convert(routes), new Gpx10Format());
        case Gpx11:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asGpx11Format();
                }
            }.convert(routes), new Gpx11Format());
        case Kml20:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKml20Format();
                }
            }.convert(routes), new Kml20Format());
        case Kml21:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKml21Format();
                }
            }.convert(routes), new Kml21Format());
        case Kml22:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKml22Format();
                }
            }.convert(routes), new Kml22Format());
        case Kmz20:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKmz20Format();
                }
            }.convert(routes), new Kmz20Format());
        case Kmz21:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKmz21Format();
                }
            }.convert(routes), new Kmz21Format());
        case Kmz22:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKmz22Format();
                }
            }.convert(routes), new Kmz22Format());
        case MagicMapsIkt:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asMagicMapsIktFormat();
                }
            }.convert(routes), new MagicMapsIktFormat());
        case Ovl:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asOvlFormat();
                }
            }.convert(routes), new OvlFormat());
        case OziExplorerTrack:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asOvlFormat();
                }
            }.convert(routes), new OvlFormat());
        case Tcx1:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asTcx1Format();
                }
            }.convert(routes), new Tcx1Format());
        case Tcx2:
            return getRoutesAsBytes(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asTcx2Format();
                }
            }.convert(routes), new Tcx2Format());
        default:
            throw new FormatNotSupportedException(format + " format is not supported");
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <F extends BaseNavigationFormat<? extends BaseRoute>> byte[] getRoutesAsBytes(List<BaseRoute> routes,
            F format) throws IOException, FormatNotSupportedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (format instanceof MultipleRoutesFormat) {
            MultipleRoutesFormat<BaseRoute> mFormat = (MultipleRoutesFormat<BaseRoute>) format;

            mFormat.write((List<BaseRoute>) routes, out);
        } else {
            throw new FormatNotSupportedException("Format cannot contain multiple routes per file");
        }
        return out.toByteArray();
    }

    protected <E, F extends Timed, T> Collection<GpxRoute> getRoutes(TrackedRace race, boolean dataBeforeAfter,
            boolean rawFixes, WaypointCreator<F> creator, Iterable<E> elements, NameReader<E> nameReader,
            TrackReaderRetriever<E, F> trackRetriever) throws FormatNotSupportedException, IOException {
        List<GpxRoute> routes = new ArrayList<>();
        for (E element : elements) {
            TimePoint start = race.getStartOfRace() != null ? race.getStartOfRace() : race.getStartOfTracking();
            TimePoint end = race.getEndOfRace() != null ? race.getEndOfRace() : race.getEndOfTracking();

            String name = nameReader.getName(element);
            GpxRoute route = new GpxRoute(new Gpx11Format(), RouteCharacteristics.Track, name,
                    Collections.<String> emptyList(), new ArrayList<GpxPosition>());

            TrackReader<E, F> trackReader = trackRetriever.retrieveTrackReader(element);
            trackReader.getLocker().lock();
            try {
                Iterable<F> fixesIter = rawFixes ? trackReader.getRawTrack(element) : trackReader.getTrack(element);
                if (fixesIter == null) {
                    continue;
                }
                int i = 0;
                for (F fix : fixesIter) {
                    if (!dataBeforeAfter && fix.getTimePoint().before(start)) {
                        continue;
                    }
                    if (!dataBeforeAfter && fix.getTimePoint().after(end)) {
                        break;
                    }
                    GpxPosition position = creator.getPosition(fix);
                    if (position != null) {
                        route.add(i++, position);
                    }
                }
            } finally {
                trackReader.getLocker().unlock();
            }

            if (!route.getPositions().isEmpty()) {
                routes.add(route);
            }
        }
        return routes;
    }
}
