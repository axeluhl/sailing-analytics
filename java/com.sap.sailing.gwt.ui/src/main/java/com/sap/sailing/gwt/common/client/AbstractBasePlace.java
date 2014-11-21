package com.sap.sailing.gwt.common.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.place.shared.Place;

/**
 * An abstract Place which is able to manage and parse place parameters.
 * @author Frank
 *
 */
public abstract class AbstractBasePlace extends Place {
    private static final Logger logger = Logger.getLogger(AbstractBasePlace.class.getName());
    private static final String VALIDATE_TOKENS_MESSAGE_PREFIX = "Missing token(s): ";

    private String placeParametersAsToken;
    private Map<String, String> params = new HashMap<String, String>();

    public AbstractBasePlace(String... paramKeysAndValues) {
        if (paramKeysAndValues.length % 2 == 0) {
            StringBuilder stringBuilder = new StringBuilder();
    
            for (int i = 0; i < paramKeysAndValues.length; i++) {
                String paramName = paramKeysAndValues[i++];
                String paramValue = paramKeysAndValues[i];
                // for now we don't add 'null' parameters
                if(paramValue != null) {
                    params.put(paramName, paramValue);
                    if(stringBuilder.length() > 0) {
                        stringBuilder.append("&");
                    }
                    stringBuilder.append(paramName + "=" + paramValue);
                }
            }
    
            placeParametersAsToken = stringBuilder.toString();
        } else {
            logger.warning("Invalid number of arguments received! Must be key,value,key,value ... ignoring arguments");
        }
    }

    public AbstractBasePlace(String url) {
        if (url != null && !url.isEmpty()) {
            this.placeParametersAsToken = url;

            List<String> list = Arrays.asList(placeParametersAsToken.split("&"));
    
            if (list == null || list.size() < 1) {
                logger.warning("Token empty, no-op");
                return;
            }
    
            for (String listItem : list) {
                List<String> nvPair = Arrays.asList(listItem.split("="));
                if (nvPair == null || nvPair.size() != 2) {
                    logger.warning("Invalid parameters");
                    continue;
                }
                params.put(nvPair.get(0), nvPair.get(1));
            }
        }
    }

    protected void validate(String... paramNames) {
        StringBuffer message = new StringBuffer(VALIDATE_TOKENS_MESSAGE_PREFIX);

        for (String name : paramNames) {
            if (!params.containsKey(name))
                message.append(name + " ");
        }

        if (!message.toString().equals(VALIDATE_TOKENS_MESSAGE_PREFIX)) {
            logger.warning(message.toString());
        }
    }

    public String getParametersAsToken() {
        return placeParametersAsToken;
    }

    public String getParameter(String name) {
        return params.get(name);
    }

    public boolean hasParameter(String name) {
        return params.containsKey(name);
    }
}