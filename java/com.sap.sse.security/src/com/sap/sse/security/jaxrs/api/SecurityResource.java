package com.sap.sse.security.jaxrs.api;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

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
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.security.SecurityUrlPathProvider;
import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.User;
import com.sun.jersey.api.client.ClientResponse.Status;

@Path(SecurityResource.RESTSECURITY)
public class SecurityResource extends AbstractSecurityResource {
    private static final Logger logger = Logger.getLogger(SecurityResource.class.getName());
    
    public static final String USERS_WITH_PERMISSION_METHOD = "/users_with_permission";
    public static final String HELLO_METHOD = "/hello";
    public static final String CHANGE_PASSWORD_METHOD = "/change_password";
    public static final String FORGOT_PASSWORD_METHOD = "/forgot_password";
    public static final String CREATE_USER_METHOD = "/create_user";
    public static final String USER_METHOD = "/user";
    public static final String HAS_PERMISSION_METHOD = "/has_permission";
    public static final String REMOVE_ACCESS_TOKEN_METHOD = "/remove_access_token";
    public static final String LOGOUT_METHOD = "/logout";
    /**
     * The path to put behind the application's prefix {@code /security/api} to reach this resource
     */
    public static final String RESTSECURITY = "/restsecurity";
    public static final String COMPANY = "company";
    public static final String FULL_NAME = "fullName";
    public static final String EMAIL = "email";
    private static final String SECURITY_UI_URL_PATH = "/security/ui/";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String PERMISSION = "permission";
    public static final String GRANTED = "granted";
    public static final String ACCESS_TOKEN_METHOD = "/"+ACCESS_TOKEN;

    /**
     * Can be used to figure out the current subject. Accepts the GET method. If the subject is
     * authenticated, the service will respond with a "Hello &lt;subjectname&gt;" message, otherwise
     * with a generic "Hello!".
     */
    @GET
    @Path(HELLO_METHOD)
    @Produces("application/json;charset=UTF-8")
    public Response sayHello() {
        return doSayHello();
    }
    
