package com.sap.sse.security.jaxrs.api;

import java.util.Locale;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONObject;

import com.sap.sse.common.Util;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sap.sse.security.shared.AdminRole;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroupManagementException;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path("/restsecurity")
public class SecurityResource extends AbstractSecurityResource {
    private static final String SECURITY_UI_URL_PATH = "/security/ui/";

    /**
     * Can be used to figure out the current subject. Accepts the GET method. If the subject is
     * authenticated, the service will respond with a "Hello &lt;subjectname&gt;" message, otherwise
     * with a generic "Hello!".
     */
    @GET
    @Path("/hello")
    @Produces("application/json;charset=UTF-8")
    public Response sayHello() {
        return doSayHello();
    }

    private Response doSayHello() {
        final Subject subject = SecurityUtils.getSubject();
        final JSONObject result = new JSONObject();
        result.put("principal", subject.getPrincipal().toString());
        result.put("authenticated", subject.isAuthenticated());
        result.put("remembered", subject.isRemembered());
        return Response.ok(result.toJSONString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Can be used to figure out the current subject. Accepts the POST method. If the subject is
     * authenticated, the service will respond with a "Hello &lt;subjectname&gt;" message, otherwise
     * with a generic "Hello!".
     */
    @POST
    @Path("/hello")
    @Produces("text/plain;charset=UTF-8")
    public Response sayHelloPost() {
        return doSayHello();
    }
    
    @POST
    @Path("/change_password")
    @Produces("text/plain;charset=UTF-8")
    public Response changePassword(@FormParam("username") String username, @FormParam("password") String password) {
        final Subject subject = SecurityUtils.getSubject();
        if (!subject.hasRole(AdminRole.getInstance().getName()) && (subject.getPrincipal() == null
                || !username.equals(subject.getPrincipal().toString()))) {
            return Response.status(Status.UNAUTHORIZED).build();
        } else {
            try {
                getService().updateSimpleUserPassword(username, password);
                return Response.ok().build();
            } catch (UserManagementException e) {
                return Response.status(Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
            }
        }
    }

    @POST
    @Path("/forgot_password")
    @Produces("text/plain;charset=UTF-8")
    public Response forgotPassword(@Context UriInfo uriInfo, @QueryParam("username") String username, @QueryParam("email") String email) {
        try {
            final User user;
            if (username != null) {
                user = getService().getUserByName(username);
            } else if (email != null) {
                user = getService().getUserByEmail(email);
            } else {
                return Response.status(Status.PRECONDITION_FAILED).entity("username or email must be provided").build();
            }
            if (user == null) {
                return Response.status(Status.PRECONDITION_FAILED).entity("user not found").build();
            } else {
                getService().resetPassword(user.getName(), getPasswordResetURL(uriInfo));
                return Response.ok().build();
            }
        } catch (UserManagementException | MailException e) {
            return Response.status(Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/create_user")
    @Produces("text/plain;charset=UTF-8")
    public Response createUser(@Context UriInfo uriInfo, @QueryParam("username") String username, @QueryParam("email") String email,
            @QueryParam("password") String password, @QueryParam("fullName") String fullName,
            @QueryParam("company") String company) {
        return getService().setOwnershipCheckPermissionForObjectCreationAndRevertOnError(SecuredSecurityTypes.USER,
                username,
                username, () -> {
                    try {
                        final String validationBaseURL = getEmailValidationBaseURL(uriInfo);
                        getService().createSimpleUser(username, email, password, fullName, company, Locale.ENGLISH,
                                validationBaseURL);
                        SecurityUtils.getSubject().login(new UsernamePasswordToken(username, password));
                        return respondWithAccessTokenForUser(username);
                    } catch (UserManagementException | MailException | UserGroupManagementException e) {
                        return Response.status(Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
                    }
                });
    }

    private String getEmailValidationBaseURL(UriInfo uriInfo) {
        final String urlPath = SECURITY_UI_URL_PATH+"EmailValidation.html";
        return getContextUrl(uriInfo, urlPath);
    }

    private String getPasswordResetURL(UriInfo uriInfo) {
        final String urlPath = SECURITY_UI_URL_PATH+"EditProfile.html";
        return getContextUrl(uriInfo, urlPath);
    }

    private String getContextUrl(UriInfo uriInfo, final String urlPath) {
        final String validationBaseURL = uriInfo.getBaseUri().getScheme()+"://"+uriInfo.getBaseUri().getHost()+
                (uriInfo.getBaseUri().getPort() == -1 ? "" : (":"+uriInfo.getBaseUri().getPort()))+urlPath;
        return validationBaseURL;
    }

    @GET
    @Path("/user")
    @Produces("application/json;charset=UTF-8")
    public Response getUser(@QueryParam("username") String username) {
        final Subject subject = SecurityUtils.getSubject();
        // ADMIN can query all; otherwise, only the owning user can query
        // TODO: ideally, we would introduce a USER:READ:<username> permission which later can be granted to tenant admins for all users of that tenant
        if (subject.getPrincipal() == null || (username != null && !subject.hasRole(AdminRole.getInstance().getName()))) {
            return Response.status(Status.UNAUTHORIZED).build();
        } else {
            final User user = getService().getUserByName(username == null ? subject.getPrincipal().toString() : username);
            if (user == null) {
                return Response.status(Status.PRECONDITION_FAILED).entity("User "+username+" not known").build();
            } else {
                JSONObject result = new JSONObject();
                result.put("username", user.getName());
                result.put("fullName", user.getFullName());
                result.put("email", user.getEmail());
                result.put("company", user.getCompany());
                return Response.ok(result.toJSONString()).build();
            }
        }
    }
    
    @DELETE
    @Path("/user")
    @Produces("text/plain;charset=UTF-8")
    public Response deleteUser(@QueryParam("username") String username) {
        return getService().checkPermissionAndDeleteOwnershipForObjectRemoval(SecuredSecurityTypes.USER, username,
                () -> {
                    try {
                        getService().deleteUser(username);
                        return Response.ok().build();
                    } catch (UserManagementException e) {
                        return Response.status(Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
                    }
                });
    }
    
    @PUT
    @Path("/user")
    @Produces("text/plain;charset=UTF-8")
    public Response updateUser(@Context UriInfo uriInfo, @QueryParam("username") String username,
            @QueryParam("email") String email, @QueryParam("fullName") String fullName,
            @QueryParam("company") String company) {
        final Subject subject = SecurityUtils.getSubject();
        // the signed-in subject has role ADMIN
        if (!subject.hasRole(AdminRole.getInstance().getName()) && (subject.getPrincipal() == null
                || !username.equals(subject.getPrincipal().toString()))) {
            return Response.status(Status.UNAUTHORIZED).build();
        } else {
            try {
                final User user = getService().getUserByName(username);
                if (user == null) {
                    return Response.status(Status.PRECONDITION_FAILED).entity("User "+username+" not known").build();
                } else {
                    getService().updateUserProperties(username, fullName, company, user.getLocale());
                    if (!Util.equalsWithNull(user.getEmail(), email)) {
                        getService().updateSimpleUserEmail(username, email, getEmailValidationBaseURL(uriInfo));
                    }
                    return Response.ok().build();
                }
            } catch (UserManagementException e) {
                return Response.status(Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
            }
        }
    }
    
    @GET
    @Path("/access_token")
    @Produces("application/json;charset=UTF-8")
    public Response accessToken() {
        return respondWithAccessTokenForAuthenticatedSubject();
    }

    @POST
    @Path("/access_token")
    @Produces("application/json;charset=UTF-8")
    public Response accessTokenPost() {
        return respondWithAccessTokenForAuthenticatedSubject();
    }
    
    @GET
    @Path("/logout")
    @Produces("application/json;charset=UTF-8")
    public Response logout() {
        return logoutPost();
    }

    @POST
    @Path("/logout")
    @Produces("application/json;charset=UTF-8")
    public Response logoutPost() {
        getService().logout();
        return Response.ok().build();
    }

    @GET
    @Path("/remove_access_token")
    @Produces("application/json;charset=UTF-8")
    public Response removeAccessToken() {
        return removeAccessTokenPost();
    }

    @POST
    @Path("/remove_access_token")
    @Produces("application/json;charset=UTF-8")
    public Response removeAccessTokenPost() {
        final Response result;
        final Object principal = SecurityUtils.getSubject().getPrincipal();
        if (principal != null) {
            final String username = principal.toString();
            result = respondToRemoveAccessTokenForUser(username);
        } else {
            result = Response.status(Status.UNAUTHORIZED).build();
        }
        return result;
    }

    Response respondToRemoveAccessTokenForUser(final String username) {
        final Response result;
        getService().removeAccessToken(username);
        result = Response.ok().build();
        return result;
    }

    private Response respondWithAccessTokenForAuthenticatedSubject() {
        final Response result;
        final Object principal = SecurityUtils.getSubject().getPrincipal();
        if (principal != null) {
            final String username = principal.toString();
            result = respondWithAccessTokenForUser(username);
        } else {
            result = Response.status(Status.UNAUTHORIZED).build();
        }
        return result;
    }

    Response respondWithAccessTokenForUser(final String username) {
        JSONObject response = new JSONObject();
        response.put("username", username);
        String accessToken = getService().getOrCreateAccessToken(username);
        if (accessToken == null) {
            accessToken = getService().createAccessToken(username);
        }
        response.put("access_token", accessToken);
        return Response.ok(response.toJSONString(), MediaType.APPLICATION_JSON_TYPE).build();
    }
}