package com.sap.sailing.server.trackfiles.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private static final Logger log = Logger.getLogger(ExportImpl.class.toString());

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
        abstract BaseRoute convert(GpxRoute route);

        List<BaseRoute> convert(List<GpxRoute> routes) {
            List<BaseRoute> result = new ArrayList<BaseRoute>(routes.size());
            for (GpxRoute route : routes)
                result.add(convert(route));
            return result;
        }
    }

    private class WriteRaceCallable implements Callable<byte[]> {
        private final TrackedRace race;
        private final TrackFilesDataSource data;
        private final TrackFilesFormat format;
        private final boolean dataBeforeAfter;
        private final boolean rawFixes;

        public WriteRaceCallable(TrackedRace race, TrackFilesDataSource data, TrackFilesFormat format,
                boolean dataBeforeAfter, boolean rawFixes) {
            super();
            this.race = race;
            this.data = data;
            this.format = format;
            this.dataBeforeAfter = dataBeforeAfter;
            this.rawFixes = rawFixes;
        }

        public TrackedRace getRace() {
            return race;
        }

        public TrackFilesFormat getFormat() {
            return format;
        }

        public byte[] call() throws Exception {
            switch (data) {
            case BUOYS:
                return writeBuoys(format, race, dataBeforeAfter, rawFixes);
            case COMPETITORS:
                return writeCompetitors(format, race, dataBeforeAfter, rawFixes);
            case WIND:
                return writeWind(format, race, dataBeforeAfter, rawFixes);
            case MANEUVERS:
                return writeManeuvers(format, race, dataBeforeAfter, rawFixes);
            }
            return new byte[0];
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <F extends BaseNavigationFormat<? extends BaseRoute>> byte[] writeRoutesInternal(List<BaseRoute> routes,
            F format) throws IOException, FormatNotSupportedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (format instanceof MultipleRoutesFormat) {
            MultipleRoutesFormat<BaseRoute> mFormat = (MultipleRoutesFormat<BaseRoute>) format;

            // necessary as GpxFormat11.write() seems to flush afterwards, thus
            // closing the HttpServletResponse Outputstream
            mFormat.write((List<BaseRoute>) routes, out);
        } else {
            throw new FormatNotSupportedException("Format cannot contain multiple routes per file");
        }
        return out.toByteArray();
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

    private <E, F extends Timed, T> byte[] writeRaceInternal(TrackFilesFormat format, TrackedRace race,
            boolean dataBeforeAfter, boolean rawFixes, WaypointCreator<F> creator, Iterable<E> elements,
            NameReader<E> nameReader, TrackReaderRetriever<E, F> trackRetriever) throws FormatNotSupportedException,
            IOException {
        List<GpxRoute> routes = new ArrayList<>();
        for (E element : elements) {
            TimePoint start = race.getStartOfRace() != null ? race.getStartOfRace() : race.getStartOfTracking();
            TimePoint end = race.getEndOfRace() != null ? race.getEndOfRace() : race.getEndOfTracking();

            String name = nameReader.getName(element);
            GpxRoute route = new GpxRoute(new Gpx11Format(), RouteCharacteristics.Track, name,
                    Collections.<String> emptyList(), Collections.<GpxPosition> emptyList());
            routes.add(route);

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
                    route.add(i++, creator.getPosition(fix));
                }
            } finally {
                trackReader.getLocker().unlock();
            }
        }
        return writeFixes(format, routes);
    }

    /**
     * Several tracks in one file - one name for each track list.
     * 
     * @param format
     * @param fixes
     * @param out
     */
    // Trying to use generics for abstract BaseRoute causes generic type loop (BaseRoute needs BaseNavigationFormat,
    // which needs BaseRoute as type parameter)
    @SuppressWarnings("rawtypes")
    public byte[] writeFixes(TrackFilesFormat format, List<GpxRoute> routes) throws FormatNotSupportedException,
            IOException {

        switch (format) {
        case Gpx10:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asGpx10Format();
                }
            }.convert(routes), new Gpx10Format());
        case Gpx11:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asGpx11Format();
                }
            }.convert(routes), new Gpx11Format());
        case Kml20:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKml20Format();
                }
            }.convert(routes), new Kml20Format());
        case Kml21:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKml21Format();
                }
            }.convert(routes), new Kml21Format());
        case Kml22:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKml22Format();
                }
            }.convert(routes), new Kml22Format());
        case Kmz20:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKmz20Format();
                }
            }.convert(routes), new Kmz20Format());
        case Kmz21:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKmz21Format();
                }
            }.convert(routes), new Kmz21Format());
        case Kmz22:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asKmz22Format();
                }
            }.convert(routes), new Kmz22Format());
        case MagicMapsIkt:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asMagicMapsIktFormat();
                }
            }.convert(routes), new MagicMapsIktFormat());
        case Ovl:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asOvlFormat();
                }
            }.convert(routes), new OvlFormat());
        case OziExplorerTrack:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asOvlFormat();
                }
            }.convert(routes), new OvlFormat());
        case Tcx1:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asTcx1Format();
                }
            }.convert(routes), new Tcx1Format());
        case Tcx2:
            return writeRoutesInternal(new RouteConverter() {
                BaseRoute convert(GpxRoute route) {
                    return route.asTcx2Format();
                }
            }.convert(routes), new Tcx2Format());
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
    public byte[] writeCompetitors(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter,
            boolean rawFixes) throws FormatNotSupportedException, IOException {

        TrackReaderRetriever<Competitor, GPSFixMoving> retriever = new TrackReaderRetriever<Competitor, GPSFixMoving>() {
            @Override
            public TrackReader<Competitor, GPSFixMoving> retrieveTrackReader(Competitor e) {
                return new GPSFixTrackReader<Competitor, GPSFixMoving>(race.getTrack(e));
            }
        };

        return writeRaceInternal(format, race, dataBeforeAfter, rawFixes, GPSFixMovingToGpxPosition.INSTANCE, race
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
    public byte[] writeWind(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes)
            throws FormatNotSupportedException, IOException {

        TrackReaderRetriever<WindSource, Wind> retriever = new TrackReaderRetriever<WindSource, Wind>() {
            @Override
            public TrackReader<WindSource, Wind> retrieveTrackReader(WindSource e) {
                return new GPSFixTrackReader<WindSource, Wind>(race.getOrCreateWindTrack(e));
            }
        };

        return writeRaceInternal(format, race, dataBeforeAfter, rawFixes, WindToGpxPosition.INSTANCE,
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
    public byte[] writeBuoys(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter, boolean rawFixes)
            throws FormatNotSupportedException, IOException {

        TrackReaderRetriever<Mark, GPSFix> retriever = new TrackReaderRetriever<Mark, GPSFix>() {
            @Override
            public TrackReader<Mark, GPSFix> retrieveTrackReader(Mark e) {
                return new GPSFixTrackReader<Mark, GPSFix>(race.getOrCreateTrack(e));
            }
        };

        return writeRaceInternal(format, race, dataBeforeAfter, rawFixes, GPSFixToGpxPosition.INSTANCE,
                race.getMarks(), new NameReader<Mark>() {
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
    public byte[] writeManeuvers(TrackFilesFormat format, final TrackedRace race, boolean dataBeforeAfter,
            boolean rawFixes) throws FormatNotSupportedException, IOException {

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

        return writeRaceInternal(format, race, dataBeforeAfter, rawFixes, ManeuverToGpxPosition.INSTANCE, race
                .getRace().getCompetitors(), new NameReader<Competitor>() {
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
    public void writeRaces(List<TrackFilesDataSource> data, TrackFilesFormat format, List<TrackedRace> races,
            boolean dataBeforeAfter, boolean rawFixes, ZipOutputStream out) throws FormatNotSupportedException {
        Map<WriteRaceCallable, Future<byte[]>> results = new HashMap<>();
        ExecutorService executor = Executors.newCachedThreadPool();
        for (TrackedRace race : races) {

            try {
                String raceName = race.getRace().getName();
                String regattaName = race.getRaceIdentifier().getRegattaName();
                out.putNextEntry(new ZipEntry(regattaName + "/" + raceName + " - METADATA.txt"));
                writeRaceMetaData(race, out);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error exporting race: " + e.getMessage());
            }

            for (TrackFilesDataSource d : data) {
                WriteRaceCallable callable = new WriteRaceCallable(race, d, format, dataBeforeAfter, rawFixes);
                Future<byte[]> result = executor.submit(callable);
                results.put(callable, result);
            }
        }

        // check for format problems first (can't write to output and then send error)
        for (Future<byte[]> result : results.values()) {
            try {
                result.get();
            } catch (Exception e) {
                if (e instanceof FormatNotSupportedException) {
                    throw (FormatNotSupportedException) e;
                }
            }
        }

        for (WriteRaceCallable callable : results.keySet()) {
            try {
                String raceName = callable.getRace().getRace().getName();
                String regattaName = callable.getRace().getRaceIdentifier().getRegattaName();
                TrackFilesFormat d = callable.getFormat();

                out.putNextEntry(new ZipEntry(regattaName + "/" + raceName + " - " + d.toString() + "." + format.suffix));

                byte[] result = results.get(callable).get();
                out.write(result);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error exporting race: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // TODO is this necessary to clean up the executor allocated resources (e.g. pooled threads)?
        executor.shutdown();
    }
}
