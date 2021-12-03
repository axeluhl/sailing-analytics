package com.sap.sse.gwt.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.gwt.http.client.URL;
import com.google.gwt.place.shared.Place;

/**
 * An abstract Place which is able to manage and parse place parameters.
 * 
 * @author Frank
 */
public abstract class AbstractBasePlace extends Place {
    private static final Logger logger = Logger.getLogger(AbstractBasePlace.class.getName());
    private static final String VALIDATE_TOKENS_MESSAGE_PREFIX = "Missing token(s): ";

    private final String placeParametersAsToken;
    private final Map<String, String> params;

    protected AbstractBasePlace(Map<String, String> params) {
        this.params = new HashMap<>();
        if (params != null) {
            this.params.putAll(params);
        }
        placeParametersAsToken = getTokenStringFromParameters(params);
    }
    
    protected AbstractBasePlace(String placeParamsFromUrlFragment) {
        this.params = extractUrlParams(placeParamsFromUrlFragment);
        placeParametersAsToken = getTokenStringFromParameters(this.params);
    }
    
    protected String getTokenStringFromParameters(Map<String, String> paramKeysAndValues) {
        final StringJoiner stringJoiner = new StringJoiner("&");
        if (paramKeysAndValues != null) {
            for (final Entry<String, String> e : paramKeysAndValues.entrySet()) {
                String paramName = e.getKey();
                String paramValue = e.getValue();
                // for now we don't add 'null' parameters
                if (paramValue != null) {
                    stringJoiner.add(paramName + "=" + paramValue);
                }
            }
        }
        return stringJoiner.toString();
    }

    protected Map<String, String> extractUrlParams(String placeParamsFromUrlFragment) {
        final Map<String, String> result = new HashMap<>();
        if (placeParamsFromUrlFragment != null && !placeParamsFromUrlFragment.isEmpty()) {
            final List<String> list = Arrays.asList(placeParamsFromUrlFragment.split("&"));
            if (list == null || list.size() < 1) {
                logger.warning("Token empty, no-op");
            } else {
                for (String listItem : list) {
                    final List<String> nvPair = Arrays.asList(listItem.split("="));
                    if (nvPair == null || nvPair.size() != 2) {
                        logger.warning("Invalid parameters");
                    } else {
                        result.put(nvPair.get(0), nvPair.get(1));
                    }
                }
            }
        }
        return result;
    }

    protected void validate(String... paramNames) {
        final StringBuffer message = new StringBuffer(VALIDATE_TOKENS_MESSAGE_PREFIX);
        for (String name : paramNames) {
            if (!params.containsKey(name)) {
                message.append(name + " ");
            }
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

    public String getParameterDecoded(String name) {
        String paramValue = params.get(name);
        return paramValue == null ? null : URL.decodeQueryString(paramValue);
    }
    
    public boolean hasParameter(String name) {
        return params.containsKey(name);
    }
}