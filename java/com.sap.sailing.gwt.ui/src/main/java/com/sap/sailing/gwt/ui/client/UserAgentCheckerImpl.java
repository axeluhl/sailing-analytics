package com.sap.sailing.gwt.ui.client;

import java.util.HashMap;

import com.sap.sailing.gwt.ui.client.UserAgentDetails.AgentTypes;

public class UserAgentCheckerImpl implements UserAgentChecker {
    
    @SuppressWarnings("serial")
	private static final HashMap<AgentTypes, Integer> UNSUPPORTED_AGENTS = new HashMap<AgentTypes, Integer>() {{
    	put(AgentTypes.MSIE, 9);
    	put(AgentTypes.SAFARI, 5);
    	put(AgentTypes.OPERA, 10);
    	put(AgentTypes.FIREFOX, 10);
    	put(AgentTypes.CHROME, 20);
    	
    }};

	@Override
	public boolean isUserAgentSupported(UserAgentDetails details) {
		if (UNSUPPORTED_AGENTS.containsKey(details.getType())) {
			if (details.getVersion()[0] < UNSUPPORTED_AGENTS.get(details.getType())) {
				return false;
			}
		}
		
		/* returning true for entries not listed */
		return true;
	}
	
}