    @GET
    @Path(USERS_WITH_PERMISSION_METHOD)
    @Produces("text/plain;charset=UTF-8")
    public Response getUsersWithPermission(@QueryParam(PERMISSION) String permission) {
        final TimePoint start = TimePoint.now();
        try {
            final WildcardPermission wildcardPermission = new WildcardPermission(permission);
            final Iterable<User> usersWithPermission = getService().getUsersWithPermissions(wildcardPermission);
            final JSONArray usernames = new JSONArray();
            for (final User userWithPermission : usersWithPermission) {
                if (getService().hasCurrentUserReadPermission(userWithPermission)) {
                    usernames.add(userWithPermission.getName());
                }
            }
            logger.info("Request for users with permission took "+start.until(TimePoint.now()));
            return Response.status(Status.OK).entity(streamingOutput(usernames)).build();
        } catch (Exception e) {
            return Response.status(Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
        }
    }

    private Response doSayHello() {
        final Subject subject = SecurityUtils.getSubject();
        final JSONObject result = new JSONObject();
        result.put("principal", subject.getPrincipal().toString());
        result.put("authenticated", subject.isAuthenticated());
        result.put("remembered", subject.isRemembered());
        return Response.ok(streamingOutput(result), MediaType.APPLICATION_JSON_TYPE).build();
    }

    /**
     * Can be used to figure out the current subject. Accepts the POST method. If the subject is
     * authenticated, the service will respond with a "Hello &lt;subjectname&gt;" message, otherwise
     * with a generic "Hello!".
     */
    @POST
    @Path(HELLO_METHOD)
    @Produces("text/plain;charset=UTF-8")
    public Response sayHelloPost() {
        return doSayHello();
    }
    
    @POST
    @Path(CHANGE_PASSWORD_METHOD)
    @Produces("text/plain;charset=UTF-8")
    public Response changePassword(@FormParam(USERNAME) String username, @FormParam(PASSWORD) String password) {
        if (!getService().hasCurrentUserUpdatePermission(getService().getUserByName(username))) {
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
    @Path(FORGOT_PASSWORD_METHOD)
    @Produces("text/plain;charset=UTF-8")
    public Response forgotPassword(@Context UriInfo uriInfo, @QueryParam(USERNAME) String username,
            @QueryParam(EMAIL) String email, @QueryParam("application") String application) {
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
                getService().resetPassword(user.getName(), getPasswordResetURL(uriInfo, application));
                return Response.ok().build();
            }
        } catch (UserManagementException | MailException e) {
            return Response.status(Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path(CREATE_USER_METHOD)
    @Produces("text/plain;charset=UTF-8")
    public Response createUser(@Context UriInfo uriInfo,
            @QueryParam(USERNAME) String queryUsername, @FormParam(USERNAME) String formUsername,
            @QueryParam(EMAIL) String queryEmail, @FormParam(EMAIL) String formEmail,
            @QueryParam(PASSWORD) String queryPassword, @FormParam(PASSWORD) String formPassword,
            @QueryParam(FULL_NAME) String queryFullName, @FormParam(FULL_NAME) String formFullName,
            @QueryParam(COMPANY) String queryCompany, @FormParam(COMPANY) String formCompany) {
        try {
            User user = getService().checkPermissionForObjectCreationAndRevertOnErrorForUserCreation(queryUsername,
                    new Callable<User>() {
                        @Override
                        public User call() throws Exception {
                            final String validationBaseURL = getEmailValidationBaseURL(uriInfo);
                            final String usernameToUse = preferFirstIfNotNullOrElseSecond(formUsername, queryUsername);
                            final String passwordToUse = preferFirstIfNotNullOrElseSecond(formPassword, queryPassword);
                            final String emailToUse = preferFirstIfNotNullOrElseSecond(formEmail, queryEmail);
                            final String fullNameToUse = preferFirstIfNotNullOrElseSecond(formFullName, queryFullName);
                            final String companyToUse = preferFirstIfNotNullOrElseSecond(formCompany, queryCompany);
                            User newUser = getService().createSimpleUser(usernameToUse, emailToUse, passwordToUse, fullNameToUse, companyToUse,
                                    Locale.ENGLISH, validationBaseURL, getService().getDefaultTenantForCurrentUser());
                            SecurityUtils.getSubject().login(new UsernamePasswordToken(usernameToUse, passwordToUse));
                            return newUser;
                        }

                        private String preferFirstIfNotNullOrElseSecond(String first, String second) {
                            final String result;
                            if (first != null) {
                                result = first;
                            } else {
                                result = second;
                            }
                            return result;
                        }
                    });
            return respondWithAccessTokenForUser(user.getName());
        } catch (Exception e) {
            return Response.status(Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
        }
    }

    private String getEmailValidationBaseURL(UriInfo uriInfo) {
        final String urlPath = SECURITY_UI_URL_PATH+"EmailValidation.html";
        return getContextUrl(uriInfo, urlPath);
    }

    private String getPasswordResetURL(UriInfo uriInfo, String application) {
        final SecurityUrlPathProvider securityUrlPathProvider = getSecurityUrlPathProvider(application);
        final String urlPath = securityUrlPathProvider.getPasswordResetUrlPath();
        return getContextUrl(uriInfo, urlPath);
    }

    private String getContextUrl(UriInfo uriInfo, final String urlPath) {
        final String validationBaseURL = uriInfo.getBaseUri().getScheme()+"://"+uriInfo.getBaseUri().getHost()+
                (uriInfo.getBaseUri().getPort() == -1 ? "" : (":"+uriInfo.getBaseUri().getPort()))+urlPath;
        return validationBaseURL;
    }

    @GET
    @Path(USER_METHOD)
    @Produces("application/json;charset=UTF-8")
    public Response getUser(@QueryParam(USERNAME) String username) {
        final Subject subject = SecurityUtils.getSubject();
        final User user = getService().getUserByName(username == null ? subject.getPrincipal().toString() : username);
        if (user == null) {
            return Response.status(Status.PRECONDITION_FAILED).entity("User "+username+" not known").build();
        } else if (getService().hasCurrentUserReadPermission(user) || getService()
                .hasCurrentUserOneOfExplicitPermissions(user, SecuredSecurityTypes.PublicReadableActions.READ_PUBLIC)) {
            JSONObject result = new JSONObject();
            result.put(USERNAME, user.getName());
            if (getService().hasCurrentUserReadPermission(user)) {
                result.put(FULL_NAME, user.getFullName());
                result.put(EMAIL, user.getEmail());
                result.put(COMPANY, user.getCompany());
            }
            return Response.ok(streamingOutput(result)).build();
        } else {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }
    
    @DELETE
    @Path(USER_METHOD)
    @Produces("text/plain;charset=UTF-8")
    public Response deleteUser(@QueryParam(USERNAME) String username) {
        User user = getService().getUserByName(username);
        if (user != null) {
            return getService().checkPermissionAndDeleteOwnershipForObjectRemoval(user, () -> {
                try {
                    getService().deleteUser(username);
                    return Response.ok().build();
                } catch (UserManagementException e) {
                    return Response.status(Status.PRECONDITION_FAILED).entity(e.getMessage()).build();
                }
            });
        } else {
            return Response.status(Status.PRECONDITION_FAILED).entity("unknown id").build();
        }
    }
    
    @PUT
    @Path(USER_METHOD)
    @Produces("text/plain;charset=UTF-8")
    public Response updateUser(@Context UriInfo uriInfo, @QueryParam(USERNAME) String username,
            @QueryParam(EMAIL) String email, @QueryParam(FULL_NAME) String fullName,
            @QueryParam(COMPANY) String company) {
        if (!getService().hasCurrentUserUpdatePermission(getService().getUserByName(username))) {
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
    @Path(ACCESS_TOKEN_METHOD)
    @Produces("application/json;charset=UTF-8")
    public Response accessToken() {
        return respondWithAccessTokenForAuthenticatedSubject();
    }

    @POST
    @Path(ACCESS_TOKEN_METHOD)
    @Produces("application/json;charset=UTF-8")
    public Response accessTokenPost() {
        return respondWithAccessTokenForAuthenticatedSubject();
    }
    
    @GET
    @Path(LOGOUT_METHOD)
    @Produces("application/json;charset=UTF-8")
    public Response logout() {
        return logoutPost();
    }

    @POST
    @Path(LOGOUT_METHOD)
    @Produces("application/json;charset=UTF-8")
    public Response logoutPost() {
        getService().logout();
        return Response.ok().build();
    }

    @GET
    @Path(REMOVE_ACCESS_TOKEN_METHOD)
    @Produces("application/json;charset=UTF-8")
    public Response removeAccessToken() {
        return removeAccessTokenPost();
    }

    @POST
    @Path(REMOVE_ACCESS_TOKEN_METHOD)
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
    
    @GET
    @Path(HAS_PERMISSION_METHOD)
    @Produces("application/json;charset=UTF-8")
    public Response getPermission(@QueryParam(PERMISSION) final List<String> permissionsAsStrings) {
        final JSONArray result = new JSONArray();
        for (final String permissionAsString : permissionsAsStrings) {
            final JSONObject entry = new JSONObject();
            result.add(entry);
            entry.put(PERMISSION, permissionAsString);
            entry.put(GRANTED, SecurityUtils.getSubject().isPermitted(permissionAsString));
        }
        return Response.ok(streamingOutput(result), MediaType.APPLICATION_JSON_TYPE).build(); 
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
        response.put(USERNAME, username);
        getService().checkCurrentUserReadPermission(getService().getUserByName(username));
        String accessToken;
        if (getService().hasCurrentUserUpdatePermission(getService().getUserByName(username))) {
            accessToken = getService().getOrCreateAccessToken(username);
        } else {
            accessToken = getService().getAccessToken(username);
            if (accessToken == null) {
                throw new AuthorizationException(
                        "No access token was found and the permission to create one is lacking.");
            }
        }
        response.put(ACCESS_TOKEN, accessToken);
        return Response.ok(streamingOutput(response), MediaType.APPLICATION_JSON_TYPE).build();
    }
}