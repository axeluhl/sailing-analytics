# Security

The Sports Sponsorships Engine (SSE) on which the SAP Sailing Analytics and the SAP Tennis Analytics are based, uses Apache Shiro to implement security. This in particular includes authentication and authorization. This document does not aim to replace the Shiro documentation which is available, e.g., at [http://shiro.apache.org/configuration.html](http://shiro.apache.org/configuration.html), [http://shiro.apache.org/permissions.html](http://shiro.apache.org/permissions.html) and [http://shiro.apache.org/web.html](http://shiro.apache.org/web.html).

## Users, Sessions, Roles, and Permissions

Users are identified in an authentication process. This can currently be a username/password login implemented by posting a form, through HTTP basic authentication (an "Authorization: Basic <some-base64-string>" HTTP header field) or by using an OAuth-like bearer access token that can be obtained by an authenticated user through a RESTful web service. OAuth-based authentication by external OAuth providers and SAP ID is planned (see [Bug 2482](http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2482)).

The response for an HTTP request by an authenticated user contains the JSESSIONID cookie whose value is the session key. Our sessions have a default timeout of 30 minutes which is provided as the Shiro default (see `org.apache.shiro.session.mgt.DefaultSessionManager.DEFAULT_GLOBAL_SESSION_TIMEOUT` and its use in the `org.apache.shiro.session.mgt.SimpleSession` constructor). Each request performed with this session key will renew the session timeout.

Logging out, e.g., by invoking the `/security/api/restsecurity/logout` service, invalidates the session key, and a new authentication will be required to obtain a new session.

An authenticated user has zero or more roles and zero or more immediate permissions assigned. Roles, in turn, can imply more permissions which are added to the immediate permissions to result in the complete set of permissions the user has. Roles and immediate permissions are stored persistently in an instance of `com.sap.sse.security.UserStore` and can be dynamically adjusted during application run-time.

The inference from roles to the permissions implied by those roles happens by implementations of the `com.sap.sse.security.shared.PermissionsForRoleProvider` interface. Authentication is mainly performed by so-called "Realm" implementations. SSE provides an abstract base class `com.sap.sse.security.AbstractUserStoreBasedRealm`. It supports setting a `PermissionsForRoleProvider`. Additionally, a `PermissionsForRoleProvider` can be used with `com.sap.sse.security.ui.shared.UserDTO.getAllPermissions(...)` so that UI clients can have the same set of permissions inferred for a user that the Shiro back-end will use for permission checks, so as to enable an disable UI features based on the user's permissions.

## How to Configure

## Notes on Replication

