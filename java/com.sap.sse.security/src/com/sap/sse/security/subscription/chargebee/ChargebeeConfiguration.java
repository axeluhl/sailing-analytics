package com.sap.sse.security.subscription.chargebee;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Subscription configuration. The system will get these information from server application startup arguments:
 * {@code chargebee.site}, {@code chargebee.apikey}
 * 
 * @author Tu Tran
 */
public class ChargebeeConfiguration {
    private static final Logger logger = Logger.getLogger(ChargebeeConfiguration.class.getName());

    private static final String CHARGEBEE_SITE = "chargebee.site";
    private static final String CHARGEBEE_APIKEY = "chargebee.apikey";

    private static ChargebeeConfiguration instance;

    private String site;
    private String apiKey;

    protected ChargebeeConfiguration(String site, String apiKey) {
        this.site = site;
        this.apiKey = apiKey;
    }

    public static ChargebeeConfiguration getInstance() {
        if (instance == null) {
            String site = System.getProperty(CHARGEBEE_SITE);
            String apiKey = System.getProperty(CHARGEBEE_APIKEY);
            logger.log(Level.INFO, "Chargebee site: " + site);
            instance = new ChargebeeConfiguration(site, apiKey);
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
