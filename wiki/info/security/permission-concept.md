[[_TOC_]]

# Introduction

This document describes the permission concept developed for the SAP Sailing Analytics (in the following just Sailing Analytics). The following requirements were considered during its conception:

* Be expressive enough to support the complex associations of the Sailing Analytics
* Support multiple organizations (clubs, events and individuals) working in one system
* Communicate the permissions to the frontend (so only UI elements that support permitted actions are active)
* Be not overly complex and implementation intensive

## Users and User Groups

A client can try to access the solution anonymously, without authentication, or can sign up for a user account and authenticate accordingly before making requests. A user has to provide a nickname that is unique and can optionally provide and validate an e-mail address as well as further attributes such as the full name, a company affiliation and various other settings.

User groups can be created that users can be assigned to as members. A user can be member of any number of user groups. By default, when a new user account is created, a new user group is created, too, named after the user with the suffix ``-tenant`` appended. For example, for the user with nickname ``john`` a user group named ``john-tenant`` will be created. The user is made a member of that default user group automatically.

## Ownership

Objects can have a user as an owner, and a user group as an owning group. The approach mimics that of a Unix/Linux file system where each file system object has a "group owner" and a "user owner." As will be explained later, certain permissions may then implicitly be granted if the requesting user belongs to the group owning the object or is identical to the user owning the object.

The user groups may, e.g., represent the organizations working in the system. For example, in the Sailing Analytics the organizations could be SAP in general (e.g., owning all content in the "archive server"), sailing clubs, or events like the Travemünder Woche. The group ownership can then be used to define access rules based on this group ownership, either generally for all users, or specifically for users that are members of that group. An example of the latter would be a training group that wants to track their training sessions and make the tracks accessible only to members of that group. An example of the former would be an event with a dedicated event user group, making all event data public by providing permission to all users to read all content owned by the event group.

Ownerships are usually assigned to an object upon its creation. For this, the session user will be taken as the default owning user. The owning group is determined based on the user's preferences. Logged-in users can select the user group to use as the group owner for new objects the user created. The user must be a member of that group at the time the selection is made. This assignment is stored on a per-server basis. When a dedicated server is set up for an event it is good practice to create the objects with a specific event group as group owner on that server. Still, on other servers the user may want to create objects with other groups as owners which is why this setting is remembered per server.

It is generally possible to change the group and user ownership of an object. This constitutes an action for which the user needs to have the corresponding permission.

## Access Control Concepts

The two big concepts that play together in this permission concept are access control lists (ACLs) (also used e.g. in the Linux or Windows file system) and role-based access control (RBAC).

The concept of ACLs is based on the idea of assigning each data object that is access controlled an ACL. The ACL used here is a list of entries that grant or revoke permissions to a user group. If e.g. read access is requested for a data object, its ACL is checked if any of the groups the user is a member of has an entry granting the read permission and no entry denying that read permission.

RBAC is based on the idea of having roles that imply certain permissions and assigning roles to users. When an action is to be executed, such as reading an object, the read permission is checked, and if it is not granted or revoked by an ACL entry, the requesting user's roles are checked regarding implying this read permission. If so, the permission is granted. It is important to note that always single permissions are checked and not whether or not the user has a certain role assigned.

## Initial Idea

The inital idea for this document is based on the existing Shiro RBAC system that handles roles that can imply permissions and that also supports directly granting permissions to a user. Furthermore, ACLs should be introduced. Those should solve the problem of â€œlosingâ€� implied permissions, e.g., upon ownership changes, because the access control lists are directly associated with the data object and do not depend on ownerships, whereas role-implied permissions may well be implied only if ownerships support this. Furthermore, an ACL can be used to explicitly revoke a permission that may otherwise have been implied for a user based on ownerships or the roles the user has assigned.

The existing Shiro authorizing realm that checks for the roles and permissions directly assigned to a user would have to be extended by the ACL concept. This will form what we call a "compound realm" that if a permission is checked looks for the permission in the roles and permissions of the user *and* in the ACL of the data object. If either the roles and permissions or the ACL grants the permission and the ACL does not revoke the permission then the user is allowed access.

## Roles

