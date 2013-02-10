package com.sap.sailing.domain.racelog.impl;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.racelog.RaceColumnIdentifier;

public class RaceColumnIdentifierImpl implements RaceColumnIdentifier {
	
	private Named named;
	private String columnName;

	public RaceColumnIdentifierImpl(Named named, String columnName) {
		this.named = named;
		this.columnName = columnName;
	}

	@Override
	public String getIdentifier() {
		return String.format("%s-%s", named.getName(), columnName);
	}

}
