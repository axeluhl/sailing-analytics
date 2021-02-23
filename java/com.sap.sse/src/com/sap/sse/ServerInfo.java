package com.sap.sse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    
    public static JSONObject getBuildVersionJson() throws FileNotFoundException, IOException, ParseException {
        final JSONObject result;
        final File versionJsonFile = new File(ServerStartupConstants.JETTY_HOME + File.separator + "version.json");
        if (versionJsonFile.exists()) {
            result = (JSONObject) new JSONParser().parse(new BufferedReader(new FileReader(versionJsonFile)));
        } else {
            result = new JSONObject();
        }
        return result;
    }
    
    public static String getName() {
        return ServerStartupConstants.SERVER_NAME;
    }
    
    /**
     * Retrieve event management base URL from server startup configuration. This is the default
     * URL for a self-service server in case the user doesn't have the {@code CREATE_OBJECT} permission
     * for the current server.
     * 
     * @return the base URL of a self service-enabled server
     * @see ServerStartupConstants#MANAGE_EVENTS_URL
     */
    public static String getManageEventsBaseUrl() {
        return ServerStartupConstants.MANAGE_EVENTS_URL;
    }
}
