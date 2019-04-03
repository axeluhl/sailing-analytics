package com.sap.sse.security.util;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sse.common.Duration;
import com.sap.sse.util.HttpUrlConnectionHelper;

public final class RemoteServerUtil {
    private static final Logger logger = Logger.getLogger(RemoteServerUtil.class.getName());
    
    private RemoteServerUtil() {
    }

    /**
     * Using the given credentials, this method calls the remote server to create a valid bearer token.
     */
    public static String resolveBearerTokenForRemoteServer(String hostname, String username, String password) {
        String token = "";
        try {
            URL base = createBaseUrl(hostname);
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                String path = "/security/api/restsecurity/access_token";
                URL serverAddress = createRemoteServerUrl(base, path, null);
                URLConnection connection = HttpUrlConnectionHelper.redirectConnection(serverAddress, Duration.ONE_MINUTE,
                        t -> {
                            String auth = username + ":" + password;
                            String base64 = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                            t.setRequestProperty("Authorization", "Basic " + base64);
                        });
    
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = connection.getInputStream().read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                String jsonToken = result.toString("UTF-8");
                Object requestBody = JSONValue.parseWithException(jsonToken);
                if (requestBody instanceof JSONObject) {
                    JSONObject json = (JSONObject) requestBody;
                    Object tokenObj = json.get("access_token");
                    if (tokenObj instanceof String) {
                        token = (String) tokenObj;
                        logger.info("Obtained access token for user "+username);
                    } else {
                        logger.warning("Did not find access token for user "+username);
                    }
                } else {
                    throw new RuntimeException("Could not obtain token for server");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return token;
    }
    
    /**
     * Strips off trailing slash and replaces an omitted or unknown protocol by HTTP
     */
    public static URL createBaseUrl(String urlAsString) throws MalformedURLException {
        final String urlAsStringWithTrailingSlashRemoved = urlAsString == null ?
                null : urlAsString.length()>0 && urlAsString.charAt(urlAsString.length()-1)=='/' ?
                        urlAsString.substring(0, urlAsString.length()-1) : urlAsString;
        URL url;
        try {
            url = new URL(urlAsStringWithTrailingSlashRemoved);
        } catch (MalformedURLException e1) {
            // trying to strip off an unknown protocol, defaulting to HTTP
            String urlAsStringAfterFormatting = urlAsStringWithTrailingSlashRemoved;
            if (urlAsStringAfterFormatting.contains("://")) {
                urlAsStringAfterFormatting = urlAsStringWithTrailingSlashRemoved.split("://")[1];
            }
            url = new URL("http://" + urlAsStringAfterFormatting);
        }
        return url;
    }
    

    public static URL createRemoteServerUrl(URL base, String pathWithLeadingSlash, String query) throws Exception {
        URL url;
        if (query != null) {
            url = new URL(base.toExternalForm() + pathWithLeadingSlash + "?" + query);
        } else {
            url = new URL(base.toExternalForm() + pathWithLeadingSlash);
        }
        return url;
    }
}