Roles have a *definition* and an *instantiation*. The role's definition is an entity that has a UUID, defines a (changeable) name and specifies the (changeable) set of permissions a user will obtain by being assigned a role instantiated from this definition. A role instantiation references a role definition and "inherits" the definition's name. The instantiation can furthermore optionally constrain the role by a tenant and/or user qualifier. In this case the permissions the role definition specifies will be granted to a user in that role only if requested for an object whose tenant/user owner matches that of the tenant/user qualifier provided by the role instantiation, respectively.

Roles with a tenant qualifier are displayed in the form "\<rolename\>:\<tenantname\>". Roles with tenant and user qualifier are displayed as "\<rolename\>:\<tenantname\>:\<username\>", and role instantiations with only a user qualifier are shown as "\<rolename\>::\<username\>". Examples:

1. Tenant Owner "owner:tw2018" (Can delete the tenant, in addition to everything the tenant admin can do)
2. Tenant Admin "admin:kw2018" (Has (almost) every permission in his tenant)
3. Eventmanager "eventmanager:VSaW"
4. Racemanager "racemanager:KYC"
5. Editor "editor:BYC"
6. Resultservice "resultservice:swc2018-miami"
7. User "user::johndoe" (A role that every user should have for himself/herself; grants permissions to modify the respective user object properties such as company affiliation, password and full name

With this it is possible, for example, to have an ``admin`` role definition with permission "\*". An instantiation of this role can then optionally restrict the tenant that must be an object's tenant owner for the role's permissions to be applied to permission checks for that object. If a user has role ``admin:server-A`` and requests permission for an object, the "\*" permission from the ``admin`` role definition is granted if and only if the object's tenant owner is ``server-A``.

Similarly, if a role instantiation provides a user qualifier, the object for which a permission is checked must be owned by the user specified by the qualifier in order for the role's permissions to be granted to a user with this role.

If both, a user and tenant parameter are declared for a role instantiation then both have to match in order for the role's permissions to be implied.

## Permissions

When the permission to perform an action is checked, the action and the object must be identified unless the action is an object *creation*. The possible actions are described by a pair of strings where the first element of the pair tells the "type" of the object to which the action applied, and the second element identifies the action to be performed on the object of that type. For example, ``leaderboard:update`` could be such an action for which permission needs to be requested when changing an object of type ``Leaderboard``.

To request the permission, the object identifier needs to be given... TODO when / backend / frontend / how...

## Defaults upon First Server Startup

When a system starts up for the first time, a set of default roles with default permissions and at least one default user need to be created.

### Role "admin"

This role implies the "*" permission. It should ideally be used with a tenant qualification.

### Role "user"

This role is intended to be used to describe the permissions that object owners shall be granted. In particular, when qualified
for objects owned by the user being assigned the role, that user obtains the permission to execute actions
``CHANGE_ACL,CHANGE_OWNERSHIP,CREATE,DELETE,READ,READ_PUBLIC,UPDATE`` for objects of any type. By default, users are also assigned
this role constrained for objects whose group owner is the user's dedicated group (``{username}-tenant``).

### Role "sailing_viewer"

This role can be used to publish the data of a sailing event. When assigning this to a user, the user obtains all
permissions required to ``READ`` the content of the sailing event, including leaderboards, regatta details, tracking, etc.
In order to constrain access to, say, a dedicated event, that event including its object hierarchy with leaderboards,
leaderboard groups, regattas, tracked races and so on can be transferred to a dedicated group owner. The ``sailing_viewer``
role can then be assigned, qualified for that group, to one or more users, or it can be assigned to the group owning
the event hierarchy now and can then be restricted to the members of that group or can be opened up to all users.

### User "admin"

On fresh instances that do not use a shared UserStore, the user "admin" is automatically created if no users exist yet on this instance. This user has the unqualified "admin" permission associated, which means this user has the permission to do everything. On event servers, this user is typically used to give permissions to specific event admin user.

### User "<all>"

When a user has not yet been authenticated, certain actions still need to be allowed for such users, in particular viewing public events or creating a new user during a sign-up activity. One approach may be to simply not request permissions for such actions. This way, all sessions would be able to perform such actions. The downside: in no server configuration would it be possible to limit access to those actions. For example, if one wanted to set up a private server instance where user sign-up is to be allowed only for administrators of that server then this could not be solved by role and permission configuration.

To allow specific permissions to get associated on specific servers, a user named "<all>" has been introduced. Any permissions/roles associated with this user are evaluated for any user (including non-authenticated ones).

## Administration of Authorization

This section will discuss how it is determined if a user can grant or revoke a permission. Therefore, we define two rules:

* We define authority as power which has been legitimately obtained (Krishnan & Zimmer, 1991)
* We regard ownership as the starting point for delegation of authority (Krishnan & Zimmer, 1991)

With these two facts in mind, data objects must have a single user as the owner. User ownership can changed over time. Additionally, as discussed earlier, tenants are a second tier of ownership. Not every user that can create data objects in a tenant should automatically be the owner of every data object of the tenant, but in return should also not lose rights e.g. for removing an accidentally created data object on creation, because the tenant is the owner. Thus, in our approach the creator of a data object will be the owner and the tenant he is currently logged in to will be the owning tenant.

Another challenge after having a concept for ownership is the delegation of power. Not every user should be allowed to delegate his permissions to other users, thus there has to be a â€œgrantPermissionâ€� permission that allows a user to delegate all his permissions to other users.

### Implementation of Ownership

Ownership is modeled as an explicit association between exactly one user and data objects and exactly one tenant and data objects. Defaults may exist; a server may specify a default tenant owner for all its objects. User ownership for objects that were created before ownerships were introduced may default to ``null`` as their owner.

Owning a data object implies having all permissions on that object, as ownership is regarded as the source of authority. Technically, this means that a logged-on user is granted all permissions on all objects for which he/she is the owning user.

Tenant ownership has implications only for the application of roles that are qualified for a specific tenant. When a user logs on, the tenant to use as tenant owner for objects that the user creates in this session has to be specified. The user account may remember the tenant that was used last as the default. If not set, the server's default tenant that is used as the default tenant owner for all server objects without explicit ownership information may be used instead.

### Implementation of Sharing Data Objects with Public

To make sharing as convenient as possible it was decided to use groups and their owned objects as basic unit of sharing. This means all domain objects of specific types owned by a common group (e.g. an event specific group) are visible to all users having a specific "viewer" role for that group.

A domain specific "viewer" role needs to be defined, having the required "READ" permissions for all object types that are open to a broader audience (e.g. events, but no advanced data). For Sailing Analytics, this role is named "sailing_viewer". The definition of a generic "viewer" role (e.g. having the permission "*:READ") is impracticabledue to the fact that this also allows read access to internal configuration objects and other administrative data. This means the associated permissions need to be explicitly enumerated.

To share the domain objects being owned by a group, you need to to associate the specific "viewer" role to a user with a qualification by the owning group. To make an event public, you can instead give the qualified "viewer" role to the "<all>" user.

To make the role work as intended, the domain objects of an event to be shared need to be consistently owned by a specific group. To make this as convenient as possible and to be able to fix wrong ownerships, batch-changing the group owner of related domain objects needs to be able. For Sailing Analytics, this means batch-changing needs to be possible under the following conditions:
* Batch-changing the ownership of a LeaderboardGroup in general includes Leaderboards/Regattas and their associated TrackedRaces.
* For event series defined by a LeaderboardGroup having an overall leaderboard, a batch change will also catch all events associated to the series.
* Batch-changing the ownership of an event means, all LeaderboardGroups not having an overall leaderboard are included with their included Leaderboards/Regattas and associated TrackedRaces.
* Such a batch change in general does not include Competitors/Boats that are used by a regatta. This is explicitly intended because Competitors/Boats are typically used on different event/regatta contexts that are not directly related. In some cases this could in fact be useful which means a user needs to have the choice of optionally include Competitors/Boats.

In addition we need to detect object associations with inconsistent group ownerships. In most cases domain objects getting associated in one of the hierarchies mentioned above, also need to inherit the group ownership if the associated "higher level" object is the first. For Sailing analytics, this means a leaderboard being added to a leaderboard group will inherit the leaderboard group's group owner if the leaderboard is not yet associated to another leaderboard group (assumed the current user is permitted to change the leaderboard's ownership). This ensures that e.g. leaderboards being created in the personal group of a user will get "fixed" when adding them to an event-context. The user needs to be informed when auto-adjusting group ownerships on association as well as cases where such an auto update is not performed. It needs to be discussed how the REST APIs can do this properly.

