package com.sap.sailing.simulator.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.osgi.framework.FrameworkUtil;

import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.simulator.Path;

public class SerializationUtils {

    private static final Logger LOGGER = Logger.getLogger("com.sap.sailing.simulator");

    public static final String LEGSNAMES_DAT = "legsNames.dat";
    public static final String RACECOURSE_DAT = "racecourse.dat";

    public static final String[] PATH_NAMES = new String[] { "1#Omniscient", "2#Opportunistic", "3#1-Turner Left", "4#1-Turner Right", "6#GPS Poly",
    "7#GPS Track" };

    public static String pathPrefix = null;

    public static String getPathPrefix() {
        String bundleName = null;
        try {
            bundleName = FrameworkUtil.getBundle(Class.forName("com.sap.sailing.simulator.impl.SailingSimulatorImpl")).getSymbolicName();
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR][SerializationUtils][getPathPrefix][ClassNotFoundException]  " + e.getMessage());
            LOGGER.severe("[ERROR][SerializationUtils][getPathPrefix][ClassNotFoundException]  " + e.getMessage());
            return null;
        }
        String bundlesProperty = System.getProperty("osgi.bundles");

        int bundleNameStart = bundlesProperty.indexOf(bundleName);
        int bundleNameEnd = bundleNameStart + bundleName.length();

        String prependedBundlePath = bundlesProperty.substring(0, bundleNameEnd);

        int prefixPos = prependedBundlePath.lastIndexOf("reference:file:");

        if (prefixPos >= 0) {
            prependedBundlePath = prependedBundlePath.substring(prefixPos + 15, prependedBundlePath.length());
        }

