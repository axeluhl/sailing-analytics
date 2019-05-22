package com.sap.sse.security.jaxrs.api;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

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
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sse.common.Util;
import com.sap.sse.common.mail.MailException;
import com.sap.sse.security.jaxrs.AbstractSecurityResource;
import com.sap.sse.security.shared.RoleDefinition;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.User;
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
    public Response createUser(@Context UriInfo uriInfo,
            @QueryParam("username") String queryUsername, @FormParam("username") String formUsername,
            @QueryParam("email") String queryEmail, @FormParam("email") String formEmail,
            @QueryParam("password") String queryPassword, @FormParam("password") String formPassword,
            @QueryParam("fullName") String queryFullName, @FormParam("fullName") String formFullName,
            @QueryParam("company") String queryCompany, @FormParam("company") String formCompany) {
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
        final User user = getService().getUserByName(username == null ? subject.getPrincipal().toString() : username);
        if (user == null) {
            return Response.status(Status.PRECONDITION_FAILED).entity("User "+username+" not known").build();
        } else if (getService().hasCurrentUserReadPermission(user) || getService()
                .hasCurrentUserOneOfExplicitPermissions(user, SecuredSecurityTypes.PublicReadableActions.READ_PUBLIC)) {
            // TODO: pruning when current user only has READ_PUBLIC
            JSONObject result = new JSONObject();
            result.put("username", user.getName());
            result.put("fullName", user.getFullName());
            result.put("email", user.getEmail());
            result.put("company", user.getCompany());
            return Response.ok(result.toJSONString()).build();
        } else {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }
    
    @DELETE
    @Path("/user")
    @Produces("text/plain;charset=UTF-8")
    public Response deleteUser(@QueryParam("username") String username) {
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
    @Path("/user")
    @Produces("text/plain;charset=UTF-8")
    public Response updateUser(@Context UriInfo uriInfo, @QueryParam("username") String username,
            @QueryParam("email") String email, @QueryParam("fullName") String fullName,
            @QueryParam("company") String company) {
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

    @POST
    @Path("/role")
    @Produces("text/plain;charset=UTF-8")
    public Response updatePermissionsForRole(@Context UriInfo uriInfo, @QueryParam("roleId") String queryRoleId,
            @FormParam("roleId") String formRoleId, @QueryParam("permissions") List<String> queryPermissions,
            @FormParam("permissions") List<String> formPermissions) {

        final String roleId = preferFirstIfNotNullOrElseSecond(formRoleId, queryRoleId);
        final List<String> permissionStrings = preferFirstIfNotEmptyOrElseSecond(formPermissions, queryPermissions);

        Response resp;
        try {

            // parse UUID
            final UUID roleUUID = UUID.fromString(roleId);

            // get role definition from role id
            final RoleDefinition roleDefinition = getService().getRoleDefinition(roleUUID);

            // null check role definition
            if (roleDefinition == null) {
                resp = Response.status(Status.NOT_FOUND).entity(String.format("No role with id '%s' found.", roleUUID))
                        .build();
            } else {
                // check update permission on role
                getService().checkCurrentUserUpdatePermission(roleDefinition);

                // create permission objects
                final Set<WildcardPermission> permissions = new HashSet<>();
                for (String permissionString : permissionStrings) {
                    permissions.add(new WildcardPermission(permissionString));
                }

                // check only those metea-permissions which changed
                Set<WildcardPermission> addedPermissions = new HashSet<>(roleDefinition.getPermissions());
                addedPermissions.removeAll(permissions);

                if (!getService().hasUserAllWildcardPermissionsForAlreadyRealizedQualifications(roleDefinition,
                        addedPermissions)) {
                    resp = Response.status(Status.UNAUTHORIZED)
                            .entity("Not permitted to grant permissions for role " + roleDefinition.getName()).build();
                } else {
                    // update role definitino with new permissions
                    roleDefinition.setPermissions(permissions);
                    getService().updateRoleDefinition(roleDefinition);
                    resp = Response.ok().build();
                }
            }
        } catch (IllegalArgumentException e) {
            resp = Response.status(Status.BAD_REQUEST).entity("Invalid roleId.").build();
        } catch (UnauthorizedException e) {
            resp = Response.status(Status.UNAUTHORIZED).build();
        }
        return resp;
    }

    @GET
    @Path("/role")
    @Produces("application/json;charset=UTF-8")
    public Response getPermissionsForRole(@Context UriInfo uriInfo, @QueryParam("roleId") String roleId) {

        Response resp;
        try {

            // parse UUID
            final UUID roleUUID = UUID.fromString(roleId);

            // get role definition from role id
            RoleDefinition roleDefinition = getService().getRoleDefinition(roleUUID);

            // null check role definition
            if (roleDefinition == null) {
                resp = Response.status(Status.NOT_FOUND).entity(String.format("No role with id '%s' found.", roleUUID))
                        .build();
            } else {
                // check read permission on role
                getService().checkCurrentUserReadPermission(roleDefinition);

                // build json result with permissions and id
                JSONObject jsonResult = new JSONObject();
                JSONArray jsonPermissions = new JSONArray();
                for (WildcardPermission permission : roleDefinition.getPermissions()) {
                    jsonPermissions.add(permission.toString());
                }
                jsonResult.put("permissions", jsonPermissions);
                jsonResult.put("id", roleId);
                resp = Response.ok(jsonResult.toJSONString()).build();
            }
        } catch (IllegalArgumentException e) {
            resp = Response.status(Status.BAD_REQUEST).entity("Invalid roleId.").build();
        } catch (UnauthorizedException e) {
            resp = Response.status(Status.UNAUTHORIZED).build();
        }
        return resp;
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
        response.put("access_token", accessToken);
        return Response.ok(response.toJSONString(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    /** Returns the second object if the first is null. */
    private <T> T preferFirstIfNotNullOrElseSecond(T first, T second) {
        final T result;
        if (first != null) {
            result = first;
        } else {
            result = second;
        }
        return result;
    }

    /** Selects the second list if the first list is null or empty. */
    private <T> List<T> preferFirstIfNotEmptyOrElseSecond(List<T> first, List<T> second) {
        final List<T> result;
        if (first != null && !first.isEmpty()) {
            result = first;
        } else {
            result = second;
        }
        return result;
    }
}