package com.sap.sailing.gwt.ui.server.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Subscription configuration. The system will get these information from server application startup arguments:
 * chargebee.site, chargebee.apikey
 * 
 * @author Tu Tran
 */
public class SubscriptionConfiguration {
    private static final Logger logger = Logger.getLogger(SubscriptionConfiguration.class.getName());

    private static final String CHARGEBEE_SITE = "chargebee.site";
    private static final String CHARGEBEE_APIKEY = "chargebee.apikey";

    private static SubscriptionConfiguration instance;

    private String site;
    private String apiKey;

    protected SubscriptionConfiguration(String site, String apiKey) {
        this.site = site;
        this.apiKey = apiKey;
    }

    public static SubscriptionConfiguration getInstance() {
        if (instance == null) {
            String site = System.getProperty(CHARGEBEE_SITE);
            String apiKey = System.getProperty(CHARGEBEE_APIKEY);
            logger.log(Level.INFO, "Chargebee site: " + site);
            instance = new SubscriptionConfiguration(site, apiKey);
        }
        return instance;
    }

    public String getSite() {
        return site;
    }

    public String getApiKey() {
        return apiKey;
    }
}
