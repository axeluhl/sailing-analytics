[[_TOC_]]

## Introduction

The following document describes which parts of the security and domain model are being migrated when starting a server with pre-existing data for the first time with a permission vertical enabled version.
In addition, several scenarios that need to get checked when doing migration testing for permission vertical are documented.

## Migration details regarding the security model

### Server group and server configuration

In the new permission system, the server is explicitly modeled as a type having an ownership and ACL.
Server specific permission checks can be qualified by the server name. To make this convenient, a group is created named like the server with the suffix "-server". This group is the initial owner of the server, which means, any server admin may configure local server configurations (e.g. making the server public).

In addition, the created server group is initially set up to grant the role "sailing_viewer" with forAll=true. This means, objects owned by the server group will be readable by everyone (including anonymous users). For event servers or the archive server, this is consistent to the previous behavior where all events where publicly visible. Servers meant to be only visible to a specific group may be configured differently after migration.

There is another new semantic introduced called Self-Service. This is implemented by granting the "CREATE_OBJECT" action to the null group of the server object. This allows newly created users to create domain objects in their own owned space. This option is intended to be off by default.

### Specific <all> user

A user named <all> is created during migration. This user has no account making it impossible to log into this user.
Instead this user is used for any permission check in addition to the real user. This is also the case for anonymous users.
Any permissions directly or indirectly associated with the <all> user will be usable by anyone.

### Existing users

All pre existing users will receive the following additions:

* A group named by the user with suffix "-tenant" is created.
* The user is a member of the newly created tenant group
* The user is the owner of itself and the associated group
* The user has the role "user" qualified by the user itself but without a group qualification

### User called "admin"

The user "admin" is meant to be the initial user created when starting a server for the first time.
This user is the one that needs to have all permissions and this user is not intended to be used as an event admin.

To guarantee that the "admin" user still has all permissions after migration, it is the only user whose "admin" role will be migrated without a qualification.

### RoleDefinitions

There are several RoleDefinitions that are automatically created for new or migrated servers. These are:

* admin
* user
* spectator
* moderator
* mediaeditor
* sailing_viewer

These roles are system roles that are intended to be used by any user. To ensure this, an ACL entry is generated for those roles granting action "READ" to the null group (all users).

Most of those default RoleDefinitions also had role with the same name in the old role model (all but sailing_viewer). On existing servers, users having any old role, need to get an equivalent role associated. Any user having one of these old roles (excluding the "admin" case described below) will have the new version of this role qualified by the server's default group associated.
This means those users should still have equivalent permissions but only for objects owned by the mentioned server group.

Any other role would be lost. This is most probably an unlikely case due to the fact that the old permission UI had no possibility to define new roles.

### Tenants

