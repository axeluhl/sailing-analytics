package com.sap.sse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class providing static information about the running server.
 * @author Frank
 *
 */
public class ServerInfo {
    private static final Logger logger = Logger.getLogger(ServerInfo.class.getName());
    
    public static String getBuildVersion() {
        String version = "Unknown or Development (" + getName() + ")";
        File versionfile = new File(ServerStartupConstants.JETTY_HOME + File.separator + "version.txt");
        if (versionfile.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(versionfile))) {
                version = bufferedReader.readLine();
            } catch (Exception ex) {
                logger.log(Level.WARNING, "Error trying to obtain version info", ex);
            }
        }
        return version;
    }
    
    public static String getName() {
        return ServerStartupConstants.SERVER_NAME;
    }
}