An additional requirement is the possibility to extract an event/series from a bigger sharing context (e.g. club group). This means a new group needs to be created and the domain objects's group owner need to be changed to this new group by following the structure explained above. In addition, all role associations that exist for the source group need to be copied as equivalent role associations for the newly created group to ensure that all users having specific permissions for the source group will keep their respective permissions. ACLs do not need to be adjusted due to the fact that those are directly associated to the respective objects that will keep their object IDs.

The downside of a role-based approach for sharing is that sharing domain objects requires every single user to have the specific role associated for the owning group. It is currently not possible to give roles to all users of a group. In contrast to this, sharing domain object with the public is easy.

In addition of sharing domain objects of a whole event, there is also the need to share single races. This can easily solved by using ACLs on the respective TrackedRace instance. Be aware that this approach is not conveniently usable for a hierarchy of domain objects due to the fact that this would require ACL rules to be implicitly batch-updated for all objects of a hierarchy. In addition, a consistent view of ACLs  in a hierarchy can not easily created in contrast of just finding differences in the group ownerships of domain objects.

## Permissions in Frontend

Currently the permissions of the roles are hard coded and can thus be easily imported in the frontend. Dynamic roles that can change at runtime would require passing the permissions implied by the roles to the frontend.

1. One option would be to resolve all permissions of a user before passing the set of permissions into the frontend. In a distributed system with multiple servers where a user could have permissions this is no viable solution.
2. ACLs could be delivered with the object itself. A permission on an object can then be checked in the frontend by asking the ACL delivered with the object. This would require adding a call to the permission system to every remote procedure call that returns an object so as to annotate the object returned with the ACL obtained.
3. A third but possibly resource hungry possibility would be to implement a service that can be called from the frontend to check single permissions. The service would implement some kind of hasPermission(permission) method. This could then be used from the frontend as well as the server code.

