package com.sap.sse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class BuildVersion {
    public static String getBuildVersion() {
        String version = "Unknown or Development (" + System.getProperty("com.sap.sailing.server.name") + ")";
        File versionfile = new File(System.getProperty("jetty.home") + File.separator + "version.txt");
        if (versionfile.exists()) {
            try {
                version = new BufferedReader(new FileReader(versionfile)).readLine();
            } catch (Exception ex) {
                /* ignore */
            }
        }
        return version;
    }

}
