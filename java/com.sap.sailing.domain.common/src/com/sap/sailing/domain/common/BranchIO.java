package com.sap.sailing.domain.common;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility class for generating BranchIO-Deeplink.
 */
public class BranchIO {

    /**
     * Generate BranchIO-Deeplink by passing all relevant parameters as they are.
     * 
     * @param branchIoAppBAseUrl
     *            BranchIO-App-Url
     * @param parameters
     *            Parameters for passing to the app
     * @return url string containing BranchIO-Deeplink
     */
    public static String generateLink(String branchIoAppBAseUrl, final Map<String, String> parameters,
            URLEncoder urlEncoder) {
        StringBuilder deepLinkUrl = new StringBuilder(branchIoAppBAseUrl);
        if (parameters != null && parameters.size() > 0) {
            deepLinkUrl.append("?");
        }
        for (Entry<String, String> param : parameters.entrySet()) {
            deepLinkUrl.append(param.getKey()).append(("=")).append(urlEncoder.attributeEncode(param.getValue()));
        }
        return deepLinkUrl.toString();
    }

    /**
     * Generate BranchIO-Deeplink by passing all relevant parameters packed into an checkin-Parmater with as a Url
     * (=checkinUrl).
     * 
     * @param branchIoAppBAseUrl
     *            BranchIO-App-Url
     * @param branchIoAppCheckinPath
     *            BranchIO-App-Url checkin-paramter name
     * @param parameters
     *            Parameters for passing to the app
     * @return url string containing BranchIO-Deeplink
     */
    public static String generateLinkWithCheckinURL(String branchIoAppBAseUrl, String branchIoAppCheckinPath,
            String checkInBaseUrl, final Map<String, String> parameters, URLEncoder urlEncoder) {
        StringBuilder checkinUrl = new StringBuilder(checkInBaseUrl.substring(0, checkInBaseUrl.lastIndexOf("/")))
                .append("/tracking/checkin");
        if (parameters != null && parameters.size() > 0) {
            checkinUrl.append("?");
        }
        for (Entry<String, String> param : parameters.entrySet()) {
            checkinUrl.append(param.getKey()).append(("=")).append(urlEncoder.attributeEncode(param.getValue()));
        }
        String deeplinkUrl = branchIoAppBAseUrl + "?" + branchIoAppCheckinPath + "="
                + urlEncoder.attributeEncode(checkinUrl.toString());
        return deeplinkUrl;
    }

    private BranchIO() {
    }

    public static interface URLEncoder {
        String attributeEncode(String encode);
    }
}
