package com.sap.sailing.domain.persistence.test;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.persistence.AlreadyRegisteredException;
import com.sap.sailing.domain.persistence.CollectionNameService;
import com.sap.sailing.domain.persistence.impl.CollectionNameServiceImpl;

public class CollectionNameServiceTest {
	private CollectionNameService service;
	
	@Before
	public void setUp() {
		service = new CollectionNameServiceImpl();
	}
	
	@Test
	public void shouldWork() throws AlreadyRegisteredException {
		service.registerExclusively("a");
		service.registerExclusively("b");
		service.registerExclusively("B");
	}
	
	@Test(expected=AlreadyRegisteredException.class)
	public void shouldNotWork() throws AlreadyRegisteredException {
		service.registerExclusively("b");
		service.registerExclusively("b");
	}
}