        return prependedBundlePath;
    }

    public static boolean savePathsToFiles(Map<String, Path> paths, Path raceCourse) {
        if (paths == null) {
            return false;
        }

        if (paths.isEmpty()) {
            return true;
        }

        if (pathPrefix == null) {
            pathPrefix = getPathPrefix();
        }

        String filePath = "";
        boolean result = true;

        for (String name : PATH_NAMES) {
            filePath = pathPrefix + "\\src\\resources\\" + name + ".dat";
            result &= saveToFile(paths.get(name), filePath);
        }

        filePath = pathPrefix + "\\src\\resources\\" + RACECOURSE_DAT;
        result &= saveToFile(raceCourse, filePath);

        return result;
    }

    public static boolean saveLegPathsToFiles(Map<String, Path> paths, Path raceCourse, int legIndex, int competitorIndex) {
        if (paths == null) {
            return false;
        }

        if (paths.isEmpty()) {
            return true;
        }

        if (pathPrefix == null) {
            pathPrefix = getPathPrefix();
        }

        String filePath = "";
        boolean result = true;

        for (String name : PATH_NAMES) {
            filePath = pathPrefix + "\\src\\resources\\" + name + "_" + competitorIndex + "_" + legIndex + ".dat";
            result &= saveToFile(paths.get(name), filePath);
        }

        filePath = pathPrefix + "\\src\\resources\\racecourse_" + competitorIndex + "_" + legIndex + ".dat";
        result &= saveToFile(raceCourse, filePath);

        return result;
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
            System.err.println("[ERROR][SerializationUtils][saveToFile][IOException]  " + ex.getMessage());
            LOGGER.severe("[ERROR][SerializationUtils][saveToFile][IOException]  " + ex.getMessage());
            result = false;
        }

        return result;
    }

    public static Pair<Map<String, Path>, Path> readLegPathsFromResources(int legIndex, int competitorIndex) {
        HashMap<String, Path> paths = new HashMap<String, Path>();

        Path path = null;
        String filePath = "";

        for (String pathName : PATH_NAMES) {
            filePath = "resources/" + pathName + "_" + competitorIndex + "_" + legIndex + ".dat";
            path = (Path) readObjectFromResources(filePath);
            if (path == null) {
                System.err.println("[ERROR][SerializationUtils][readPathsFromResources] Cannot de-serialize path from" + pathName);
                LOGGER.severe("[ERROR][SerializationUtils][readPathsFromResources] Cannot de-serialize path from" + pathName);
            } else {
                paths.put(pathName, path);
            }
        }

        Path raceCourse = (Path) readObjectFromResources("resources/racecourse_" + competitorIndex + "_" + legIndex + ".dat");

        return new Pair<Map<String, Path>, Path>(paths, raceCourse);
    }

    public static Pair<Map<String, Path>, Path> readPathsFromResources() {
        HashMap<String, Path> paths = new HashMap<String, Path>();
        Path path = null;
        String filePath = "";

        for (String pathName : PATH_NAMES) {
            filePath = "resources/" + pathName + ".dat";
            path = (Path) readObjectFromResources(filePath);
            if (path == null) {
                System.err.println("[ERROR][SerializationUtils][readPathsFromResources] Cannot de-serialize path from" + pathName);
                LOGGER.severe("[ERROR][SerializationUtils][readPathsFromResources] Cannot de-serialize path from" + pathName);
            } else {
                paths.put(pathName, path);
            }
        }

        Path raceCourse = (Path) readObjectFromResources("resources/" + RACECOURSE_DAT);

        return new Pair<Map<String, Path>, Path>(paths, raceCourse);
    }

    public static Object readObjectExternalFile(String fileName) {
        Object result = null;
        try {
            InputStream file = new FileInputStream(fileName);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);

            try {
                result = input.readObject();
            } finally {
                input.close();
                buffer.close();
                file.close();
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("[ERROR][SerializationUtils][readFromExternalFile][ClassNotFoundException] " + ex.getMessage());
            LOGGER.severe("[ERROR][SerializationUtils][readFromExternalFile][ClassNotFoundException] " + ex.getMessage());
            result = null;
        } catch (IOException ex) {
            System.err.println("[ERROR][SerializationUtils][readFromExternalFile][IOException]  " + ex.getMessage());
            LOGGER.severe("[ERROR][SerializationUtils][readFromExternalFile][IOException]  " + ex.getMessage());
            result = null;
        }

        return result;
    }

    public static Object readObjectFromResources(String fileName) {
        Object result = null;

        try {
            ClassLoader classLoader = Class.forName("com.sap.sailing.simulator.impl.SailingSimulatorImpl").getClassLoader();
            InputStream file = classLoader.getResourceAsStream(fileName);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);

            try {
                result = input.readObject();
            } finally {
                input.close();
                buffer.close();
                file.close();
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("[ERROR][SerializationUtils][readFromResourcesFile][ClassNotFoundException] " + ex.getMessage());
            LOGGER.severe("[ERROR][SerializationUtils][readFromResourcesFile][ClassNotFoundException] " + ex.getMessage());
            result = null;
        } catch (IOException ex) {
            System.err.println("[ERROR][SerializationUtils][readFromResourcesFile][IOException]  " + ex.getMessage());
            LOGGER.severe("[ERROR][SerializationUtils][readFromResourcesFile][IOException]  " + ex.getMessage());
            result = null;
        }

        return result;
    }

    public static boolean saveLegsNamesToFiles(List<String> legsNames) {
        if (legsNames == null) {
            return false;
        }

        if (legsNames.isEmpty()) {
            return true;
        }

        if (pathPrefix == null) {
            pathPrefix = getPathPrefix();
        }

        String filePath = pathPrefix + "\\src\\resources\\" + LEGSNAMES_DAT;

        boolean result = true;

        try {
            OutputStream file = new FileOutputStream(filePath);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);

            try {
                output.writeObject(legsNames);
            } finally {
                output.close();
                buffer.close();
                file.close();
            }
        } catch (IOException ex) {
            System.err.println("[ERROR][SerializationUtils][saveLegsNamesToFiles][IOException]  " + ex.getMessage());
            LOGGER.severe("[ERROR][SerializationUtils][saveLegsNamesToFiles][IOException]  " + ex.getMessage());
            result = false;
        }

        return result;
    }

}