As ACLs will probably remain small in general, we will implement the (2) second approach. Furthermore, the ACLs that are returned by the server will be reduced to the entries that are relevant for the current user.

## Permission Defaults

This section will discuss how to handle default permissions. Default permissions for data objects should not be implied by the context they are created in. Implying permissions could lead to unwanted permissions on data objects. There are two options for default permissions:

1. Each type of object has its own mask of permissions it gets assigned. This has the advantage over pure roles that if one wants to change the norm only for one instance of an object one can. The masks could even be editable for each tenant so different defaults can be set. However, this also requires that the default ACL contents are conveyed to the user for checking and display.
2. The creator can choose on creation which other permissions to grant.
3. Have default permissions in roles so they do not have to be entered into each ACL, but have negative permissions to revoke defaults.

The default permissions will be handled by approach 3. to keep the ACLs short and less redundant.

## Constraints

A problem that is not easily solved with either ACLs or RBAC is constraining accesses in a more complex way than checking for a permission. A use case that may be important in the future when clubs can use the Sailing Analytics on their own is as follows.

Clubs may only be able to create races with e.g. < 60 boats, so club events cannot exceed the infrastructure provided to them. How could this be implemented with permission checking?

A "create_big_race" permission could be hardcoded that is checked when a user tries to add more than 60 competitors to a race.

There are even more expressive access control systems than RBAC. They are called constraint based access control systems. They allow constraints to be expressed in a less black and white way, however are very complex. This concept is not supported by the permission concept proposed here, because use cases like the above are probably edge cases that will be hard coded.

## Use Cases

In the following example use cases are listed that describe how user actions will impact the ACLs of the data objects the user interacts with. It is to note that this list of use cases is no complete list of all use cases for the permission handling system. Listing all of them is outside the scope of this document.

It is always assumed that the ID of the user is â€œuserâ€� and the ID of its tenant is â€œtenantâ€�.

