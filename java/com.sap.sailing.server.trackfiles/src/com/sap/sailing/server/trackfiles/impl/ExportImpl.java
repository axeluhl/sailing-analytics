package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import slash.navigation.base.BaseNavigationFormat;
import slash.navigation.base.BaseNavigationPosition;
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

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Timed;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.trackfiles.TrackFilesDataSource;
import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.server.trackfiles.Export;
import com.sap.sailing.server.trackfiles.common.FormatNotSupportedException;

/**
 * Export data to well-known formats such as GPX, KML etc. Internally, the RouteConverter Library is used.
 * 
 * Should you need to support another format, add this to {@link TrackFilesFormat} and extend the writeFixes method.
 * 
 * @author Fredrik Teschke
 * 
 */
public class ExportImpl implements Export {
    private interface NameReader<E> {
        String getName(E e);
    }

    private interface TrackReader<E, T extends Timed> {// T extends Track<? extends Timed>> {
        Iterable<T> getRawTrack(E e);

        Iterable<T> getTrack(E e);

        IterableLocker getLocker();
    }

    private interface IterableLocker {
        void lock();

        void unlock();
    }

    private class NoIterableLocker implements IterableLocker {
        @Override
        public void lock() {
        }

        @Override
        public void unlock() {
        }
    }

    private interface TrackReaderRetriever<E, T extends Timed> {
        TrackReader<E, T> retrieveTrackReader(E e);
    }

    private class GPSFixTrackReader<E, T extends Timed> implements TrackReader<E, T>, IterableLocker {
        private final Track<T> track;

        GPSFixTrackReader(Track<T> track) {
            this.track = track;
        }

        @Override
        public void lock() {
            track.lockForRead();
        }

        @Override
        public void unlock() {
            track.unlockAfterRead();
        }

        @Override
        public Iterable<T> getTrack(E e) {
            return track.getFixes();
        }

        @Override
        public Iterable<T> getRawTrack(E e) {
            return track.getRawFixes();
        }

        @Override
        public IterableLocker getLocker() {
            return new IterableLocker() {
                @Override
                public void lock() {
                    track.lockForRead();
                }

                @Override
                public void unlock() {
                    track.unlockAfterRead();
                }
            };
        }
    }

    @SuppressWarnings("rawtypes")
    private abstract class RouteConverter {
        abstract BaseRoute convert(BaseRoute route);

        List<BaseRoute> convert(List<BaseRoute> routes) {
            List<BaseRoute> result = new ArrayList<BaseRoute>(routes.size());
            for (BaseRoute route : routes)
                result.add(convert(route));
            return result;
        }
    }

