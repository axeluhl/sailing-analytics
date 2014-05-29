package com.sap.sailing.server.trackfiles.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import slash.common.type.CompactCalendar;
import slash.navigation.base.BaseNavigationPosition;
import slash.navigation.base.BaseRoute;
import slash.navigation.base.NavigationFormat;
import slash.navigation.base.NavigationFormatParser;
import slash.navigation.base.NavigationFormats;
import slash.navigation.base.ParserResult;
import slash.navigation.base.Wgs84Position;
import slash.navigation.bcr.MTP0607Format;
import slash.navigation.bcr.MTP0809Format;
import slash.navigation.copilot.CoPilot6Format;
import slash.navigation.copilot.CoPilot7Format;
import slash.navigation.copilot.CoPilot8Format;
import slash.navigation.copilot.CoPilot9Format;
import slash.navigation.fpl.GarminFlightPlanFormat;
import slash.navigation.gopal.GoPal3RouteFormat;
import slash.navigation.gopal.GoPal5RouteFormat;
import slash.navigation.gpx.BrokenGpx10Format;
import slash.navigation.gpx.BrokenGpx11Format;
import slash.navigation.gpx.Gpx10Format;
import slash.navigation.gpx.Gpx11Format;
import slash.navigation.itn.TomTom5RouteFormat;
import slash.navigation.itn.TomTom8RouteFormat;
import slash.navigation.klicktel.KlickTelRouteFormat;
import slash.navigation.kml.BrokenKml21Format;
import slash.navigation.kml.BrokenKml21LittleEndianFormat;
import slash.navigation.kml.BrokenKml22BetaFormat;
import slash.navigation.kml.BrokenKml22Format;
import slash.navigation.kml.BrokenKmz21Format;
import slash.navigation.kml.BrokenKmz21LittleEndianFormat;
import slash.navigation.kml.Igo8RouteFormat;
import slash.navigation.kml.Kml20Format;
import slash.navigation.kml.Kml21Format;
import slash.navigation.kml.Kml22BetaFormat;
import slash.navigation.kml.Kml22Format;
import slash.navigation.kml.Kmz20Format;
import slash.navigation.kml.Kmz21Format;
import slash.navigation.kml.Kmz22BetaFormat;
import slash.navigation.kml.Kmz22Format;
import slash.navigation.lmx.NokiaLandmarkExchangeFormat;
import slash.navigation.mm.MagicMaps2GoFormat;
import slash.navigation.mm.MagicMapsIktFormat;
import slash.navigation.mm.MagicMapsPthFormat;
import slash.navigation.nmea.BrokenNmeaFormat;
import slash.navigation.nmea.MagellanExploristFormat;
import slash.navigation.nmea.MagellanRouteFormat;
import slash.navigation.nmea.NmeaFormat;
import slash.navigation.nmn.NavigatingPoiWarnerFormat;
import slash.navigation.nmn.Nmn4Format;
import slash.navigation.nmn.Nmn5Format;
import slash.navigation.nmn.Nmn6FavoritesFormat;
import slash.navigation.nmn.Nmn6Format;
import slash.navigation.nmn.Nmn7Format;
import slash.navigation.nmn.NmnRouteFormat;
import slash.navigation.ovl.OvlFormat;
import slash.navigation.simple.BrokenHaicomLoggerFormat;
import slash.navigation.simple.BrokenNavilinkFormat;
import slash.navigation.simple.ColumbusV900ProfessionalFormat;
import slash.navigation.simple.ColumbusV900StandardFormat;
import slash.navigation.simple.GlopusFormat;
import slash.navigation.simple.GoRiderGpsFormat;
import slash.navigation.simple.GpsTunerFormat;
import slash.navigation.simple.GroundTrackFormat;
import slash.navigation.simple.HaicomLoggerFormat;
import slash.navigation.simple.Iblue747Format;
import slash.navigation.simple.KienzleGpsFormat;
import slash.navigation.simple.KompassFormat;
import slash.navigation.simple.NavilinkFormat;
import slash.navigation.simple.OpelNaviFormat;
import slash.navigation.simple.QstarzQ1000Format;
import slash.navigation.simple.Route66Format;
import slash.navigation.simple.SygicAsciiFormat;
import slash.navigation.simple.SygicUnicodeFormat;
import slash.navigation.simple.WebPageFormat;
import slash.navigation.tcx.Tcx1Format;
import slash.navigation.tcx.Tcx2Format;
import slash.navigation.tour.TourFormat;
import slash.navigation.viamichelin.ViaMichelinFormat;
import slash.navigation.wbt.WintecWbt201Tk1Format;
import slash.navigation.wbt.WintecWbt201Tk2Format;
import slash.navigation.zip.ZipFormat;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.trackimport.FormatNotSupportedException;
import com.sap.sailing.domain.tracking.impl.GPSFixImpl;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;
import com.sap.sailing.server.trackfiles.common.BaseGPSFixImporterImpl;

/**
 * Only supports such navigation formats for which RouteConverter implements own parser.
 * Other formats are supported by RouteConverter via GPSBabel, for which there is a licensing
 * issue (see to Bug 1933).
 * 
 * For a list of these formats, see {@link NavigationFormats#SUPPORTED_FORMATS} (specifically,
 * the part of the static block directly below labeled "//self-implemented formats".
 * 
 * @author Fredrik Teschke
 *
 */