Existing users (including the "admin" but excluding "<all>) will automatically get a UserGroup created named like the user with the suffix "-tenant". This means the admin's tenant is called "admin-tenant".

### Default creation tenant

We introduced a new concept called "default creation tenant" to the security model. Users now own an association of server names to group names. When a user creates a security or domain object, the owning group is set to the default creating tenant associated to the current server for that user. The fallback (if no explicit association exists for that server and user) is the user's tenant that is named like the user with the "-tenant" suffix.

The "admin" user is the only user that will get the server group set as default creation tenant for that server. For the admin user this is the right setting for most cases. Any other user will have it's tenant set as default creation tenant.

### Users having the old "admin" role

Any user having the old role "admin" will be added as a member of the server's group. Members of a group can set this group as their default creation tenant for the server. On a public server it is intended that all event data is owned by the server's group to ensure that these domain objects are publicly readable. To help prevent typical misconfigurations any user logging in to the admin console on a public server who has not set the server group as default creation group will see a warning. Any member of that group will be asked if this should be fixed automatically.

### Special role sailing_viewer

Before permission vertical, most parts of the website were completely unsecured. With permission vertical, most parts of the UI are filtered by checking read permissions. By default, most of those contents will just not exist for users that do not have those permissions. The newly created role "sailing_viewer" grants those permissions that previously were not needed to view contents of the website. As mentioned above, this role is associated with the server group by default allowing all users to still see the contents of the website.

### Ownerships

Any security and domain object being loaded will receive an initial ownership. The inital owning group is the server's default group while no owning user is set in most cases (users own themselves and their tenant group). Making the server's default group own those objects will ensure that any admin on that server will still be able to manage those objects. Due to the scoping of all migrated admin roles by the server, it is safe to import several migrated user stores to a central one because those permissions do not affect objects of other servers.

### ACLs

The only ACLs that are automatically created on migration or server initialization are those to make the initial RoleDefinitions publicly readable. No other ACLs are intended to exist for new or initially migrated servers.

### Users having specific permissions

In general, permission of a user are not removed during migration. In most cases those permissions will be useless after migration because the former lower case permissions will not match the newer case sensitive/upper case permissions.

The only widely used permission is "data_mining" for which we have specific handling. Any user having this permission will have the equivalent permission "SERVER:DATA_MINING" with a qualification by the server's name. Again, this means that such a permission is safe in case of an import of several user stores because the migrated permission does not affect other servers.

## Basic verification for initial and migrated servers

Any security enabled server needs to initially meet some criteria caused by the basic security setup. This needs to be consistent for both, newly created and migrated servers. Despite potentially different code paths of server initialization and migration, the results must be identical.

### Verification of the basic security model setup

The following criteria must be fulfilled for new or initially migrated servers after startup:

* Predefined RoleDefinitions need to exist as described. All initial RoleDefinitions need to have an ACL attached granting "READ" to all users.
* Any existing security or domain objects (matching types in SecuredSecurityTypes or SecuredDomainTypes) need to have an initial ownership associated.
* The server needs to be configured as "public" in the "Local server" tab but not as "Self-service". This means the server group needs to have the "sailing_viewer" role associated with forAll=true.

## Domain and security object migration

Some of the semantics described above will apply to all objects being part of a security type. Those semantics need to get checked not only for security related objects but also for domain objects.

### Verification of security effects to the basic domain 

Existing security and domain object migration is executed on every server restart. This ensures that no domain object is lost if the security information (e.g. ownerships) is invalidated e.g. by deleting a user that owns objects. In addition, the initial server setup ensures specific visibilities of objects that used to be publicly visible before. The following verifications need to be done to check this:

* During migration or on later server startups, any instance of a security type (excluding Users and UserGroups) that has no valid ownership set, will get a default ownership assigned which references the server group as owning group.
* Any Object being owned by the server group during migration will initially be readable by all users. This means in AdminConsole, such objects will be listed for any authenticated user. In addition, those objects are listed on the homepage and several other entry points may be shown to users (including anonymous ones). This is caused by the server to be set public on migration. In the sailing domain, this especially means, any event/regatta/race that previously existed (events having public flag now called "list on homepage") must still be visible for anonymous as well as any logged in user after migration. In addition, entry points like Leaderboard and RaceBoard need to be anonymously usable.
* When setting the public setting in "Local server" to false, those objects need to not be publicly readable anymore to normal users. This means, those objects aren't shown in AdminConsole and on Home anymore and opening those objects in Leaderboard or RaceBoard entry points won't show the contents.
* When an admin user (who has correctly set his default creation group) tracks new races, these must also be publicly visible
* On restarts of the server, any object without a valid ownership will get updated to be owned by the server group. It need to get verified that this works correctly for orphaned objects. To test this, create some objects with a newly created user. Those objects are by default owned by the user and its tenant. When deleting this user and tenant, the objects aren't owned anymore. After a server restart, those objects may be administered by any server admin.

## Scenarios for the security model migration

To completely verify the impact of security model migrations, some scenarios need to get set up on a server running a master version of the code. After a restart of the server with a version of the application having the new security model, some verifications can be done. The following scenarios need to be checked when doing migration tests for the security model.

### Test user setup

Test users for the following scenarios need to be created:

* "admin" (as it is initially created)
* A user not being the "admin" with role "admin"
* A user having role "moderator"
* A user having permission "data_mining"
* A user having role "mediaeditor"
* A user without any specific roles

### Verification of test user migration

In addition for the verifications for the basic security model (described above) the following verifications regarding the security model need to be done for migrated servers.

* In general, the migration rules for users documented above need to be checked
* Going to the user profile for the "admin" user, the default creation tenant needs to be set to the server group initially
* Going to the AdminConsole with either user having role admin (excluding the "admin" user) will get asked to set the default creation group. Any other user will just see a warning.
* The user having role "moderator" must still be able to replay during live races. Others must not.
* The user having "data_mining" permission must still be able to use DataMining on the server. Others must not be able to access the DataMining UI.
* In RaceBoard, the user having role "mediaeditor" needs to be able to manage media. Others must not.

## Usage based scenarios for the security model

### verification

TODO:
* one user creates an event model -> not visible by another user
* one user adds another user by name to his group and adds "sailing_viewer" to his group -> stuff needs to be visible to the other user, but not editable

## Changed semantics in the sse model

The semantics (requirements and behavior) of several parts of the sse model were changed based on the new security model.

### Master Data Import (MDI)

Due to the fact that servers and their data are now secured by default, the interface providing data on existing servers during MDI is also affected. To make it possible to import data from secured servers, credentials must now be given in the MDI import. Those credentials need to be valid on the target server and provide all the required READ permissions for the data intended to be imported.

### Replication

The replication is also affected by the new security model. A server may not register itself as a replica without further authorization. This means, credentials for the master server need to be provided on the replica. This user needs to have the SERVER:REPLICATE:<server-name> permission on the master server. The required credentials can be provided by system properties for auto replication as well as via UI for manual replication setup.

### Verification scenarios of changed sse semantics

TODO

## Changed semantics in the sailing domain model

Due to the new security model, there are semantic changes regarding several parts of the domain.

### Wind tracking from Igtimi

Wind tracking via Igtimi changed its semantic the respect account visibility for a user who tracks a race. When a user tracks a new race or starts tracking wind for existing races, only Igtimi accounts are included that the user has READ permissions for.

When races are automatically tracked during server restarting, the accounts are filtered by the user owner of the TrackedRace to simulate the tracking of the race as it would be done by the initial creator. If there is no user owner, the accounts are filtered by the readability of the race's group owner.

### DeviceConfiguration UUID

DeviceConfigurations for the race manager app were changed to now have an identifying UUID. As a fallback behavior, the name is also usable to login to such a device configuration. The REST API was extended to accept both values to associate a device to a configuration. Both app versions (an old build and one that includes the security model change) need to be able to connect to both server versions.

### Verification scenarios of changed sailing semantics

TODO

## Deployment scenarios

This chapter is intended to sum up the aspects to be tested along specific deployment scenarios. While the writing above focuses on relevant changes introduced by the security model or specific migration details, the following write up is meant as a checklist to do a system test within the borders of a distinct scenario.

### Migrate a public server with a big amount of historic event data (aka archive migration)

A server with a big amount of historic event data needs to get migrated to the new security model. After the initial loading is finished, the following verifications need to be done. This scenario is intended to be focused on the following aspects:

* migration of existing data
* consistent restore of data
* availability and publicity of migrated data
* basic visibility activation states of admins vs non admins
* integration of external systems
* possibility for deeper technical evaluations (e.g. memory consumption)

To verify that all data is migrated as intended, several checks need to get fulfilled as an admin user. The described checks should be done using the global "admin" user as well as a server admin. The results must be identical in both cases for the following criteria:

* The loaded TrackedRaces need to exactly match the old server
* All existing domain data is shown in tables and provides the full set of actions
* Identify regattas that are tracked using the various tracking technologies (TracTrac, Swiss Timing, Smartphone Tracking) and verify the correctness regarding the following aspects:
    * On the Homepage verify statistics, wind information, results, contents of the leaderboard
    * Show the stand-alone leaderboard and verify the contents
* External servers need to still be referenced
* Several technical parts of the AdminConsole need to be accessible and provide all available actions:
    * Local server
    * MDI
    * Remote servers
    * Replication
    * File storage

The following checks must be repeated with all kinds of users (admin, normal user, anonymous) to ensure the correct functionality regarding publicity of the server:

* In the global event list, check the statistics of each year to be sure, that all events are available with their respective contents
* Events originating from external servers need to still be visible in the event list
* For an event of each kind (single regatta, multi regatta, series), show the following views (if available for that type):
    * Event page
    * Regatta page
    * series page
    * stand-alone leaderboard
    * RegattaOverview
* Identify TrackedRaces that are tracked using the various tracking technologies and verify the following for those races:
    * The RaceBoard works and shows a valid leaderboard containing all competitors
    * In the RaceBoard, all expandable panels show valid data (competitor chart, wind, ...)
    * EmbeddedMapAndWindChart can be shown
* Identify a TrackedRace with attached media and verify that the media is correctly played

The following checks need to be done using a normal user:

* Technical tabs in the AdminConsole need to be unavailable (e.g. local server)
* Many publicly readable domain objects must not provide update actions in the respective AdminConsole tabs (events, leaderboards, ...)
* Other sensible data must be invisible (either absence of the tab or empty list) to normal users (e.g. Tracking connections, Igtimi accounts, ...)
* The server group may not be available in the default creation tenant selection of the user profile page
* The whole user profile page needs to be available and usable

### New event server setup

In addition of the migration test it is intended to do a test setup as shadow server for a real event. The focus for this scenario is defined as follows:

* Setup of a new server
* consistency of operational tasks
* Replication
* Definition of different user types
* Integration of live tracking technologies

Note for the tester: All users intended to do administrative tasks need to be added as members of the server group. All such users must set their default creation tenant to the server group to consistently to make all created objects consistently be owned by the server group. This is very important to make the publicity aspect of the server work as intended.

An event setup needs to be created including various types of domain objects (event, regatta, leaderboard, ...). For each of those objects, the following validations need to be fulfilled:

* all object consistently have the server group set as owner
* all server admin users as well as the global admin can mutate those object regardless which admin created those objects

The following operational scenarios need to be checked

* At least one replica needs to be created using admin credentials to connect to the master server
* One user created an Igtimi account in the server group and another admin tracks a race. The wind needs to be available in this race.
* Users only having specific instance-based mutation permissions (e.g. LEADERBOARD:UPDATE:<name>) only see mutation action for that domain object but not others
* A user having LEADERBOARD:UPDATE:<name> and REGATTA:UPDATE:<name> permissions must be able to edit score corrections
* A moderator only has specific permissions in RaceBoard but none in AdminConsole

### Smartphone tracking and App interaction test

A test setup needs to be created to cover an end to end scenarios using our various apps. The following scenarios should be verified in this test:

* Different types of invitations and QR codes
* Integration of branch.io and redirects of the QRCodePlace
* Compatibility with the old and new tracking apps
* Non-authenticated access and tracking based on the secret
* App based race management

For the tests of this scenario, we need a fully featured setup consisting of the following domain objects:

* A public event hierarchy
* A private event hierarchy (not just listed==false, but also not publicly readable)
* A regatta having static competitor/boat assignments
* A regatta having dynamic competitor/boat assignments
* Pinged marks as well as tracked marks

The a smartphone tracking setup needs to get repeated using the following invitation and app combinations (some cases can be tested in parallel):

* Legacy invitations/QR codes with the old apps
* Legacy invitations/QR codes with Sail Insight 2.0
* Branch.io invitations for the old apps
* Branch.io invitations for the new apps

In addition to the tracking tests, this setup needs to get managed by the race manager app. Due to the fact that the DeviceConfiguration model changed during development of the permission model and this change also affected the app code, both versions need to be tested:

* The latest store release
* A build that includes the security model changes

### Public self-service setup

An additional deployment scenario is the self-service case that will be covered by my.sapsailing.com. This case is already well covered from the perspective of the app development but several backend-focused validations need to be ensured as well. The following scenarios need to be covered:

* Setup and management of open regattas from the App and AdminConsole
* Inviting others to track
* Migrate parts of my stuff to a new group for sharing
* Share my stuff with others

TODO more detail