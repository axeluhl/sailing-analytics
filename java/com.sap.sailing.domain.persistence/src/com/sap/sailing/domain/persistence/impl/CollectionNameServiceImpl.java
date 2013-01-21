package com.sap.sailing.domain.persistence.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.persistence.AlreadyRegisteredException;
import com.sap.sailing.domain.persistence.CollectionNameService;

public class CollectionNameServiceImpl implements CollectionNameService {
	/**
	 * collection name -> fully qualified class name
	 */
	private Map<String, String> registered = new HashMap<String, String>();
	
	private static final Logger logger = Logger.getLogger(CollectionNameService.class.getName());
	
	@Override
	public void registerExclusively(Class<?> registerForInterface, String collectionName)
			throws AlreadyRegisteredException {
		String fullyQualified = registerForInterface.getName();
		if (registered.keySet().contains(collectionName) && registered.get(collectionName) != fullyQualified) {
			logger.log(Level.SEVERE, "Same collection name (" + collectionName + " is required in two different places - this may lead to problems: \n" 
					+ " - already registered for: " + registered.get(collectionName) + "\n"
					+ " - tried to register for: " + fullyQualified);
			throw new AlreadyRegisteredException();
		}
		logger.log(Level.INFO, "Registered collection name: " + collectionName);
		registered.put(collectionName, fullyQualified);
	}

}