public class RouteConverterGPSFixImporterImpl extends BaseGPSFixImporterImpl {
    /**
     * A list of the track file formats that RouteConverter can parse out of the box.
     * @see NavigationFormats#SUPPORTED_FORMATS
     */
    private final static List<Class<? extends NavigationFormat<?>>> SUPPORTED_FORMATS =
            Arrays.<Class<? extends NavigationFormat<?>>>asList(
                    // self-implemented formats
                    NmeaFormat.class,
                    MTP0809Format.class,
                    MTP0607Format.class,
                    TomTom8RouteFormat.class,
                    TomTom5RouteFormat.class,
                    Kml20Format.class,
                    Kmz20Format.class,
                    Kml21Format.class,
                    Kmz21Format.class,
                    Kml22BetaFormat.class,
                    Kmz22BetaFormat.class,
                    Igo8RouteFormat.class,
                    Kml22Format.class,
                    Kmz22Format.class,
                    Gpx10Format.class,
                    Gpx11Format.class,
                    Nmn7Format.class,
                    Nmn6FavoritesFormat.class,
                    Nmn6Format.class,
                    Nmn5Format.class,
                    Nmn4Format.class,
                    WebPageFormat.class,
                    GpsTunerFormat.class,
                    HaicomLoggerFormat.class,
                    CoPilot6Format.class,
                    CoPilot7Format.class,
                    CoPilot8Format.class,
                    CoPilot9Format.class,
                    Route66Format.class,
                    KompassFormat.class,
                    GlopusFormat.class,
                    ColumbusV900ProfessionalFormat.class,
                    ColumbusV900StandardFormat.class,
                    QstarzQ1000Format.class,
                    Iblue747Format.class,
                    SygicAsciiFormat.class,
                    SygicUnicodeFormat.class,
                    MagicMapsPthFormat.class,
                    GoPal3RouteFormat.class,
                    GoPal5RouteFormat.class,
                    OvlFormat.class,
                    TourFormat.class,
                    ViaMichelinFormat.class,
                    MagicMapsIktFormat.class,
                    MagicMaps2GoFormat.class,
                    MagellanExploristFormat.class,
                    MagellanRouteFormat.class,
                    Tcx1Format.class,
                    Tcx2Format.class,
                    NokiaLandmarkExchangeFormat.class,
                    KlickTelRouteFormat.class,
                    GarminFlightPlanFormat.class,
                    WintecWbt201Tk1Format.class,
                    WintecWbt201Tk2Format.class,
                    NavilinkFormat.class,
                    GoRiderGpsFormat.class,
                    KienzleGpsFormat.class,
                    GroundTrackFormat.class,
                    OpelNaviFormat.class,
                    NavigatingPoiWarnerFormat.class,
                    NmnRouteFormat.class,
                    ZipFormat.class,

                    // second try for broken files
                    BrokenNmeaFormat.class,
                    BrokenHaicomLoggerFormat.class,
                    BrokenGpx10Format.class,
                    BrokenGpx11Format.class,
                    BrokenKml21Format.class,
                    BrokenKml21LittleEndianFormat.class,
                    BrokenKmz21Format.class,
                    BrokenKmz21LittleEndianFormat.class,
                    BrokenKml22BetaFormat.class,
                    BrokenKml22Format.class,
                    BrokenNavilinkFormat.class
                    );
    
    /**
     * Copied from {@link NavigationFormatParser#getFormatInstances}.
     * @param restrictToWritableFormats
     * @return
     */
    @SuppressWarnings("rawtypes")
    private static List<NavigationFormat> getFormatInstances(boolean restrictToWritableFormats) {
        List<NavigationFormat> formats = new ArrayList<NavigationFormat>();
        for (Class<? extends NavigationFormat> formatClass : SUPPORTED_FORMATS) {
            try {
                NavigationFormat format = formatClass.newInstance();
                if (restrictToWritableFormats && format.isSupportsWriting() ||
                        !restrictToWritableFormats && format.isSupportsReading())
                    formats.add(format);
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot instantiate " + formatClass, e);
            }
        }
        return formats;
    }
    
    @SuppressWarnings("rawtypes")
    private static List<NavigationFormat> SUPPORTED_READ_FORMATS = getFormatInstances(false);
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void importFixes(InputStream inputStream, Callback callback, boolean inferSpeedAndBearing)
            throws IOException, FormatNotSupportedException {
        //TODO dirty hack, because no public read method for inputstream and custom list of formats
        NavigationFormatParser parser = new NavigationFormatParser();
        List<BaseRoute> routes;
        try {
            Method m = NavigationFormatParser.class.getDeclaredMethod("read", InputStream.class, Integer.TYPE,
                    CompactCalendar.class, List.class);
            m.setAccessible(true);
            ParserResult result = (ParserResult) m.invoke(parser, inputStream, 1024 * 1024, null, SUPPORTED_READ_FORMATS);
            if (result == null) {
                throw new FormatNotSupportedException();
            }
            routes = result.getAllRoutes();
        } catch (Exception e) {
            throw new IOException(e);
        }
        
        for (BaseRoute route : routes) {
            List<? extends BaseNavigationPosition> positions = (List<? extends BaseNavigationPosition>) route.getPositions();
            String routeName = route.getName();
            route.ensureIncreasingTime();
            callback.startTrack(routeName, null);
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
                    
                    if (speed != null && heading != null) {
                        SpeedWithBearing speedWithBearing = new KnotSpeedWithBearingImpl(
                                speed, new DegreeBearingImpl(heading));
                        addFixAndInfer(callback, inferSpeedAndBearing, new GPSFixMovingImpl(pos, timePoint, speedWithBearing));
                    } else {
                        addFixAndInfer(callback, inferSpeedAndBearing, new GPSFixImpl(pos, timePoint));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Iterable<String> getSupportedFileExtensions() {
        //TODO the list is not yet complete
        return Arrays.asList("gpx", "kml", "kmz");
    }

    @Override
    public String getType() {
        return "RouteConverter";
    }
}