    /**
     * Warning can't be helped, because there is a generic type loop if one sticks to only the abstract types.
     * 
     * @param fixes
     * @param creator
     * @return
     */
    @SuppressWarnings("rawtypes")
    private <I> List<BaseRoute> getRoutes(Map<String, List<I>> fixes, WaypointCreator<I> creator) {
        List<BaseRoute> routes = new ArrayList<BaseRoute>();

        for (String track : fixes.keySet()) {
            // one implementation of BaseRoute has to be chosen
            List<GpxPosition> positions = new ArrayList<GpxPosition>();

            for (I fix : fixes.get(track)) {
                BaseNavigationPosition position = creator.getPosition(fix);
                if (position != null) {
                    positions.add(position.asGpxPosition());
                }
            }
            GpxRoute gpxRoute = new GpxRoute(new Gpx11Format(), RouteCharacteristics.Track, track,
                    Collections.<String> emptyList(), positions);
            routes.add(gpxRoute);
        }
        return routes;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <F extends BaseNavigationFormat<? extends BaseRoute>> void writeRoutesInternal(List<BaseRoute> routes,
            F format, final OutputStream out) throws IOException, FormatNotSupportedException {
        if (format instanceof MultipleRoutesFormat) {
            MultipleRoutesFormat<BaseRoute> mFormat = (MultipleRoutesFormat<BaseRoute>) format;

            // necessary as GpxFormat11.write() seems to flush afterwards, thus
            // closing the HttpServletResponse Outputstream
            mFormat.write((List<BaseRoute>) routes, new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    out.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    out.write(b, off, len);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    out.write(b);
                }
            });
        } else {
            throw new FormatNotSupportedException("Format cannot contain multiple routes per file");
        }
        out.flush();
    }

    private void writeRaceMetaData(TrackedRace race, ZipOutputStream out) {
        PrintWriter pw = new PrintWriter(out);
        DateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        if (race.getStartOfRace() != null)
            pw.println("START_OF_RACE\t" + f.format(race.getStartOfRace().asDate()));
        if (race.getEndOfRace() != null)
            pw.println("END_OF_RACE\t" + f.format(race.getEndOfRace().asDate()));
        if (race.getStartOfTracking() != null)
            pw.println("START_OF_TRACKING\t" + f.format(race.getStartOfTracking().asDate()));
        if (race.getEndOfTracking() != null)
            pw.println("END_OF_TRACKING\t" + f.format(race.getEndOfTracking().asDate()));
        pw.println();
        pw.flush();
    }

    private <E, F extends Timed, T> void writeRaceInternal(TrackFilesFormat format, TrackedRace race,
            boolean dataBeforeAfter, boolean rawFixes, OutputStream out, WaypointCreator<F> creator,
            Iterable<E> elements, NameReader<E> nameReader, TrackReaderRetriever<E, F> trackRetriever)
            throws FormatNotSupportedException, IOException {
        Map<String, List<F>> fixes = new HashMap<String, List<F>>();
        for (E element : elements) {
            List<F> fixList = new ArrayList<F>();

            TimePoint start = race.getStartOfRace() != null ? race.getStartOfRace() : race.getStartOfTracking();
            TimePoint end = race.getEndOfRace() != null ? race.getEndOfRace() : race.getEndOfTracking();

            TrackReader<E, F> trackReader = trackRetriever.retrieveTrackReader(element);
            trackReader.getLocker().lock();
            try {
                Iterable<F> fixesIter = rawFixes ? trackReader.getRawTrack(element) : trackReader.getTrack(element);
                if (fixesIter == null) {
                    continue;
                }
                for (F fix : fixesIter) {
                    // TODO is putting the dynamic iterator result into a list
                    // acceptable? export will most likely only take place when
                    // race is done and over, and tracks do not change, however
                    // this goes against the concept of locking and the iterator
                    // in place
                    // if (fix.getTimePoint().before(race.getStartOfRace()))
                    // continue;
                    // if (fix.getTimePoint().after(race.getEndOfRace()))
                    // break;
                    if (!dataBeforeAfter && fix.getTimePoint().before(start)) {
                        continue;
                    }
                    if (!dataBeforeAfter && fix.getTimePoint().after(end)) {
                        break;
                    }
                    fixList.add(fix);
                }
            } finally {
                trackReader.getLocker().unlock();
            }
            fixes.put(nameReader.getName(element), fixList);
        }
        writeFixes(format, fixes, creator, out);
    }

    /**
     * Several tracks in one file - one name for each track list.
     * 
     * @param format
     * @param fixes
     * @param out
     */
    @SuppressWarnings("rawtypes")
    public <I> void writeFixes(TrackFilesFormat format, Map<String, List<I>> fixes, WaypointCreator<I> creator,
            OutputStream out) throws FormatNotSupportedException, IOException {

        switch (format) {
        case Gpx10:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asGpx10Format();
                }
            }.convert(getRoutes(fixes, creator)), new Gpx10Format(), out);
            break;
        case Gpx11:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asGpx11Format();
                }
            }.convert(getRoutes(fixes, creator)), new Gpx11Format(), out);
            break;
        case Kml20:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asKml20Format();
                }
            }.convert(getRoutes(fixes, creator)), new Kml20Format(), out);
            break;
        case Kml21:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asKml21Format();
                }
            }.convert(getRoutes(fixes, creator)), new Kml21Format(), out);
            break;
        case Kml22:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asKml22Format();
                }
            }.convert(getRoutes(fixes, creator)), new Kml22Format(), out);
            break;
        case Kmz20:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asKmz20Format();
                }
            }.convert(getRoutes(fixes, creator)), new Kmz20Format(), out);
            break;
        case Kmz21:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asKmz21Format();
                }
            }.convert(getRoutes(fixes, creator)), new Kmz21Format(), out);
            break;
        case Kmz22:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asKmz22Format();
                }
            }.convert(getRoutes(fixes, creator)), new Kmz22Format(), out);
            break;
        case MagicMapsIkt:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asMagicMapsIktFormat();
                }
            }.convert(getRoutes(fixes, creator)), new MagicMapsIktFormat(), out);
            break;
        case Ovl:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asOvlFormat();
                }
            }.convert(getRoutes(fixes, creator)), new OvlFormat(), out);
            break;
        case OziExplorerTrack:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asOvlFormat();
                }
            }.convert(getRoutes(fixes, creator)), new OvlFormat(), out);
            break;
        case Tcx1:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asTcx1Format();
                }
            }.convert(getRoutes(fixes, creator)), new Tcx1Format(), out);
            break;
        case Tcx2:
            writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(BaseRoute route) {
                    return route.asTcx2Format();
                }
            }.convert(getRoutes(fixes, creator)), new Tcx2Format(), out);
            break;
        default:
            throw new FormatNotSupportedException(format + " format is not supported");
        }
    }

    /**
     * Reads GPS fixes for competitors from tracked race.
     * 
     * @param format
     * @param race
     * @param out
     * @throws FormatNotSupportedException
     */
    @Override
    public void writeCompetitors(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes,
            OutputStream out) throws FormatNotSupportedException, IOException {

        TrackReaderRetriever<Competitor, GPSFixMoving> retriever = new TrackReaderRetriever<Competitor, GPSFixMoving>() {
            @Override
            public TrackReader<Competitor, GPSFixMoving> retrieveTrackReader(Competitor e) {
                return new GPSFixTrackReader<Competitor, GPSFixMoving>(race.getTrack(e));
            }
        };

        writeRaceInternal(format, race, dataBeforeAfter, rawFixes, out, GPSFixMovingToGpxPosition.INSTANCE, race
                .getRace().getCompetitors(), new NameReader<Competitor>() {
            @Override
            public String getName(Competitor c) {
                return c.getName() + " - " + c.getBoat().getSailID() + " - "
                        + c.getTeam().getNationality().getThreeLetterIOCAcronym();
            }
        }, retriever);
    }

    /**
     * Reads GPS fixes for wind from tracked race.
     * 
     * @param format
     * @param race
     * @param out
     * @throws FormatNotSupportedException
     */
    @Override
    public void writeWind(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes,
            OutputStream out) throws FormatNotSupportedException, IOException {

        TrackReaderRetriever<WindSource, Wind> retriever = new TrackReaderRetriever<WindSource, Wind>() {
            @Override
            public TrackReader<WindSource, Wind> retrieveTrackReader(WindSource e) {
                return new GPSFixTrackReader<WindSource, Wind>(race.getOrCreateWindTrack(e));
            }
        };

        writeRaceInternal(format, race, dataBeforeAfter, rawFixes, out, WindToGpxPosition.INSTANCE,
                race.getWindSources(WindSourceType.TRACK_BASED_ESTIMATION), new NameReader<WindSource>() {
                    @Override
                    public String getName(WindSource s) {
                        return s.name();
                    }
                }, retriever);
    }

    /**
     * Reads GPS fixes for buoys from tracked race.
     * 
     * @param format
     * @param race
     * @param out
     * @throws FormatNotSupportedException
     */
    @Override
    public void writeBuoys(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes,
            OutputStream out) throws FormatNotSupportedException, IOException {

        TrackReaderRetriever<Mark, GPSFix> retriever = new TrackReaderRetriever<Mark, GPSFix>() {
            @Override
            public TrackReader<Mark, GPSFix> retrieveTrackReader(Mark e) {
                return new GPSFixTrackReader<Mark, GPSFix>(race.getOrCreateTrack(e));
            }
        };

        writeRaceInternal(format, race, dataBeforeAfter, rawFixes, out, GPSFixToGpxPosition.INSTANCE, race.getMarks(),
                new NameReader<Mark>() {
                    @Override
                    public String getName(Mark m) {
                        return m.getName();
                    }
                }, retriever);
    }

    /**
     * Reads GPS fixes for maneuvers from tracked race.
     * 
     * @param format
     * @param race
     * @param out
     * @throws FormatNotSupportedException
     */
    @Override
    public void writeManeuvers(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes,
            OutputStream out) throws FormatNotSupportedException, IOException {

        final TimePoint start = race.getStartOfRace() == null ? race.getStartOfTracking() : race.getStartOfRace();
        final TimePoint end = race.getEndOfRace() == null ? race.getEndOfTracking() : race.getEndOfRace();

        TrackReaderRetriever<Competitor, Maneuver> retriever = new TrackReaderRetriever<Competitor, Maneuver>() {
            @Override
            public TrackReader<Competitor, Maneuver> retrieveTrackReader(Competitor e) {
                return new TrackReader<Competitor, Maneuver>() {
                    @Override
                    public Iterable<Maneuver> getRawTrack(Competitor e) {
                        try {
                            return race.getManeuvers(e, start, end, false);
                        } catch (NoWindException e1) {
                            e1.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public Iterable<Maneuver> getTrack(Competitor e) {
                        try {
                            return race.getManeuvers(e, start, end, false);
                        } catch (NoWindException e1) {
                            e1.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    public IterableLocker getLocker() {
                        return new NoIterableLocker();
                    }

                };
            }
        };

        writeRaceInternal(format, race, dataBeforeAfter, rawFixes, out, ManeuverToGpxPosition.INSTANCE, race.getRace()
                .getCompetitors(), new NameReader<Competitor>() {
            @Override
            public String getName(Competitor s) {
                return s.getName();
            }
        }, retriever);
    }

    /**
     * Writes the wanted data of all the races to the output stream. One file per race and data type.
     * 
     * @param data
     * @param format
     * @param races
     * @param dataBeforeAfter
     *            false: do not include data from before the race started and after the race ended
     * @param out
     * @throws FormatNotSupportedException
     */
    @Override
    public void writeRaces(List<TrackFilesDataSource> data, TrackFilesFormat format, List<TrackedRace> races, boolean dataBeforeAfter,
            boolean rawFixes, ZipOutputStream out) throws FormatNotSupportedException {
        // TODO optimize: perhaps gather gpx binary file data in parallel for
        // different races in threads, buffer in byte arrays and join
        for (TrackedRace race : races) {

            String raceName = race.getRace().getName();
            String regattaName = race.getRaceIdentifier().getRegattaName();

            try {
                out.putNextEntry(new ZipEntry(regattaName + "/" + raceName + " - METADATA.txt"));
                writeRaceMetaData(race, out);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (TrackFilesDataSource d : data) {
                try {
                    out.putNextEntry(new ZipEntry(regattaName + "/" + raceName + " - " + d.toString() + "."
                            + format.suffix));

                    switch (d) {
                    case BUOYS:
                        writeBuoys(format, race, dataBeforeAfter, rawFixes, out);
                        break;
                    case COMPETITORS:
                        writeCompetitors(format, race, dataBeforeAfter, rawFixes, out);
                        break;
                    case WIND:
                        writeWind(format, race, dataBeforeAfter, rawFixes, out);
                        break;
                    case MANEUVERS:
                        writeManeuvers(format, race, dataBeforeAfter, rawFixes, out);
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
