package com.sap.sse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * A class providing static information about the running server.
 * @author Frank
 *
 */
public class ServerInfo {
    public static String getBuildVersion() {
        String version = "Unknown or Development (" + ServerStartupConstants.SERVER_NAME + ")";
        File versionfile = new File(ServerStartupConstants.JETTY_HOME + File.separator + "version.txt");
        if (versionfile.exists()) {
            try {
                version = new BufferedReader(new FileReader(versionfile)).readLine();
            } catch (Exception ex) {
                /* ignore */
            }
        }
        return version;
    }
    
    public static String getName() {
        return ServerStartupConstants.SERVER_NAME;
    }
}
