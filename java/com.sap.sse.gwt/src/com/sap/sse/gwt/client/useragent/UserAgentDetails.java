package com.sap.sse.gwt.client.useragent;

/**
 * This class holds user agent specific details like type and version.
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jul 21, 2012
 */
public class UserAgentDetails {

    public enum AgentTypes {
        MSIE, FIREFOX, SAFARI, OPERA, CHROME, UNKNOWN
    }

    public static enum PlatformTypes {
        DESKTOP, MOBILE, UNKNOWN
    }

    static final String[] MOBILE_SPECIFIC_SUBSTRING = { "iPhone", "Android", "MIDP", "Opera Mobi", "iPad",
            "Opera Mini", "BlackBerry", "HP iPAQ", "IEMobile", "MSIEMobile", "Windows Phone", "HTC", "LG", "MOT",
            "Nokia", "Symbian", "Fennec", "Maemo", "Tear", "Midori", "armv", "Windows CE", "WindowsCE", "Smartphone",
            "240x320", "176x220", "320x320", "160x160", "webOS", "Palm", "Sagem", "Samsung", "SGH", "SIE",
            "SonyEricsson", "MMP", "UCWEB" };

    private AgentTypes type;
    private Integer[] version;
    private PlatformTypes platform;
    private String userAgentRaw;

    public UserAgentDetails(String userAgent) {

        userAgent = userAgent.toLowerCase();
        try {
            if (userAgent.indexOf("msie") != -1 && !(userAgent.indexOf("opera") != -1)
                    && (userAgent.indexOf("webtv") == -1)) {
                String ieVersionString = userAgent.substring(userAgent.indexOf("msie ") + 5);
                ieVersionString = safeSubstring(ieVersionString, 0, ieVersionString.indexOf(";"));
                setType(AgentTypes.MSIE);
                setVersion(parseVersionString(ieVersionString));
            } else if (userAgent.indexOf(" firefox/") != -1) {
                int i = userAgent.indexOf(" firefox/") + 9;
                setType(AgentTypes.FIREFOX);
                setVersion(parseVersionString(safeSubstring(userAgent, i, i + 5)));
            } else if (userAgent.indexOf(" chrome/") != -1) {
                int i = userAgent.indexOf(" chrome/") + 8;
                setType(AgentTypes.CHROME);
                setVersion(parseVersionString(safeSubstring(userAgent, i, i + 5)));
            } else if (!(userAgent.indexOf(" chrome/") != -1) && userAgent.indexOf("safari") != -1) {
                int i = userAgent.indexOf(" version/") + 9;
                setType(AgentTypes.SAFARI);
                setVersion(parseVersionString(safeSubstring(userAgent, i, i + 5)));
            } else if (userAgent.indexOf("opera") != -1) {
                int i = userAgent.indexOf(" version/");
                if (i != -1) {
                    i += 9; /* " version/".length */
                } else {
                    i = userAgent.indexOf("opera/") + 6;
                }
                setType(AgentTypes.OPERA);
                setVersion(parseVersionString(safeSubstring(userAgent, i, i + 5)));
            }
        } catch (Exception e) {
            /* Silently ignore but provide default values */
            setType(AgentTypes.UNKNOWN);
            setVersion(new Integer[] { -1, -1 });
        }
        userAgentRaw = userAgent;
    }

    /**
     * Returns indicator if agent is mobile or desktop. Do NOT invoke this method repeatedly (e.g. at every request)
     * because it has runtime of O(~20)!
     * 
     * @return {@link PlatformTypes} describing if user agent is mobile or not
     */
    public PlatformTypes isMobile() {
        setPlatform(UserAgentDetails.PlatformTypes.DESKTOP);
        for (String mobile : UserAgentDetails.MOBILE_SPECIFIC_SUBSTRING) {
            if (userAgentRaw.contains(mobile) || userAgentRaw.contains(mobile.toUpperCase())
                    || userAgentRaw.contains(mobile.toLowerCase())) {
                setPlatform(UserAgentDetails.PlatformTypes.MOBILE);
                break;
            }
        }
        return getPlatform();
    }

    private Integer[] parseVersionString(String versionString) {
        Integer[] version = new Integer[] { -1, -1 };

        int idx = versionString.indexOf('.');
        if (idx < 0) {
            idx = versionString.length();
        }
        version[0] = Integer.parseInt(safeSubstring(versionString, 0, idx));

        int idx2 = versionString.indexOf('.', idx + 1);
        if (idx2 < 0) {
            idx2 = versionString.length();
        }
        try {
            version[1] = Integer.parseInt(safeSubstring(versionString, idx + 1, idx2).replaceAll("[^0-9].*", ""));
        } catch (NumberFormatException e) {
            // leave the minor version unmodified (-1 = unknown)
        }
        return version;
    }

    private String safeSubstring(String string, int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            beginIndex = 0;
        }
        if (endIndex < 0 || endIndex > string.length()) {
            endIndex = string.length();
        }
        return string.substring(beginIndex, endIndex);
    }

    public AgentTypes getType() {
        return type;
    }

    public void setType(AgentTypes name) {
        this.type = name;
    }

    public Integer[] getVersion() {
        return version;
    }

    public void setVersion(Integer[] version) {
        this.version = version;
    }

    public PlatformTypes getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformTypes platform) {
        this.platform = platform;
    }
}