1. Create Event (or any other data object)
  * User creates event
  * Event is owned by tenant that user is associated with in this session
  * Access control list is created for the event
  * Permissions as e.g. â€œviewâ€� are implicitly granted by the roles of the owning tenant
2. Transfer ownership of event (or any other ownership transfer) (Already described in section â€œOwnershipâ€�)
3. Link RegattaLeaderboard into LeaderboardGroup
  * If either the user, a role or a tenant the user is part of, has the permission to edit the LeaderboardGroup (LBG) and view the RegattaLeaderboard (RL), the user can link them. LBG ACL = {â€œuserâ€�:[â€œeditâ€�]} | RL ACL = {â€œuserâ€�:[â€œviewâ€�]}
  * If the user has the â€œgrantPermissionsâ€� permission, the â€œviewâ€� permission will automatically granted to all that can view the LeaderboardGroup.
4. Unlink TrackedRace
  * If either the user, a role or a tenant, the user is part of, has the permission to edit the Leaderboard, the user can unlink them.
5. Share TrackedRace
  * If either the user, a role or a tenant, the user is part of, has the permission to view the TrackedRace and the â€œgrantPermissionsâ€� permission, the user can grant view permissions to anybody. ACL = {â€œuserâ€�:[â€œview, grantPermissionsâ€�]}
  * The user shares the TrackedRace with â€œuser2â€�. ACL = {â€œuserâ€�:[â€œview, grantPermissionâ€�], â€œuser2â€�:[â€œviewâ€�]}
6. Create GPSFix
  * It would only be consistent to attach a ACL to each GPSFix, however it probably never happens that a GPSFix has other permissions than a whole track, thus I propose to leave GPSFixes without ACLs and only introduce access windows on the tracks that can have their own ACL.
7. Masterdata import
  * This should import all permissions as they are. A masterdata import itself is no reason to change permissions, however only data objects that the user that is importing has a â€œviewâ€� permission for should be importable. If the importing user is the owner or a tenant owner of the imported data, he can also change the ownership.
8. Share event with public
  * If either the user, a role or a tenant, the user is part of, has the permission to view the data object and the â€œgrantPermissionsâ€� permission, the user can share the event with the public. ACL = {â€œuserâ€�:[â€œview, grantPermissionsâ€�]}
  * The user shares the data object. ACL = {â€œuserâ€�:[â€œviewâ€�], â€œ*â€�:[â€œviewâ€�]}
9. Revoke permissions
  * Simply revoke: If either the user is the owner of or the user is a tenant owner/admin of the data object, the user can revoke every permission to the data object from anybody. 
  * Overwrite with negative: the same rules apply as for revoking, however a permissions in the following form is inserted into the ACL. {â€œuserâ€�:[â€œ!viewâ€�]}
10. First boot of server
  * On first boot of the server, an admin user is created. The creator of the server will log in as that admin user and in most cases create a new tenant. Thereafter, the creator will create at least one new users, assign the admin role to that user and delete the default admin user.

## Algorithm `boolean isPermitted(PrincipalCollection principals, WildcardPermission permission)` for Composite Realm

The above describes the data model that is relevant to the composite realm that implements the `isPermitted` function. The â€œpermissionâ€� parameter should be of the pattern â€œtype:action:instanceâ€�. It is assumed that the user (with associated permissions and roles), tenant, ownership associations and ACL entries are available. The following describes in which order the different sources for permissions are checked and how they depend on each other.

1. Check if the ACL entries grant or explicitly revoke the permission to the user under consideration of the userâ€™s roles
  * If there is an entry, return true if granted and false if revoked, but take the most explicit entry and in doubt return false
2. Check if the permission is directly assigned to the user
  * If this is true return true
3. Check if a role grants the permission to the user, which for instances of qualified roles requires the tenant/user parameter values to match the object's tenant / user.
  * If this is true return true.

## Migration
With such an extensive existing system as the Sailing Analytics Suite, migration is a big concern. The existing RBAC system is easily extended to support ACLs. However, implementing permission checking in the whole system will be a long process, because probably almost every service request will have to be edited.

