package com.sap.sailing.simulator.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.simulator.Path;
import com.sap.sailing.simulator.TimedPositionWithSpeed;

//I077899 - Mihai Bogdan Eugen
public class SimulatorUtils {

    private static double TRESHOLD_DEGREES = 15;

    public static boolean isPosition2Turn(Position position1, Position position2, Position position3) {
        return SimulatorUtils.isPosition2Turn(position1, position2, position3, TRESHOLD_DEGREES);
    }

    public static boolean isPosition2Turn(Position pos1, Position pos2, Position pos3, double treshold) {
        Bearing b1 = pos1.getBearingGreatCircle(pos2);
        Bearing b2 = pos2.getBearingGreatCircle(pos3);
        double diff = b1.getDifferenceTo(b2).getDegrees();

        if (diff < 0)
            diff = 360 + diff;

        return !((diff >= 0 && diff <= treshold) || (diff >= (180 - treshold) && diff <= (180 + treshold)) || (diff >= (360 - treshold) && diff <= 360));
    }

    public static boolean saveToFile(Path path, String fileName) {
        boolean result = true;
        try {
            OutputStream file = new FileOutputStream(fileName);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            try {
                output.writeObject(path);
            } finally {
                output.close();
                buffer.close();
                file.close();
            }

        } catch (IOException ex) {
            result = false;
        }

        return result;
    }

    public static Path readFromExternalFile(String fileName) {
        Path result = null;
        try {
            InputStream file = new FileInputStream(fileName);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            try {

                result = (Path) input.readObject();
            } finally {
                input.close();
                buffer.close();
                file.close();
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("[ERROR in readFromExternalFile]:[ClassNotFoundException]: " + ex.getMessage());
            result = null;
        } catch (IOException ex) {
            System.out.println("[ERROR in readFromExternalFile]:[IOException]: " + ex.getMessage());
            result = null;
        }

        return result;
    }

    public static Path readFromResourcesFile(String fileName) {
        Path result = null;
        try {
            ClassLoader classLoader = Class.forName("com.sap.sailing.simulator.impl.SimulatorUtils").getClassLoader();
            InputStream file = classLoader.getResourceAsStream(fileName);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            try {
                result = (Path) input.readObject();
            } finally {
                input.close();
                buffer.close();
                file.close();
            }
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException " + ex.getMessage());
            result = null;
        } catch (IOException ex) {
            System.out.println("IOException " + ex.getMessage());
            result = null;
        }

        return result;
    }

    public static boolean saveToGpxFile(Path path, String fileName) {

        if (path == null)
            return false;

        List<TimedPositionWithSpeed> pathPoints = path.getPathPoints();
        if (pathPoints == null || pathPoints.isEmpty())
            return false;

        TimedPositionWithSpeed timedPoint = null;
        Position point = null;
        int noOfPoints = pathPoints.size();
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer buffer = new StringBuffer();

        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\r\n<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\r\n\t<trk>\r\n\t\t<name>GPSPolyPath</name>\r\n\t\t<trkseg>");

        for (int index = 0; index < noOfPoints; index++) {
            timedPoint = pathPoints.get(index);
            point = timedPoint.getPosition();
            buffer.append("\r\n\t\t\t<trkpt lat=\"" + point.getLatDeg() + "\" lon=\"" + point.getLngDeg()
                    + "\">\r\n\t\t\t\t<ele>0</ele>\r\n\t\t\t\t<time>"
                    + formatter.format(timedPoint.getTimePoint().asDate()) + "</time>\r\n\t\t\t</trkpt>");
        }

        buffer.append("\t\t</trkseg>\r\n\t</trk>\r\n</gpx>\r\n");

        String content = buffer.toString();

        try {

            FileWriter writer = new FileWriter(fileName);
            BufferedWriter output = new BufferedWriter(writer, 32768);

            try {
                output.write(content);
            } finally {
                output.close();
                writer.close();
            }

        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
