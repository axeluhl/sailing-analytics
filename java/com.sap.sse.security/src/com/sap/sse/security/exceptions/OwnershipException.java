package com.sap.sse.security.exceptions;

import javax.ws.rs.core.Response.Status;
import com.sap.sse.security.jaxrs.api.OwnershipResource;

/***
 * OwnershipException is used for {@link OwnershipResource} class to handle any exception in ownership Api's.
 * @author Usman Ali
 *
 */
public class OwnershipException extends Exception{
	
	private static final long serialVersionUID = 1L;
	
	private Status status;
	
	public OwnershipException(String message, Status status) {
		super(message);
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}
	
	
	
}
