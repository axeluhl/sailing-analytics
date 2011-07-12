package com.sap.sailing.declination.impl;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.sap.sailing.declination.Declination;
import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.util.QuadTree;

/**
 * Manages resources in which declinations can be stored for off-line look-up. Time resolution is
 * one year. The declination values are expected to be provided for mid-year (June 30). The annual
 * change can be used to extrapolate to other times of year.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class DeclinationStore {
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Returns <code>null</code> if no stored declinations exist for the <code>year</code> requested.
     */
    public QuadTree<Declination> getStoredDeclinations(int year) throws IOException, ClassNotFoundException, ParseException {
        Declination record;
        QuadTree<Declination> result = null;
        InputStream is = getInputStreamForYear(year);
        if (is != null) {
            result = new QuadTree<Declination>();
            ObjectInput in = new ObjectInputStream(is);
            while ((record = readExternal(in)) != null) {
                result.put(record.getPosition(), record);
            }
        }
        return result;
    }
    
    private String getResourceForYear(int year) {
        String filename = getFilenameForYear(year);
        return "resources/" + filename;
    }

    private String getFilenameForYear(int year) {
        String filename = "declination-"+year+".txt";
        return filename;
    }
    
    private InputStream getInputStreamForYear(int year) {
        return getClass().getResourceAsStream("/"+getFilenameForYear(year));
    }
    
    public void writeExternal(Declination record, ObjectOutput out) throws IOException {
        out.writeUTF(dateFormatter.format(record.getTimePoint().asDate()));
        out.writeDouble(record.getPosition().getLatDeg());
        out.writeDouble(record.getPosition().getLngDeg());
        out.writeDouble(record.getBearing().getDegrees());
        out.writeDouble(record.getAnnualChange().getDegrees());
    }

    /**
     * @return <code>null</code> if EOF has been reached
     */
    public Declination readExternal(ObjectInput in) throws IOException, ClassNotFoundException, ParseException {
        try {
            TimePoint timePoint = new MillisecondsTimePoint(dateFormatter.parse(in.readUTF()).getTime());
            double lat = in.readDouble();
            double lng = in.readDouble();
            Position position = new DegreePosition(lat, lng);
            Bearing bearing = new DegreeBearingImpl(in.readDouble());
            Bearing annualChange = new DegreeBearingImpl(in.readDouble());
            return new DeclinationRecordImpl(position, timePoint, bearing, annualChange);
        } catch (EOFException e) {
            return null;
        }
    }

    private void fetchAndAppendDeclination(TimePoint timePoint, Position position, NOAAImporter importer,
            ObjectOutput out) throws IOException {
        Declination declination = null;
        // re-try three times
        for (int i=0; i<3; i++) {
            try {
                declination = importer.importRecord(position, timePoint);
                break;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                if (i<2) {
                    System.out.println("re-trying");
                }
            }
        }
        if (declination != null) {
            writeExternal(declination, out);
            out.flush();
        }
    }

    private void run(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException, ParseException {
        if (args.length == 0) {
            usage();
        } else {
            int fromYear = Integer.valueOf(args[0]);
            int toYear = Integer.valueOf(args[1]);
            if (toYear < fromYear) {
                usage();
            } else {
                double grid = Double.valueOf(args[2]);
                NOAAImporter importer = new NOAAImporter();
                for (int year = fromYear; year <= toYear; year++) {
                    QuadTree<Declination> storedDeclinations = getStoredDeclinations(year);
                    // append if file already exists
                    File fileForYear = new File(getResourceForYear(year));
                    ObjectOutput out;
                    if (fileForYear.exists()) {
                        out = new ObjectOutputStream(new FileOutputStream(getResourceForYear(year), /* append */ true)) {
                            @Override
                            protected void writeStreamHeader() throws IOException {
                                // no header in append mode
                            }
                        };
                    } else {
                        out = new ObjectOutputStream(new FileOutputStream(getResourceForYear(year)));
                    }
                    int month = 6;
                    Calendar cal = new GregorianCalendar(year, month, /* dayOfMonth */ 1);
                    TimePoint timePoint = new MillisecondsTimePoint(cal.getTimeInMillis());
                    for (double lat = 0; lat < 90; lat += grid) {
                        System.out.println("Date: " + year + "/" + (month + 1) + ", Latitude: " + lat);
                        for (double lng = 0; lng < 180; lng += grid) {
                            Position point = new DegreePosition(lat, lng);
                            Declination existingDeclinationRecord = storedDeclinations.get(point);
                            if (DeclinationServiceImpl.timeAndSpaceDistance(existingDeclinationRecord.getPosition().getDistance(point),
                                    timePoint, existingDeclinationRecord.getTimePoint()) > 0.1) {
                                // less than ~6 nautical miles and/or ~.6 months off
                                fetchAndAppendDeclination(timePoint, point, importer, out);
                            }
                        }
                        for (double lng = -grid; lng > -180; lng -= grid) {
                            Position point = new DegreePosition(lat, lng);
                            Declination existingDeclinationRecord = storedDeclinations.get(point);
                            if (DeclinationServiceImpl.timeAndSpaceDistance(existingDeclinationRecord.getPosition().getDistance(point),
                                    timePoint, existingDeclinationRecord.getTimePoint()) > 0.1) {
                                // less than ~6 nautical miles and/or ~.6 months off
                                fetchAndAppendDeclination(timePoint, point, importer, out);
                            }
                        }
                    }
                    for (double lat = -grid; lat > -90; lat -= grid) {
                        System.out.println("Date: " + year + "/" + (month + 1) + ", Latitude: " + lat);
                        for (double lng = 0; lng < 180; lng += grid) {
                            Position point = new DegreePosition(lat, lng);
                            Declination existingDeclinationRecord = storedDeclinations.get(point);
                            if (DeclinationServiceImpl.timeAndSpaceDistance(existingDeclinationRecord.getPosition().getDistance(point),
                                    timePoint, existingDeclinationRecord.getTimePoint()) > 0.1) {
                                // less than ~6 nautical miles and/or ~.6 months off
                                fetchAndAppendDeclination(timePoint, point, importer, out);
                            }
                        }
                        for (double lng = -grid; lng > -180; lng -= grid) {
                            Position point = new DegreePosition(lat, lng);
                            Declination existingDeclinationRecord = storedDeclinations.get(point);
                            if (DeclinationServiceImpl.timeAndSpaceDistance(existingDeclinationRecord.getPosition().getDistance(point),
                                    timePoint, existingDeclinationRecord.getTimePoint()) > 0.1) {
                                // less than ~6 nautical miles and/or ~.6 months off
                                fetchAndAppendDeclination(timePoint, point, importer, out);
                            }
                        }
                    }
                    out.close();
                }
            }
        }
    }

    /**
     * Launches the importer, writing to resources/declination-year.txt (where "year" represents the year for which the
     * values are stored in the file) the declinations downloaded online for the years <code>args[0]</code> to
     * <code>args[1]</code> (inclusive) for all positions with a grid of <code>args[2]</code> degrees each, starting at
     * 0&deg;0.0'N and 0&deg;0.0'E.
     * @throws ParseException 
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, ParseException {
        DeclinationStore store = new DeclinationStore();
        store.run(args);
    }
    
    private void usage() {
        System.out.println("java " + NOAAImporter.class.getName() + " <fromYear> <toYear> <gridSizeInDegrees>");
    }
}
