package com.sap.sse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sap.sse.common.TimePoint;

/**
 * A class providing static information about the running server. Some information is taken from system
 * properties that are assigned to constants in {@link ServerStartupConstants}, others are taken
 * from the {@code version.json} file located under {@code configuration/jetty}.
 * 
 * @author Frank Mittag, Axel Uhl
 *
 */
public class ServerInfo {
    private static final Logger logger = Logger.getLogger(ServerInfo.class.getName());
    public static final String COMMIT_ID_FIELD_NAME = "commit_id";
    public static final String ACTIVE_BRANCH_FIELD_NAME = "active_branch";
    public static final String BUILD_DATE_FIELD_NAME = "build_date";
    public static final String RELEASE_FIELD_NAME = "release";
    public static final String START_TIME_MILLIS_FIELD_NAME = "start_time_millis";
    public static final String PORT_FIELD_NAME = "port";
    public static final String SERVER_GROUP_NAME_SUFFIX = "-server";
    
    private final String commitId;
    private final String activeBranch;
    private final TimePoint buildDate;
    private final String release;
    private final TimePoint startTime;
    private final Integer port;
    
    private ServerInfo(String commitId, String activeBranch, TimePoint buildDate, String release, TimePoint startTime, Integer port) {
        super();
        this.commitId = commitId;
        this.activeBranch = activeBranch;
        this.buildDate = buildDate;
        this.release = release;
        this.startTime = startTime;
        this.port = port;
    }

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
    
    public static ServerInfo getServerInfo() throws FileNotFoundException, IOException, ParseException, NumberFormatException, java.text.ParseException {
        final SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmX");
        final JSONObject buildVersionJson = getBuildVersionJson();
        return new ServerInfo((String) buildVersionJson.get(COMMIT_ID_FIELD_NAME),
                              (String) buildVersionJson.get(ACTIVE_BRANCH_FIELD_NAME),
                              buildVersionJson.get(BUILD_DATE_FIELD_NAME) == null ? null : TimePoint.of(format.parse(((String) buildVersionJson.get(BUILD_DATE_FIELD_NAME))+"Z")),
                              (String) buildVersionJson.get(RELEASE_FIELD_NAME),
                              buildVersionJson.get(START_TIME_MILLIS_FIELD_NAME) == null ? null : TimePoint.of(((Number) buildVersionJson.get(START_TIME_MILLIS_FIELD_NAME)).longValue()),
                              buildVersionJson.get(PORT_FIELD_NAME) == null ? null : ((Number) buildVersionJson.get(PORT_FIELD_NAME)).intValue());
    }
    
    /**
     * Example output:
     * <pre>
     * {
     *    "commit_id": "418b55c1dda167e2446c17a22a1fd1686da4188b",
     *    "active_branch": "build",
     *    "build_date": "202104182222",
     *    "release": "build-202104182222",
     *    "start_time_millis": 1618912876000,
     *    "port": 8888
     *  }
     * </pre>
     * 
     * @see #ACTIVE_BRANCH_FIELD_NAME
     * @see #COMMIT_ID_FIELD_NAME
     * @see #BUILD_DATE_FIELD_NAME
     * @see #RELEASE_FIELD_NAME
     * @see #START_TIME_MILLIS_FIELD_NAME
     * @see #PORT_FIELD_NAME
     */
    public static JSONObject getBuildVersionJson() throws FileNotFoundException, IOException, ParseException {
        JSONObject result;
        final File versionJsonFile = new File(ServerStartupConstants.JETTY_HOME + File.separator + "version.json");
        if (versionJsonFile.exists()) {
            try (final BufferedReader br = new BufferedReader(new FileReader(versionJsonFile))) {
                result = (JSONObject) new JSONParser().parse(br);
            } catch (Exception e) {
                logger.warning("Unable to read version.json: "+e.getMessage());
                result = new JSONObject();
            }
        } else {
            result = new JSONObject();
        }
        return result;
    }
    
    public static File getServerDirectory() throws IOException {
        return new File(".").getCanonicalFile();
    }
    
    public static String getName() {
        return ServerStartupConstants.SERVER_NAME;
    }
    
    public static String getServerGroupName() {
        return getName()+SERVER_GROUP_NAME_SUFFIX;
    }
    
    public String getCommitId() {
        return commitId;
    }

    public String getActiveBranch() {
        return activeBranch;
    }

    public TimePoint getBuildDate() {
        return buildDate;
    }

    public String getRelease() {
        return release;
    }

    public TimePoint getStartTime() {
        return startTime;
    }

    public Integer getPort() {
        return port;
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
