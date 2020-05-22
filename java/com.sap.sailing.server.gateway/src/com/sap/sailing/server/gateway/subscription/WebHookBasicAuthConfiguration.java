package com.sap.sailing.server.gateway.subscription;

/**
 * WebHook basic authentication configuration. The basic authentication user and password have to be in server
 * application start arguments: chargebee.basicauthuser, chargebee.basicauthpass
 * 
 * @author tutran
 */
public class WebHookBasicAuthConfiguration {
    private static final String USER = "chargebee.basicauthuser";
    private static final String PASSWORD = "chargebee.basicauthpass";

    private static WebHookBasicAuthConfiguration instance;

    private String username;
    private String password;

    public static WebHookBasicAuthConfiguration getInstance() {
        if (instance == null) {
            instance = new WebHookBasicAuthConfiguration(System.getProperty(USER), System.getProperty(PASSWORD));
        }

        return instance;
    }

    public WebHookBasicAuthConfiguration(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
