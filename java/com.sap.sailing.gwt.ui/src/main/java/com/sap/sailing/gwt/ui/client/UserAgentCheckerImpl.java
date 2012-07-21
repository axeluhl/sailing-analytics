package com.sap.sailing.gwt.ui.client;

import java.util.HashMap;

import com.sap.sailing.gwt.ui.client.UserAgentDetails.AgentTypes;

public class UserAgentCheckerImpl implements UserAgentChecker {
    
    private static final String[] MOBILE_SPECIFIC_SUBSTRING = { "iPhone", "Android", "MIDP", "Opera Mobi", "iPad",
            "Opera Mini", "BlackBerry", "HP iPAQ", "IEMobile", "MSIEMobile", "Windows Phone", "HTC", "LG", "MOT",
            "Nokia", "Symbian", "Fennec", "Maemo", "Tear", "Midori", "armv", "Windows CE", "WindowsCE", "Smartphone",
            "240x320", "176x220", "320x320", "160x160", "webOS", "Palm", "Sagem", "Samsung", "SGH", "SIE",
            "SonyEricsson", "MMP", "UCWEB" };
    
    @SuppressWarnings("serial")
	private static final HashMap<AgentTypes, Integer> UNSUPPORTED_AGENTS = new HashMap<AgentTypes, Integer>() {{
    	put(AgentTypes.MSIE, 9);
    	put(AgentTypes.SAFARI, 5);
    	put(AgentTypes.OPERA, 10);
    	put(AgentTypes.FIREFOX, 10);
    	put(AgentTypes.CHROME, 20);
    	
    }};

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

	/**
	 * Returns false if the given userAgent is not supported.
	 */
	@Override
	public boolean isUserAgentSupported(String userAgent) {
		UserAgentDetails details = new UserAgentDetails(userAgent);
		if (UNSUPPORTED_AGENTS.containsKey(details.getType())) {
			if (details.getVersion()[0] < UNSUPPORTED_AGENTS.get(details.getType())) {
				return false;
			}
		}
		
		/* returning true for entries not listed */
		return true;
	}
	
}
