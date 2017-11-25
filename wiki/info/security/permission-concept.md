# Table of Contents

* [[Introduction|#introduction]]
* [[Access Control Concepts|#access-control-concepts]]

# Introduction

This document describes the permission concept developed for the SAP Sailing Analytics (in the following just Sailing Analytics). Currently a very rough permission system based on role based access control (RBAC) is used to e.g. restrict access to the administration console. The system is built on the Apache Shiro (in the following just Shiro) framework. This system currently does not support unified user management (in the sense of a central user management system that manages the users for all deployments of the Sailing Analytics) or dynamic access control for all aspects of the Sailing Analytics.

However, as one medium term goal is to develop the Sailing Analytics to be usable by sailing clubs and eventually individuals as a cloud application, the user management and access control should be unified and expanded to be dynamic and provide the appropriate (the Sailing Analytics manages very personal data) security aspects. 
The concept developed in this document should focus on fine grained access control with a strong focus on being able to unify and strengthen the user management and access control. Resulting in a system where multiple organizations and individuals can work on one system without unwantedly interfering in their actions and data. 
A secure access control concept for the existing data model is not easily developed, because the data objects in the Sailing Analytics do not merely form static trees. Data objects can form graphs where there is no clear root for a given node. Furthermore the associations of data objects can change. These challenges have to be addressed by possible concepts by adapting them to this very specific domain.

The following requirements result from the above described (the access control system has to…):

* Be expressive enough to support the complex associations of the Sailing Analytics
* Support multiple organizations (clubs, events and individuals) working in one system
* Communicate the permissions to the frontend (so only UI elements that support permitted actions are active)
* Be reasonably complex and implementation intensive

# Access Control Concepts

The two big concepts that play together in this permission concept are access control lists (ACLs) (also used e.g. in the Linux or Windows file system) and RBAC. Furthermore, there is the concept of attribute based access control (ABAC) that is not explored in this concept document.
The concept of ACLs is based on the idea of assigning each data object that is access controlled an ACL. The ACL is a list of entries that assign a user or group of users to permissions. If e.g. read access is requested for a data object, its ACL is checked if the user or a group, the user belongs to, has an entry granting the read permission.
RBAC is based on the idea of having roles that imply certain permissions and assigning roles to users. The roles of RBAC are on a simple level equivalent to groups for ACLs. (Barkley, 1997) However, in general roles combine a set of users with a set of permissions, whereas groups represent only a set of users. According to (Sandhu, Coyne, Feinstein, & Youman, 1996) there are multiple models for RBAC. The model described above is called RBAC_0. Furthermore, there are RBAC_1, RBAC_2 and RBAC_3 which all include RBAC_0, but add additional features. 
RBAC_1 adds the concept of role hierarchies, where roles inherit the permissions granted by their parent roles. However, they do not inherit the set of users. RBAC_2 adds constraints which restrict how and when roles and permissions can be combined with other roles or permissions. Besides mutual exclusion of roles or permissions, constraints could also require a user to have role s when assigned role r (or the same with permissions, which could be used e.g. to require to be able to view an event when a view permission for a race in the event is granted). Furthermore, the concept of constraints could also restrict the roles and permissions that a user can simultaneously have in a session (e.g. only one tenant role at a time). RBAC_3 combines RBAC_1 and RBAC_2.
In the context of RBAC (Ferraiolo, Cugini, & Kuhn) mentions the concept of subjects. A user may in one session only have a certain set of roles and permissions. This may be due to choice to reduce accidental actions with a wrong role or a constraint enforced by the system (See RBAC_2). This set of roles and permissions is called a subject. A user may have any number of active subjects. However, a subject is only associated with one user.
It is to note that simple RBAC models show no difference in their ability to express access control policies than ACLs. (Barkley, 1997) More complex RBAC models are more expressive than ACLs.
In the course of this concept document there will be no difference in meaning between roles and groups. In the existing system they are named roles, thus the term roles will be used for both roles and groups.

# Initial Idea

The inital idea for this document is based on the existing Shiro RBAC system that should handle global static roles and also supports directly granting permissions to a user. Furthermore, ACLs should be introduced. Those should solve the problem of “losing” implied permissions, because the access control lists are directly associated with the data object. The existing Shiro authorizing realm that checks for the roles and permissions directly assigned to a user would have to be extended by the ACL concept. This will form what we call a compound realm that if a permission is checked looks for the permission in the roles and permissions of the user and in the ACL of the data object. If either the roles and permissions or the ACL grants the permission the user is allowed access.

# Ownership

It is common place in cloud applications where multiple groups of users that each belong to some kind of organization work in one system to summarize these groups of users as tenants. The tenants represent the organizations and---if a hierarchy is allowed---the sub-organizations working in the system. In the Sailing Analytics the organizations could be SAP in general (e.g. archive server), sailing clubs, events like the Travemünder Woche or in the future private users.

One idea behind tenants is to encapsulate organizations so users of one organization cannot work with data objects from another organization if they are not granted the permissions explicitly. Furthermore, tenants are used to group data objects so users can have access to all data objects of a tenant and do not have to be granted every permission explicitly. Additional to data objects, tenants are also associated with roles. Thus, granting a role associated with a tenant is equivalent to granting the role for each data object which is associated with the tenant. 

In the Sailing Analytics the set of roles for each tenant may, apart from certain exceptions, represent the subjects a user can adopt. A constraint could be introduced that only allows the roles for one tenant as a subject.

Tenants pose an UI problem, because it has to be clear to the user in which tenant he is currently working. The user has to be member of the tenant he is working for. Currently the best idea is to let the user select a tenant when he logs in and have default tenants for event and club servers that correspond to the event or club. Every following action is performed as that tenant.

In some cases, it might be necessary to transfer the ownership to another tenant/user. Thus, the owner should not be final but changeable.

## Users or Tenants as Owners

A problem with the tenant approach is that users could have no permissions to e.g. remove data objects that they have just created on accident, because the remove permission is reserved to admins of the tenant which a user that has create permissions may not be.

Four solutions come to mind. (1) The creator of a data object could always be granted the permission to remove the data object explicitly. (2) Moreover, the log where the creation was logged could be crawled to find the user that created the data object and override the permission system when in a certain timespan. 

(3) There is an alternative approach to ownership. In this approach, a single user would own a data object so he can do everything with it. The tenant would then be a kind of secondary owner or group in Linux terms. This solves the problem that users could have no permissions to e.g. remove data objects that they have just created on accident. However, it also introduces a second layer of ownership. (4) Only grant the create permission when the user also has the corresponding delete permission.

Approaches (2) and (4) are impractical. (2) is too complicated. The user would need delete permission for everything in the tenant for (4) to work.

Approach (1) solves the problem on hand, however these explicitly granted remove permissions are not just removed when the ownership of the data object changes, but remain. This could lead to users being able to delete data objects in other tenants, just because they created the object. Approach (3) more explicitly creates an ownership relation that can be edited and is thus chosen.

As the tenant is a data object itself, it also has an owner. The owning tenant of a tenant is the tenant itself.

## Subtenants

Subtenants could be a convenient way to restrict the permissions of certain users to only a part of a tenant’s domain. However, this introduces a hierarchy of tenants that brings with it its own challenges. Imagine there is a tenant “tw2017” and the 49er boat class races should not be manageable by the same race managers that can manage races of the other regattas. So “tw2017” would require a subtenant “other” and “49er” that encapsulate the 49er boat class and everything else from each other. Now if a permission is checked on an ACL, the ACL has to traverse the tenant hierarchy to find out if the user is part of a role for a parent tenant that grants the permission. However, the convenience of tenant hierarchies might be stronger than the traversal problem, because the hierarchy will probably never be deeper than one or two levels.

Another challenge with subtenants is how to communicate the concept to users. Which also makes it harder to imply with which tenant or subtenant a user is currently working.

An alternative strategy is just creating a completely new top level tenant for the 49er boat class races of “tw2017”. This would not introduce a hierarchy, but would require users that have roles for all boat classes to have their roles for both tenants instead of only the role for the parent tenant.

## Administration of Authorization

This section will discuss how it is determined if a user can grant or revoke a permission. Therefore, we define two rules:

* We define authority as power which has been legitimately obtained (Krishnan & Zimmer, 1991)
* We regard ownership as the starting point for delegation of authority (Krishnan & Zimmer, 1991)

With these two facts in mind, data objects must have a single user as the owner. However, as discussed earlier tenants are a second tier of ownership. Not every user that can create data objects in a tenant should automatically be the owner of every data object of the tenant, but in return should also not lose rights e.g. for removing an accidentally created data object on creation, because the tenant is the owner. Thus, in our approach the creator of a data object will be the owner and the tenant he is currently logged in to will be the owning tenant.

Another challenge after having a concept for ownership is the delegation of power. Not every user should be allowed to delegate his permissions to other users, thus there has to be a “grantPermission” permission that allows a user to delegate all his permissions to other users.

## Implementation of Ownership

Ownership is modeled as an explicit association between exactly one user and data objects and exactly one tenant and data objects. Owning a data object implies having all permissions on that object, as ownership is the regarded as the source of authority. In order to change ownership one has to be either owning user or tenant owner. If the user only changes the tenant owner he may remain owning user. If the user only changes the owning user he may remain owning tenant.

## Implementation of sharing data objects with public

In general, sharing a data object with the public should just be granting the “view” permission to everyone. This in return allows everybody that knows the link where one can view the data object to view it.
This is separated from promoting this public data object (e.g. event) on the official SAP site. This would have to be a separate list which can be edited by e.g. media admins. This would however only link to the source, because otherwise all promoted material would have to be imported to the archive.

The ACL will also be checked for permission requests by not authenticated users. This will only apply to ACL entries that are valid for all users.