Another challenge besides the code changes is the data migration. On the one hand, we don't want to have to create ACLs and ownerships explicitly for each and every object. We could work with defaults that seem reasonable, such as assuming that each server has a server name which is usually provided by the ``SERVER_NAME`` environment variable during server startup and is passed through to the ``com.sap.sailing.server.name`` system property. These names are expected to be unique across the landscape and could serve as the name for the default tenant / user group assumed to be the group owner for all objects in that server, and using ``null`` as the default user owner. This seems a backward-compatible approach because so far there is no user ownership at all, hence no permissions have been implied through such an ownership.

But there is a problem with this. When no explicit ownership information is maintained for an object with a given key, other servers could create an object with an equal key and *claim* ownership for that object. Worse even, should objects with equal keys exist in different servers at the same time which at one point start sharing the *SecurityService*, the two servers would both think they are the legitimate owner of the object.

This leads us to think that during server migration *all* objects for which ownership *can* be specified *need* to have their ownership specified explicitly. The default ownership to be claimed in the SecurityService shall be the same default as described above, namely the server's default user group as the group owner, and no user owner. During this process, conflicts for objects already owned by another server's default group can be recognized and flagged.

During the migration we should also consider establishing ACLs that grant public read access to those objects that so far were publicly readable. A domain-specific logic will need to be implemented that understands existing policies and procedures. In particular, public ``READ`` permission shall be granted to all Event, Regatta, Leaderboard, LeaderboardGroup and TrackedRace objects where the Event object to which they "belong" was flagged as ``isPublic``. Events not flagged as ``isPublic`` shall not be provided with such an ACL and hence be visible only for users that obtain ``READ`` permission through some role, explicit permission or another ACL.

Of course, ownerships can explicitly be defined for new objects or adjusted for existing ones. Adjusting ownership is considered a default action that should be available on most secured objects, and permission to change ownership can hence be granted or denied explicitly. Object owners should by default be granted the right to change the ownership information of the objects they own (like the *chown* operation in a Unix-like file system).

**Role Migration**

Following roles have to be migrated to dynamic roles. Beforehand they were hard coded in the SailingPermissionsForRoleProvider class. However, aside from the admin role, all roles listed here are only containing permissions that are checked in the frontend for showing/hiding tabs in the different menus.

* admin
  * "*"
* eventmanager
  * "manage_media"
  * "manage_mark_passings"
  * "manage_mark_positions"
  * "manage_all_competitors"
  * "manage_course_layout"
  * "manage_device_configuration"
  * "manage_events"
  * "manage_igtimi_accounts"
  * "manage_leaderboard_groups"
  * "manage_leaderboards"
  * "manage_leaderboard_results"
  * "manage_racelog_tracking"
  * "manage_regattas"
  * "manage_result_import_urls"
  * "manage_structure_import_urls"
  * "manage_tracked_races"
  * "manage_wind"
  * "event"
  * "regatta"
  * "leaderboard"
  * "leaderboard_group"
* mediaeditor
  * "manage_media"
* moderator
  * "can_replay_during_live_races"

During role migration again the server's default tenant derived from the server name comes into play. If a user had the ``admin`` role assigned on a server, this role must be qualified now to limit its scope to what the user was effectively granted permissions to, namely the objects in the current server. This can be achieved by qualifying the ``admin`` role with the default tenant derived from the server name and by making this tenant also the default tenant for all users in the server. With this, new objects will be created by all users with this default tenant as the tenant owner, so users with the migrated role ``admin:the-default-tenant`` will still have full administration privileges for all objects created on that server.

## Implementation Details

Access control relevant objects are stored in the AccessControlStore, while user related objects are stored in the user, i.e. the UserStore. Tenants are currently stored as UserGroups in the user store. The algorithm outlined above is implemented in the PermissionChecker, which is one of the most central classes in the permission system. Therefrom it should be possible to find all other relevant classes. Other relevant classes include the AbstractCompositeAuthorizingRealm that implements permission checking in Shiro. Therefor it uses the PermissionChecker and is connected to the UserStore and the AccessControlStore. Furthermore the RolePermissionModel implements how roles imply permissions. The RolePermissionModel as well as the parameterization of roles need more work and are just a rough sketch. The UserManagementService exposes most of the access control relevant parts to the frontend. It works through the SecurityService with the AccessControlStore and the UserStore. Permission checking in the frontend is also done with the PermissionChecker. To ease the creation of WildcardPermissions, a PermissionBuilder was introduced that also needs some more work. All access control relevant UI parts can be found in the "Advanced" tab under "User management" and "Tenant management". A further tab "Access Control Management" should be introduced. Unit tests can be found in the package com.sap.sse.security.test.



