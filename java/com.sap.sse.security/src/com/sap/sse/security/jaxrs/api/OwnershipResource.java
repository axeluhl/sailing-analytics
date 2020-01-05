package com.sap.sse.security.jaxrs.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.SecurityUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sap.sse.security.exceptions.OwnershipException;
import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sap.sse.security.model.GeneralResponse;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.TypeRelativeObjectIdentifier;
import com.sap.sse.security.shared.impl.QualifiedObjectIdentifierImpl;
import com.sap.sse.security.shared.impl.User;
import com.sap.sse.security.shared.impl.UserGroup;

@Path("/restsecurity/ownership")
public class OwnershipResource extends AbstractSecurityResource {
	private static final String KEY_OBJECT_ID = "objectId";
	private static final String KEY_OBJECT_TYPE = "objectType";
	private static final String KEY_USERNAME = "username";
	private static final String KEY_GROUP_ID="groupId";

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("application/json;charset=UTF-8")
	public Response createUser(String jsonBody) throws OwnershipException {

		final JSONObject json = (JSONObject) JSONValue.parse(jsonBody);
		User user =getService().getUserByName((String)json.get(KEY_USERNAME));
		if(user == null) {
			throw new OwnershipException("User Not found",Status.BAD_REQUEST);
		}
		UserGroup userGroup =getService().getUserGroupByName((String)json.get(KEY_GROUP_ID));
		if(userGroup == null) {
			throw new OwnershipException("UserGroup Not found",Status.BAD_REQUEST);
		}
		QualifiedObjectIdentifier identifier = new QualifiedObjectIdentifierImpl(
				(String)json.get(KEY_OBJECT_TYPE), 
				new TypeRelativeObjectIdentifier((String)json.get(KEY_OBJECT_ID))
				);

		try {
			SecurityUtils.getSubject()
			.checkPermission(identifier.getStringPermission(DefaultActions.CHANGE_OWNERSHIP));
		}catch(Exception ex) {
			throw new OwnershipException("Not permitted to change ownership.",	Status.FORBIDDEN);
		}

		getService().setOwnership(identifier, user, userGroup);
		
		
		return Response.ok(new GeneralResponse(true, "Ownership changed successfully").toString()).build();
	}
}
