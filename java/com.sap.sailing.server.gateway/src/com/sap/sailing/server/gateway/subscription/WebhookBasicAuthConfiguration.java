package com.sap.sailing.server.gateway.subscription;

/**
 * Chargebee webhook basic authentication configuration. The basic auth user and password have to be in server
 * application start arguments: chargebee.basicauthuser, chargebee.basicauthpass
 * 
 * @author tutran
 */
public class WebhookBasicAuthConfiguration {
    private static final String USER = "chargebee.basicauthuser";
    private static final String PASSWORD = "chargebee.basicauthpass";

    private static WebhookBasicAuthConfiguration instance;

    private String username;
    private String password;

    public static WebhookBasicAuthConfiguration getInstance() {
        if (instance == null) {
            instance = new WebhookBasicAuthConfiguration(System.getProperty(USER), System.getProperty(PASSWORD));
        }

        return instance;
    }

    public WebhookBasicAuthConfiguration(String username, String password) {
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
