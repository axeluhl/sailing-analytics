package com.sap.sailing.domain.persistence.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.persistence.AlreadyRegisteredException;
import com.sap.sailing.domain.persistence.CollectionNameService;

public class CollectionNameServiceImpl implements CollectionNameService {
	private Set<String> registered = new HashSet<String>();
	
	private static final Logger logger = Logger.getLogger(CollectionNameService.class.getName());
	
	@Override
	public void registerExclusively(String collectionName)
			throws AlreadyRegisteredException {
		if (registered.contains(collectionName)) {
			logger.log(Level.SEVERE, "Same collection name is required in two different places - this may lead to problems: " + collectionName);
			throw new AlreadyRegisteredException();
		}
		logger.log(Level.INFO, "Registered collection name: " + collectionName);
		registered.add(collectionName);
	}

}
