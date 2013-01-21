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
		service.registerExclusively(TestInterface.class, "a");
		service.registerExclusively(TestInterface.class, "b");
		service.registerExclusively(TestInterface.class, "B");
		service.registerExclusively(TestInterface.class, "B");
	}
	
	@Test(expected=AlreadyRegisteredException.class)
	public void shouldNotWork() throws AlreadyRegisteredException {
		service.registerExclusively(TestInterface.class, "b");
		service.registerExclusively(com.sap.sailing.domain.persistence.test.needadifferentpackage.TestInterface.class, "b");
	}
}
