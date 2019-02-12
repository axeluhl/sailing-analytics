[[_TOC_]]

## Introduction

The following document describes which parts of the security and domain model are being migrated when starting a server with pre-existing data for the first time with a permission vertical enabled version.
In addition, several concrete scenarios that need to get checked when doing migration testing for permission vertical are documented.

## Migration details

### Server group and server configuration

In the new permission system, the server is explicitly modeled as a type having an ownership and ACL.
Server specific permission checks can be qualified by the server name. To make this convenient, a group is created named by the server with the suffix "-server". This group is the initial owner of the server, which means, any server admin may configure local server configurations (e.g. making the server public).

In addition, the created server group is initially set up to grant the role "sailing_viewer" with forAll=true. This means, objects owned by the server group will be readable by everyone (including anonymous users). Speaking of event servers or the archive server, this is consistent to the previous behavior where all events where publicly visible. Servers meant to be only visible to a specific group may be configured differently after migration.

### Specific <all> user

A user named <all> is created during migration. This user has no account set to make it impossible to log into this user.
Instead this user is used for any permission check in addition to the real user. This is also the case for anonymous users.
Any permissions directly or indirectly associated with the <all> user will be usable by anyone.

### All users

All pre existing users will receive the following additions:

* A group named by the user with suffix "-tenant" is created.
* The user is a member of the newly created tenant group
* The user is the owner of itself and the associated group
* The user has the role "user" qualified by the user itself but without a group qualification

### User called "admin"

The user "admin" is meant to be the initial user created when starting a server for the first time.
This user is the one that needs to have all permissions and this user is not intended to be used as an event admin.

To guarantee that the "admin" user still has all permissions after migration, it is the only user whose "admin" role will be migrated without a qualification.

### Users having roles

There is specific handling of some old roles. The following old roles are being recreated as RoleDefinitions on permission vertical:

* admin
* user
* spectator
* mediaeditor
* moderator

Any user having one of these roles (excluding the "admin" case described above) will have the new version of this role qualified by the server's default group.
This means those users will still have the same permissions bust only for objects owned by the mentioned server group.

Any other role would be lost. This is most probably an unlikely case due to the fact that the old permission UI had not possibility to define new roles.

### Users having "admin" role

Any user having the role "admin" will be added as a member of the server's group. Members of a group can set this group as their default creation tenant for the server. On a public server it is intended that all event data is owned by the server's group to ensure that these domain objects are publicly readable. To help prevent typical misconfigurations any user logging in to the admin console on a public server who has not set the server group as default creation group on that server will see a warning. Any member of that group will be asked if this should be fixed automatically.

### New role sailing_viewer

Before permission vertical, most parts of the website were completely unsecured. With permission vertical, most parts of the UI are filtered by checking read permissions. By default, most of those contents will just not exist for users that do not have those permissions. The newly created role "sailing_viewer" grants those permissions that previously were not needed to view contents of the website. As mentioned above, this role is associated with the server group by default allowing all users to still see the contents of the website.

### Ownerships

Any security and domain object being loaded will receive an initial ownership. The inital owning group is the server's default group while no owning user is set in most cases (users own themselves and their tenant group). Making the server's default group own those objects will ensure that any admin on that server who is migrated as an admin qualified by the server group will still be able to manage those objects. While retaining the permissions for objects on that server, it is save to import several migrated user stores to a central one because those permissions do not include objects of other servers.

### Users having specific permissions

In general, permission of a user are not removed during migration. In most cases those permissions will be useless after migration because the former lower case permissions will not match the newer case sensitive/upper case permissions.

The only widely used permission is "data_mining" for which we have specific handling. Any user having this permission will have the equivalent perrmission "SERVER:DATA_MINING" with a qualification by the server's name. Again, this means that such a permission is save in case of an import of several user stores because the migrated permission does not affect other servers.

## Scenarios

The following scenarios need to be checked when doing migration tests.

### Test user setup

Test users for the following scenarios need to be created:

* "admin" (as it is initially created)
* A user not being the "admin" with role "admin"
* A user having role "moderator"
* A user having permission "data_mining"
* A user having role "mediaeditor"
* A user without any specific roles

### Verification

* In general, the migration rules for users documented above need to be checked
* Going to the AdminConsole with either user having role admin will ask to set the default creation group. Any other user will just see a warning.
* Any event/regatta/race previously (events having public flag now called "list on homepage") must still be visible for anonymous as well as any logged in user after migration
* When an admin user (who has corectly set his default creation group) tracks new races, these must also be publicly visible
* The user having role "moderator" must still be able to replay during live races. Others must not.
* The user having "data_mining" permission must still be able to use DataMining on the server. Others must not be able to access the DataMining UI.
* In RaceBoard, the user having role "mediaeditor" needs to be able to manage media. Others must not.

TODO more