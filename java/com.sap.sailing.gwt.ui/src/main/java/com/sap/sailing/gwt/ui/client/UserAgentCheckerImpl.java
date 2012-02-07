package com.sap.sailing.gwt.ui.client;


public class UserAgentCheckerImpl implements UserAgentChecker {
    
    private static final String[] MOBILE_SPECIFIC_SUBSTRING = { "iPhone", "Android", "MIDP", "Opera Mobi", "iPad",
            "Opera Mini", "BlackBerry", "HP iPAQ", "IEMobile", "MSIEMobile", "Windows Phone", "HTC", "LG", "MOT",
            "Nokia", "Symbian", "Fennec", "Maemo", "Tear", "Midori", "armv", "Windows CE", "WindowsCE", "Smartphone",
            "240x320", "176x220", "320x320", "160x160", "webOS", "Palm", "Sagem", "Samsung", "SGH", "SIE",
            "SonyEricsson", "MMP", "UCWEB" };

    @Override
    public UserAgentTypes checkUserAgent(String userAgent) {
        UserAgentTypes agentType = UserAgentTypes.DESKTOP;
        for (String mobile : MOBILE_SPECIFIC_SUBSTRING) {
            if (userAgent.contains(mobile) || userAgent.contains(mobile.toUpperCase())
                    || userAgent.contains(mobile.toLowerCase())) {
                agentType = UserAgentTypes.MOBILE;
                break;
            }
        }
        return agentType;
    }

}
