package com.sap.sailing.domain.persistence.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.persistence.AlreadyRegisteredException;
import com.sap.sailing.domain.persistence.CollectionNameService;

public class CollectionNameServiceImpl implements CollectionNameService {
	private Map<String, Class<?>> registered = new HashMap<String, Class<?>>();
	
	private static final Logger logger = Logger.getLogger(CollectionNameService.class.getName());
	
	@Override
	public void registerExclusively(Class<?> registerForThisClass, String collectionName)
			throws AlreadyRegisteredException {
		if (registered.keySet().contains(collectionName) && registered.get(collectionName) != registerForThisClass) {
			logger.log(Level.SEVERE, "Same collection name (" + collectionName + " is required in two different places - this may lead to problems: \n" 
					+ " - already registered for: " + registered.get(collectionName).getName() + "\n"
					+ " - tried to register for: " + registerForThisClass.getName());
			throw new AlreadyRegisteredException();
		}
		logger.log(Level.INFO, "Registered collection name: " + collectionName);
		registered.put(collectionName, registerForThisClass);
	}

}
