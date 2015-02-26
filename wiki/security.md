# Security

The Sports Sponsorships Engine (SSE) on which the SAP Sailing Analytics and the SAP Tennis Analytics are based, uses Apache Shiro to implement security. This in particular includes authentication and authorization.

## Users, Sessions, Roles, and Permissions

Users are identified in an authentication process. This can be a username/password login implemented by posting a form, through HTTP basic authentication (an "Authorization: Basic <some-base64-string>" HTTP header field) or by using an OAuth-like bearer access token that can be obtained by an authenticated user through a RESTful web service.

The response for an HTTP request by an authenticated user contains the JSESSIONID cookie whose value is the session key. Our sessions have a default timeout of 30 minutes (see 

## How to Configure

