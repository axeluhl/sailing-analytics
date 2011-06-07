package com.sap.sailing.declination.impl;

import java.io.EOFException;
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
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.declination.DeclinationRecord;
import com.sap.sailing.declination.NOAAImporter;
import com.sap.sailing.domain.base.Bearing;
import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.impl.DegreeBearingImpl;
import com.sap.sailing.domain.base.impl.DegreePosition;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;

/**
 * Manages resources in which declinations can be stored for off-line look-up.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class DeclinationStore {
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public Set<DeclinationRecord> getStoredDeclinations(int year) throws IOException, ClassNotFoundException, ParseException {
        DeclinationRecord record;
        Set<DeclinationRecord> result = new HashSet<DeclinationRecord>();
        InputStream is = getInputStreamForYear(year);
        if (is != null) {
            ObjectInput in = new ObjectInputStream(is);
            while ((record = readExternal(in)) != null) {
                result.add(record);
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
    
    public void writeExternal(DeclinationRecord record, ObjectOutput out) throws IOException {
        out.writeUTF(dateFormatter.format(record.getTimePoint().asDate()));
        out.writeDouble(record.getPosition().getLatDeg());
        out.writeDouble(record.getPosition().getLngDeg());
        out.writeDouble(record.getBearing().getDegrees());
        out.writeDouble(record.getAnnualChange().getDegrees());
    }

    /**
     * @return <code>null</code> if EOF has been reached
     */
    public DeclinationRecord readExternal(ObjectInput in) throws IOException, ClassNotFoundException, ParseException {
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

    private void fetchAndAppendDeclination(int year, int month, double lat, double lng, NOAAImporter importer,
            ObjectOutput out) throws IOException {
        Position position = new DegreePosition(lat, lng);
        Calendar cal = new GregorianCalendar(year, month, /* dayOfMonth */ 1);
        TimePoint timePoint = new MillisecondsTimePoint(cal.getTimeInMillis());
        DeclinationRecord declination = null;
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
        }
    }

    private void run(String[] args) throws FileNotFoundException, IOException {
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
                    ObjectOutput out = new ObjectOutputStream(new FileOutputStream(getResourceForYear(year)));
                    int month = 6;
                    for (double lat = 0; lat < 90; lat += grid) {
                        System.out.println("Date: " + year + "/" + (month + 1) + ", Latitude: " + lat);
                        for (double lng = 0; lng < 180; lng += grid) {
                            fetchAndAppendDeclination(year, month, lat, lng, importer, out);
                        }
                        for (double lng = -grid; lng > -180; lng -= grid) {
                            fetchAndAppendDeclination(year, month, lat, lng, importer, out);
                        }
                    }
                    for (double lat = -grid; lat > -90; lat -= grid) {
                        System.out.println("Date: " + year + "/" + (month + 1) + ", Latitude: " + lat);
                        for (double lng = 0; lng < 180; lng += grid) {
                            fetchAndAppendDeclination(year, month, lat, lng, importer, out);
                        }
                        for (double lng = -grid; lng > -180; lng -= grid) {
                            fetchAndAppendDeclination(year, month, lat, lng, importer, out);
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
     */
    public static void main(String[] args) throws IOException {
        DeclinationStore store = new DeclinationStore();
        store.run(args);
    }
    
    private void usage() {
        System.out.println("java " + NOAAImporter.class.getName() + " <fromYear> <toYear> <gridSizeInDegrees>");
    }
}
