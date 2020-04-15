package com.sap.sailing.gwt.common.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.sap.sse.common.Util;

public abstract class AbstractMapTokenizer<P extends Place> implements PlaceTokenizer<P> {

    private static final Logger logger = Logger.getLogger(AbstractMapTokenizer.class.getName());

    @Override
    public P getPlace(String token) {
        return getPlaceFromParameters(toParameterMap(token));
    }

    protected abstract P getPlaceFromParameters(Map<String, Set<String>> parameters);

    protected abstract Map<String, Set<String>> getParameters(P place);

    @Override
    public String getToken(P place) {
        return toToken(getParameters(place));
    }

    private String toToken(Map<String, Set<String>> parameters) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<String, Set<String>> entry : parameters.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            for (String val : entry.getValue()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("&");
                }
                stringBuilder.append(entry.getKey() + "=" + val);
            }
        }

        return stringBuilder.toString();
    }

    private Map<String, Set<String>> toParameterMap(String placeParametersAsToken) {
        List<String> list = Arrays.asList(placeParametersAsToken.split("&"));

        if (list == null || list.size() < 1) {
            logger.warning("Token empty, no-op");
            Collections.emptyMap();
        }

        Map<String, Set<String>> result = new HashMap<>();
        for (String listItem : list) {
            String[] nvPair = listItem.split("=");
            if (nvPair == null || nvPair.length != 2) {
                logger.warning("Invalid parameters");
                continue;
            }
            Util.addToValueSet(result, nvPair[0], nvPair[1]);
        }
        return result;
    }

}
