package com.sap.sailing.winregatta.resultimport.impl;

import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.common.RegattaScoreCorrections;
import com.sap.sailing.domain.common.ScoreCorrectionProvider;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;

public class ScoreCorrectionProviderImpl implements ScoreCorrectionProvider {
	private static final long serialVersionUID = -52564333737320563L;

	private static final String name = "Official Results from 'WinRegatta Plus#";

    @Override
    public String getName() {
        return name;
    }

	@Override
	public Map<String, Set<Pair<String, TimePoint>>> getHasResultsForBoatClassFromDateByEventName()
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RegattaScoreCorrections getScoreCorrections(String eventName,
			String boatClassName, TimePoint timePoint) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