### Discussion

#### Users and/or Tenants as Owners

A problem with the tenant approach is that users could have no permissions to e.g. remove data objects that they have just created by accident, because the remove permission is reserved to admins of the tenant which a user who has create permissions may not be.

Four solutions come to mind. (1) The creator of a data object could always be granted the permission to remove the data object explicitly. (2) Moreover, the log where the creation was logged could be crawled to find the user that created the data object and override the permission system when in a certain timespan.

(3) There is an alternative approach to ownership. In this approach, a single user would own a data object so he can do everything with it. The tenant would then be a kind of secondary owner or group in Linux terms. This solves the problem that users could have no permissions to e.g. remove data objects that they have just created by accident. However, it also introduces a second layer of ownership. (4) Only grant the create permission when the user also has the corresponding delete permission.

Approaches (2) and (4) are impractical. (2) is too complicated. The user would need delete permission for everything in the tenant for (4) to work.

Approach (1) solves the problem on hand, however these explicitly granted remove permissions are not just removed when the ownership of the data object changes, but remain. This could lead to users being able to delete data objects in other tenants, just because they created the object. Approach (3) more explicitly creates an ownership relation that can be edited and is thus chosen.

As the tenant is a data object itself, it also has an owner. The owning tenant of a tenant is the tenant itself. (TODO: Should it be possible to change the tenant owner of a tenant? What is the best default tenant owner for a tenant? The tenant itself or the tenant to which the user creating the tenant is currently logged on?)

#### Subtenants

Subtenants could be a convenient way to restrict the permissions of certain users to only a part of a tenantâ€™s domain. However, this introduces a hierarchy of tenants that brings with it its own challenges. Imagine there is a tenant â€œtw2017â€� and the 49er boat class races should not be manageable by the same race managers that can manage races of the other regattas. So â€œtw2017â€� would require a subtenant â€œotherâ€� and â€œ49erâ€� that encapsulate the 49er boat class and everything else from each other. Now if a permission is checked on an ACL, the ACL has to traverse the tenant hierarchy to find out if the user is part of a role for a parent tenant that grants the permission. However, the convenience of tenant hierarchies might be stronger than the traversal problem, because the hierarchy will probably never be deeper than one or two levels.

Another challenge with subtenants is how to communicate the concept to users. Which also makes it harder to imply with which tenant or subtenant a user is currently working.

An alternative strategy is just creating a completely new top level tenant for the 49er boat class races of â€œtw2017â€�. This would not introduce a hierarchy, but would require users that have roles for all boat classes to have their roles for both tenants instead of only the role for the parent tenant.

For now, we will not consider the concept of subtenants further.

## TODOs

Dump of TODOs. More structured in Bugzilla.

* Ownership/Access control list as expandable column in all access controlled data object tables
* Permission checking for every domain data object type
* Changing ownership (incl. replication)
* Masterdataimport
* Tenant management UI
  * Tenant in create dialog check if tenant exists (red outline of text input field)
  * Refresh tenant selection in tenant list on the left correctly
  * String messages
* User management UI
  * Auto complete for roles and permissions
* Access control list / Roles / Ownership UI
* User role for editing own user parametrized by user name
* Do we want to throw unauthorized exceptions or how should this work? (See addUserToTenant or removeUserFromTenant) / UnauthorizedException handling (i.e. popup that shows some text)
* Create ownerships from config / Server-wide default owner and default tenant owner
* Create test setup for final test
* Button to transfer ownership of all objects from one user/tenant to another user/tenant (chgroup recursive?)
* Rework meta permissions (rework to also check all permissions that are granted or revoked?)
* Permission reloading without site refresh / Frontend update elements that require permission when permissions change (Refresh call on panels)
* Default tenant input field in login popup
