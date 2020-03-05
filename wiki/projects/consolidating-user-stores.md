# Consolidating User Stores

[[_TOC_]]

This document is an evolving set of ideas, requirements and strategies that will lead us to consolidating and joining our SecurityService instances with their embedded UserStore objects into a central shared one for most, and separate ones for a few server instances. The tasks and their history are described in ([bug 4006](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=4006) and [bug 4018](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=4018)).

## First Steps
An exported version of the ``USERS``, ``USER_GROUPS`` and ``PREFERENCES`` collections is at ``dbserver.internal.sapsailing.com:/var/lib/mongodb/dump``. The command to obtain this was:

```
	for port in 10201 10202 10203; do echo "rs.slaveOk()
	show dbs
	quit()" | mongo --port $port | tail -n +5| awk '{print $1;}' | grep -v ^config$ | grep -v ^local$ | grep -v ^admin$ | while read i; do echo $i; for c in USERS PREFERENCES USER_GROUPS; do mongoexport --port $port --db $i -c $c -o dump/${i}_${c}.json; done; done; done
```
This provides a basis for some initial cross-DB querying, figuring out which conflicts or clashes to expect and developing approaches for consolidation and conflict resolution.

## Active DBs vs. Inactive DBs vs. "Isolated" DBs

We have a lot of databases from past events and a few from servers that are actively running. Content from old, inactive servers is obviously less important to recover than content from servers still running. Especially, when conflicts between a record in an inactive and an active DB arises, the active DB should take precedence.

Records from inactive DBs may not have undergone migration to the ``permission-vertical`` code base, so particularly their roles and permissions as well as user group assignments may be obsolete. There are probably low expectations by those users that their records would ever re-appear or be recovered. Therefore, we may simply decide to ignore old, inactive DBs altogether.

Other DBs may belong to "isolated" servers from which we don't want content to spill over into a shared SecurityService's user store, such as the ``dev.sapsailing.com`` instance. For other more test-like servers such as ``d-labs.sapsailing.com`` or ``sailtracks.sapsailing.com`` we have to decide whether we'd like those to become part of the shared SecurityService landscape.

To figure out which databases are active and then classify them into those to be merged and those to remain isolated, we'll start looking at the current server landscape. Eight league servers of which two look at the same DB are still active at this point in time (2019-11-14T16:09:00Z) but will be archived until latest 2019-11-15T18:00:00Z. We therefore implicitly assume their DBs as "inactive."

There are a few dedicated server instances with their DBs:
* ARCHIVE, DB ``dbserver.internal.sapsailing.com:10201/winddb``
* AST (Australian Sailing Team), DB ``ast.sapsailing.com:27017/AST`` backed up to ``dbserver.internal.sapsailing.com:10202/AST``
* The DBs of all instances running on "SL Multi-Instance Kiel" (we should check if we could take this opportunity to shut down those that are still entirely empty): bartlomiejkapusta benjaminalhadef ditlevleth emilienpochet filip first-seascape-germany frederiksivertsen guentherpachschwoell gustavhultgren hinnerksiemsen janekbalster jassiskogman joanameyerduro jyrikuivalainen konradlipski lukasgrallert mandy marcusbaur martinmaursundrishovd oriolmahiques paulbakker peterwagner rolandregnemer tamarafischer thorbennowak thorechristiansen
* The DBs of all instances running on "SL Multi-Instance Sailing Server NVMe" (we should check if we could take this opportunity to shut down those that are still entirely empty): AARHUSSEJLKLUB abeam-training alarie505 ASVIA baldeneysee BYC d-labs DUTCH_FEDERATION ess-team-training formula18 galvestaure HNV hsc-womensteam irishdbscdw3 ISR JOJOPOLGAR jonaswitt KBSC KJSCS KSSS kyc LYC mdf my NOR oakcliff phoenix PMYC RDSAILING rheinwoche2019 SAILCANADA SAILINGACADEMY sailracer Sailtracks schadewaldt schanzenberg schwielochsee seascape SEGELZENTRUM SINGAPORESAILING SITGES SRN SRV SSC SSV STARLAB TracTracTest ubilabs-test USSAILING VSAW YCL

bartlomiejkapusta is empty. Removing. benjaminalhadef is largely empty. Removing. mandy is basically empty. Removing. filip is empty. Removing. thorbennowak empty. Removing. lukasgrallert basically empty. Removing. konradlipski only imported old stuff. Removing. gustavhultgren was not even started and had only one old test event. Removing. oriolmahiques has only empty events. Removing. thorechristiansen empty other than Bundesliga imports. Removing. tamarafischer basically empty. Removing. emilienpochet contains only empty events. Removing. paulbakker empty, removing. guentherpachschwoell only has league imports. Removing. rolandregnemer empty; removing. ditlevleth empty; removing. frederiksivertsen empty; removing. jassiskogman empty; removing. jyrikuivalainen empty; removing. martinmaursundrishovd empty; removing. The janekbalster and peterwagner DBs have remained, and their server instances have now been moved to the common multi-instance server. The "SL Multi-Instance Kiel" server has been terminated by now. YCL is completely empty. Removed.

On the "SL Multi-Instance Sailing Server NVMe" server some cleanup is possible, too. I'll remove the ``ubilabs-test`` instance and DB. In particular, "49er" is empty and dead. I'll remove that now. After checking back with Rasmus Koestner, I also got permission to dismantle the extreme-team.sapsailing.com instance with its ess-team-training DB.

So the list of remaining active, non-isolated server database names other than ``winddb`` for the archive server is:
* AARHUSSEJLKLUB
* abeam-training
* alarie505
* AST
* ASVIA
* baldeneysee
* BYC
* DUTCH_FEDERATION
* first-seascape-germany
* formula18
* galvestaure
* HNV
* hsc-womensteam
* irishdbscdw3
* ISR
* janekbalster
* JOJOPOLGAR
* jonaswitt
* KBSC
* KJSCS
* KSSS
* kyc
* LYC
* mdf
* my
* NOR
* oakcliff
* peterwagner
* phoenix
* PMYC
* RDSAILING
* rheinwoche2019
* SAILCANADA
* SAILINGACADEMY
* sailracer
* schadewaldt
* schanzenberg
* schwielochsee
* seascape
* SEGELZENTRUM
* SINGAPORESAILING
* SITGES
* SRN
* SRV
* SSC
* SSV
* STARLAB
* TracTracTest
* USSAILING
* VSAW

Isolated but active databases that will explicitly not be merged are:
* Sailtracks
* d-labs
* dev

## Data to Consider

### ``USERS`` Collection

Users are identified by their name which in the ``USERS`` collection is represented by the ``NAME`` field. Further attributes are the ``EMAIL``, ``FULLNAME``, ``COMPANY``, ``LOCALE``, ``EMAIL_VALIDATED``, ``PASSWORD_RESET_SECRET`` (in case a reset transaction is currently ongoing), ``VALICATION_SECRET``, then under ``ACCOUNTS`` the username/salted-password content.

Regarding security, the ``USERS`` collection for each user holds the ``ROLE_IDS`` and the ``PERMISSIONS`` arrays, furthermore the ``DEFAULT_TENANT_IDS`` map whose keys are the ``DEFAULT_TENANT_SERVER`` name and the value providing the ID of the default object creation group for the user on the server identified by the key.

The roles referenced by the ``ROLE_IDS`` array have a full configuration in the ``ROLES`` table. There are a number of default roles, such as ``user``, ``sailing_viewer`` and ``admin`` for which we want to assume that they have not been altered from their defaults.

Most DBs have an entry for the &lt;all&gt; user whose key function consists in granting permission to create a new user to everybody, including not logged-on users.

#### Existing Data

Special role assignments are those that do not only assign the ``user`` role that any user has:

```
for i in *_USERS.json; do output=$( cat $i | jq -r 'select(.ROLE_IDS != null) | select([.ROLE_IDS[] | select(.NAME != "user")] != []) | ("- ".NAME, (.ROLE_IDS[] | select(.NAME != "user") | .NAME":.QUALIFYING_TENANT_NAME ); if [ \! -z "$output" ]; then echo "${i}:"; echo "$output"; fi; done
```
shows several ``admin`` role assignments and a handful of ``moderator`` assignments. The ``moderator`` assignments are all on servers no longer active or just now under master data import (bundesliga2019.sapsailing.com). Suggestion: ignore the ``moderator`` assignments in case they cause trouble.

A number of accounts have an unqualified ``admin::`` role assignment. These belong to admins of event servers that have either already been archived or are in the process of being archived. The fact that quite numerous users have been equipped with this global privilege shows two things:
* users seem to be missing something when their ``admin`` role is qualified with the server group (what is this?)
* in the future we must avoid granting this super-power to any but the most trusted "root" users

The ``PERMISSIONS`` field has entries for a significant number of users, mostly ``SERVER:DATA_MINING`` and ``LEADERBOARD:UPDATE`` entries where the latter seem to have been established mostly for user accounts used on Race Manager App devices. Some inactive instances have permissions set for ``MEDIA_TRACK:CREATE``. On the archive server, two users have explicit ``SERVER:CAN_EXPORT_MASTERDATA:ARCHIVE`` permission.

The query

```
for i in *_USERS.json; do output=$( cat $i | jq -r 'select(.NAME != "<all>" and .PERMISSIONS != [] and .PERMISSIONS != null) | ( .NAME+":", "  "+(.PERMISSIONS[] | select(match("DATA_MINING"))) )'); if [ \! -z "$output" ]; then echo " ====== $i ====="; echo "$output"; fi; done
```
reveals that there are a number of unqualified ``DATA_MINING`` permissions, one on bundesliga1-2019 which is just about to be archived, one on a recently archived server hwcs2020. And then there is a user "Chelsie2112" with unqualified ``SERVER:DATA_MINING`` access on the archive server. This is being changed now to a qualified ``SERVER:DATA_MINING:ARCHIVE``.

1856 user records exist in more than one database.

### ``USER_GROUPS`` Collection

For all servers there is a ``-server`` group, furthermore for each user that user's personal group, named according the user's name, with ``-tenant`` appended. The ``USERNAMES`` array contains the user name keys identifying the users that are part of this group.

The ``ROLE_DEFINITION_MAP`` array has entries referencing a role definition by UUID in the field ``ROLE_DEFINITION_MAP_ROLE_ID`` and defining whether or not the permissions implied by that role shall be granted for access to objects owned by the group only to users belonging to the group or to all users (field ``ROLE_DEFINITION_MAP_FOR_ALL``).

#### Existing Data

For all user groups (active and inactive server DBs) there is only one group that has a role assigned that is not the ``sailing_viewer`` role:

```
for i in *_USER_GROUPS.json; do output=$( cat $i | jq -r 'select(.NAME | (test("-tenant$") or test("-server$")) | not) | select((.ROLE_DEFINITION_MAP[] | select((.ROLE_DEFINITION_MAP_ROLE_ID | .["$binary"]) != "y0V7Ud9IKcQV4Rif59Gpnw==") | length) >= 1) | ("  "+.NAME, (.ROLE_DEFINITION_MAP[] | select((.ROLE_DEFINITION_MAP_ROLE_ID | .["$binary"]) != "y0V7Ud9IKcQV4Rif59Gpnw==")) )' ); if [ \! -z "$output" ]; then echo " ===== ${i} ====="; echo "$output"; fi; done
```
yields

```
 ===== d-labs_USER_GROUPS.json =====
  StefansTestEventGroup
{
  "ROLE_DEFINITION_MAP_ROLE_ID": {
    "$binary": "ZEQ9sUhRHa0uTk1uOXzEkA==",
    "$type": "03"
  },
  "ROLE_DEFINITION_MAP_FOR_ALL": false
}
```
In all other cases, groups either have no role assignment or assign the ``sailing_viewer`` role. Of those, only two assign the role only to the users of their group:

```
for i in *_USER_GROUPS.json; do output=$( cat $i | jq -r 'select(.NAME | (test("-tenant$") or test("-server$")) | no) | select(.ROLE_DEFINITION_MAP[] | select((.ROLE_DEFINITION_MAP_ROLE_ID | .["$binary"]) == "y0V7Ud9IKcQV4Rif59Gpnw==" and .ROLE_DEFINITION_MAP_FOR_ALL==false)) | ("  "+.NAME, .ROLE_DEFINITION_MAP[] )' ); if [ \! -z "$output" ]; then echo " ===== ${i} ====="; echo "$output"; fi; done
```
yields

```
 ===== dev_USER_GROUPS.json =====
  Secret-test-group
{
  "ROLE_DEFINITION_MAP_ROLE_ID": {
    "$binary": "y0V7Ud9IKcQV4Rif59Gpnw==",
    "$type": "03"
  },
  "ROLE_DEFINITION_MAP_FOR_ALL": false
}
 ===== d-labs_USER_GROUPS.json =====
  AxelsSecretTestGroup
{
  "ROLE_DEFINITION_MAP_ROLE_ID": {
    "$binary": "y0V7Ud9IKcQV4Rif59Gpnw==",
    "$type": "03"
  },
  "ROLE_DEFINITION_MAP_FOR_ALL": false
}
```
and both are test groups in the isolated servers "dev" and "d-labs" so would not need further consideration in case of problems with this constellation.

### ``PREFERENCES`` Collection

This collection can contain string-based key/value pairs for users identified by their user name which is given in the ``USERNAME`` attribute. The ``KEYS_AND_VALUES`` array has objects with a ``KEY`` and a ``VALUE`` attribute each. One typical key is the ``___access_token___`` key where the value contains the API access token that, when sent with a REST request's ``Authorization`` header field, is good for authenticating the corresponding user.

Other than the access token, the ``KEYS_AND_VALUES`` array is used largely to store user settings from the various components, with or without a "document" qualification, such as with keys ``sailing.ui.usersettings.SeriesOverallLeaderboard#Allsvenskan Segling 2019 Overall`` or ``sailing.ui.usersettings.Raceboard.WINNING_LANES#WCS 2019 Marseille - RS:X Men,R9 (RS:X Men),WCS 2019 Marseille - RS:X Men``.

#### Existing Data

There are currently 2998 distinct keys used. An interesting one could have been ``sailing.datamining.storedqueries`` but it turns out that up to now there are only eight occurrences of this key across the landscape, so the feature of storing data mining queries does not seem to be in "overly active use" and no dedicated merging / migration strategy seems required.

Overall we have 24623 keys in all ``PREFERENCES`` collections out of which 872 use the ``___access_token___`` key.

### Scripts for Data Extraction

Exporting the collections into JSON files in a ``dump/`` sub-folder of the current working directory:

```
	for port in 10201 10202 10203; do echo "rs.slaveOk()
	show dbs
	quit()" | mongo --host dbserver.internal.sapsailing.com --port $port | tail -n +5| awk '{print $1;}' | grep -v ^config$ | grep -v ^local$ | grep -v ^admin$ | while read i; do echo $i; for c in USERS PREFERENCES USER_GROUPS OWNERSHIPS ACCESS_CONTROL_LISTS; do mongoexport --host dbserver.internal.sapsailing.com --port $port --db $i -c $c -o dump/${i}_${c}.json; done; done; done
```

Obtaining users with non-default ("user") role assignments:

```
for i in *_USERS.json; do output=$( cat $i | jq -r 'select(.ROLE_IDS != null) | select([.ROLE_IDS[] | select(.NAME != "user")] != []) | ("- "+.NAME, (.ROLE_IDS[] | select(.NAME != "user") | .NAME+":"+.QUALIFYING_TENANT_NAME+":"+.QUALIFYING_USERNAME ))'); if [ \! -z "$output" ]; then echo " ===== ${i} ====="; echo "$output"; fi; done
```

Obtaining the users that are not the &lt;all&gt; user and have non-empty permissions:

```
for i in *_USERS.json; do output=$( cat $i | jq -r 'select(.NAME != "<all>" and .PERMISSIONS != [] and .PERMISSIONS != null) | ( .NAME+":", "  "+.PERMISSIONS[] )'); if [ \! -z "$output" ]; then echo " ====== $i ====="; echo "$output"; fi; done
```

Finding user groups that are not the regular ``-tenant`` or ``-server`` groups:

```
for i in *_USER_GROUPS.json; do output=$( cat $i | jq -r 'select(.NAME | (test("-tenant$") or test("-server$")) | not).NAME' ); if [ \! -z "$output" ]; then echo " ${i} "; echo "$output"; fi; done
```

Finding user groups with more than one user in it:

```
for i in *_USER_GROUPS.json; do output=$( cat $i | jq -r 'select(.NAME | (test("-tenant$") or test("-server$")) | not) | select((.USERNAMES | length) > 1) | ("  "+.NAME, .USERNAMES[])' ); if [ \! -z "$output" ]; then echo " ===== ${i} ====="; echo "$output"; fi; done
```

Finding non-distinct users after having stored all sorted user names including duplicates in ``/tmp/all_users_name.json`` and all uniquely sorted user names in ``/tmp/unique_users_name.json``:

```
diff /tmp/all_users_name.json  /tmp/unique_users_name.json  | grep "^[<>]" | sed -e 's/^[<>] "//' -e 's/"$//' | sort -u | wc
   1856    1936   16642
```

Under the same premise regarding the input files,

```
diff /tmp/all_users_name.json  /tmp/unique_users_name.json  | grep "^[<>]" | sed -e 's/^[<>] "//' -e 's/"$//' | sort -u | while read i; do echo $i; cat *_USERS.json | jq .NAME | grep -c $i; done
```
lists all users with the number of their occurrences

List all databases used by a multi-instance server:

```
for i in */env.sh; do cat $i | grep "\(MONGODB_URI\)\|\(MONGODB_NAME\)" |  grep -v "^#" | sed -e 's/MONGODB_NAME=\(.*\)$/\1/' -e 's/MONGODB_URI=.*\/\([^?]*\)\?.*$/\1/' | tail -n 1; done | while read n; do echo -n "$n "; done
```

## Strategy and Approach for Merging the Collections

As the starting point we should use the ``winddb`` ARCHIVE server's MongoDB collections ``USERS``, ``PREFERENCES``, and ``USER_GROUPS``. We will ignore all databases from inactive servers, including all content archived during the end of the 2019 season. Further collections to consider are the ``ACCESS_CONTROL_LIST`` and ``OWNERSHIPS`` collections.

Remaining to be added to the ``winddb`` content are the same collections from all active, non-isolated servers (see above).

### Preparing Permissions and Roles Regarding their Qualifications

Role assignments have been migrated during the introduction of the ``permission-vertical`` branch to the master branch. Only an unqualified ``admin`` role may exist for a single ``admin`` user in the archive. All unqualified roles will be ignored during import/merge.

Before merging a permission into the ``PERMISSIONS`` array of the ``USERS`` collection we have to ensure proper qualification. The ``SERVER:DATAMINING`` permission must be constrained to the server name from where the record is being merged. This can easiest be established by stepping through the permissions granted on the active servers to be merged. Only 49er, HNV, my, NOR, sailracer, SRV and VSAW have such records, and I'll qualify or remove any unqualified permissions. (Side note: currently, due to bug 5156 it is not possible to remove those erroneous permission assignments. However, a fix is underway with commit 841893528be759d11f776bb1c437e61ffc66dccf and as soon as a release becomes available we can upgrade the servers that require it.)

Remaining permissions to clean up:

```
 ====== HNV =====
Johannes.Hellmich:
  admin
 ====== NOR =====
saillogic:
  manage_device_configuration:*
  leaderboard:update:*
 ====== sailracer =====
Kai:
  admin
 ====== SRV =====
Max:
  Admin
 ====== VSAW =====
peter:
  MANAGE_DEVICE_CONFIGURATION
```

Cleaned up permissions as of 2019-11-15T23:50:00Z.

### Algorithm for Merging

We assume that no relevant additions have been made anywhere to the ``ROLES`` collection, so we don't bother copying or merging it.

Merging the ``USER_GROUPS``, ``USERS``, ``PREFERENCES``, ``OWNERSHIPS`` and ``ACCESS_CONTROL_LISTS`` collections has several interdependencies and therefore needs to take place in several phases. User groups reference the users they contain. Users have a default creation group. Users can refer to Role objects which in turn can (and should) refer to groups and users, qualifying the object set for which they imply their permissions based on who owns those objects.

#### Pass 1: Marking Objects for Adding, Merging or Dropping

In the first pass the decision is made for each object of type ``User``, ``UserGroup``, ``Ownership``, ``AccessControlList`` and the user preferences whether it will be added, merged or dropped. Warnings will be issued for objects dropped.

Groups will be added if none with the same UUID or an equal name exists on the importing side. Should a user group with the same ID exist, it will be marked for merge. If a user group exists on the importing side that has an equal name but a different ID, the groups will only be marked for merge if their equal names have the format``"&lt;username&gt;-tenant"`` and a user named ``&lt;username&gt;`` exists in both groups and the user in the imported group is merged with the corresponding user in the importing group. Otherwise, the source group will be marked for dropping.

Users with a name for which no user exists in the target will be marked for "add". If a target user exists with a name equal to that of the source user the source user will be marked for merge into the target user if the e-mail addresses are equal. Otherwise, the source user is marked for dropping. For objects marked for merging, the merge target is recorded for each merge source.

Ownerships can only be marked for add or drop. They are added if no ownership exists in the target for the object ID yet and group or user owner will exist in the target (either added or merged); they are dropped otherwise, or if all owner references (group and/or user) point to objects marked for drop.

Access control lists (ACLs) for object IDs for which no ACL exists in the target are marked for add. ACLs for object IDs for which an ACL exists already in the target are marked for merge. ACLs for which all groups used by it are marked for drop will be marked for drop.

Preferences are marked for add if no preference object exists in the target for the user and the user is marked for add or merge. They are marked for merge if a preference object exists for a user by the same name in the target. They are marked for drop if the user is marked for drop.

#### Pass 2: Update Source Objects Based on Pass 1 Marking Results

All references from objects marked as "add" or "merge" pointing to objects in the source store marked as "merge" are updated to point to the corresponding merged objects in the target store. (Objects referenced that were also marked "add" will be moved to the target store, so references pointing to them don't need to be updated.) All references that point to objects marked as "drop" are removed, also removing as much context as necessary for consistency. For example, if an ACL grants permissions for a group marked as "drop" then that part of the ACL including the permissions granted/denied are removed. Similarly, if a user's role refers to a user or group to qualify the role and the user/role are marked for "drop" then the role has to be removed from the user.

The types whose objects are scanned for such references are:
* ``User``: check role qualifications (group/user) and remove roles for which any qualifying object is marked as "drop"; check default creation groups
* ``UserGroup``: check users that are members of that group; note that the role definitions the group has don't need a check as the definition itself has no qualifications
* ``Ownership``: check user and group owner; should an ownership lose both, its group and user owner, the ownership will not be imported
* ``AccessControlList``: check groups; if a group is dropped and a permission was denied for that group, throw an exception because this would grow permissions; otherwise drop the permissions granted for the group dropped
* ``Preferences``: check user

#### Pass 3: Adding and Merging Objects

All objects marked to be added are copied from their source store to the target store. For those marked as "merge" the target object is read from the mapping and the merge is performed according to the following rules.

For ``UserGroup`` objects the set of role definitions will be merged, and the set of users will be merged. When merging the role definitions, if source and target group refer to the same RoleDefinition (equal IDs) and one role is granted for all users, the other only for members of the group, the merge result will grant the role for all users.

For ``User`` objects the roles are merged in a "set" logic, avoiding duplicates. Comparison includes the group/user qualifications. Unqualified roles from the source object are ignored. The same applies for the permissions. Proper permission qualification particularly for ``SERVER`` operations needs to be ensured in a preparatory step. The default creation groups are merged by server name to which they apply. If both objects have a default creation group for the same server name, the importing side "wins." The e-mail validation status is set to ``true`` if at least one of the two objects has a validated e-mail address. Furthermore, a role referenced in the user's roles will only be merged if the role's ID exists in the target's role definitions  (which should be the case by the assumption above that no extensions were made anywhere to the ``ROLES`` collection). According to the research above, only ``user``, ``admin`` and ``moderator`` assignments seem to exist and those are pre-defined roles that will exist in the target with the same IDs. During a merge we won't touch the account and password information. The importing side's account data remains unchanged. Users affected by a merge will therefore end up with the username/password combination they have on the ARCHIVE server. Should that be different and should they have forgotten, they can still recover the password. If the importing side lacks optional field values for the User object for which valid values exist on the imported side, the valid values are used (company, full name, locale).

The ``UserStore.getPreference(...)`` data can be merged based on user name for all users in the source store. If a preference key for a user already exists in the importing (target) side, the target wins, and the source key's value is dropped, and the situation is logged.

Merging two ``AccessControlList`` objects considers the groups and the permissions granted to and denied from each respective group. When there are entries for the same group on both sides, combine the permissions granted, and combine the permissions denied.

## Open Issues

Should we implement a migration of Local Storage properties? If a server is configured to use a cross-domain storage and under the application origin's local storage there exist one or more properties, should we make an effort to copy those into the shared cross-domain storage? How to resolve conflicts there?

## The Actual Merging Process

There is a new server instance ``security-service.sapsailing.com`` with MongoDB ``mongodb://mongo0.internal.sapsailing.com,mongo1.internal.sapsailing.com/security_service?replicaSet=live&retryWrites=true``. The initial contents of the DB were the copies of the security-related collections as taken from the ``ARCHIVE`` server, with DB connectivity string ``mongodb://dbserver.internal.sapsailing.com:10201/winddb?replicaSet=archive&retryWrites=true``. This process is encoded in the script ``dbserver.internal.sapsailing.com:/var/lib/mongodb/security_service/copyarchivestore.sh``. A copy of this script can also be found in git under ``configuration/``. It uses the scripts ``export_security.sh`` and ``import_security.sh`` which are found in the same directory, respectively.

An exported JAR file based on a launch configuration (``MergeSecurityAndAccessControlStores.launch``) for the ``SecurityStoreMerger`` class has been placed under ``dbserver.internal.sapsailing.com:/var/lib/mongodb/security_service``. To merge, e.g., the ``VSAW`` database, use the following command:

```
      java -Dmongo.uri="mongodb://localhost:10203/security_service?replicaSet=live&retryWrites=true" -Ddefault.tenant.name=ARCHIVE-server -jar SecurityStoreMerger.jar "mongodb://localhost:10203/VSAW?replicaSet=live&retryWrites=true" VSAW-server 2>&1 | tee storemerge_VSAW.log      
```

The log output is stored in the ``storemerge_VSAW.log`` file which may contain WARNINGs or even Exceptions. Search for those in the log and do some plausibility checks. You may also restart the security-service.sapsailing.com instance to use its AdminConsole for checking the results and comparing with the server from which the security-related information was imported.

Then, repeat for more server DBs that you'd like to import. This may be scripted, at least for all DBs from the ``live`` replica set, as follows:

```
        for DB in VSAW SEASCAPE ...; do
                java -Dmongo.uri="mongodb://localhost:10203/security_service?replicaSet=live&retryWrites=true" -Ddefault.tenant.name=ARCHIVE-server -jar SecurityStoreMerger.jar "mongodb://localhost:10203/${DB}?replicaSet=live&retryWrites=true" ${DB}-server 2>&1 | tee storemerge_${DB}.log
        done
```

Likewise, those DBs that are not hosted on the ``live`` MongoDB replica set but on the MongoDB "archive" instance running on port 10202, the following would be the script:

```
        for DB in SSC SRV ...; do
                java -Dmongo.uri="mongodb://localhost:10203/security_service?replicaSet=live&retryWrites=true" -Ddefault.tenant.name=ARCHIVE-server -jar SecurityStoreMerger.jar "mongodb://localhost:10202/${DB}" ${DB}-server 2>&1 | tee storemerge_${DB}.log
        done
```
There are three files describing the DBs in question:
* ``list-of-archive-dbs`` has those DBs available on ``dbserver.internal.sapsailing.com:10202``
* ``list-of-live-dbs`` has those DBs available on ``mongodb://localhost:10203/security_service?replicaSet=live&retryWrites=true``
* ``list-of-dbs-to-merge`` has the list of DBs we want to merge, taken from the list at the beginning of this document

We now need to find each element from ``list-of-dbs-to-merge`` first in ``list-of-live-dbs`` and if it's not there, then in ``list-of-archive-dbs``. If a DB to merge cannot be found in either one then that's an error. There is the script ``dbserver.internal.sapsailing.com:/var/lib/mongodb/security_service/mergedbs.sh`` which enumerates all DBs to merge, looks them up in the archive and live DB lists and based on that decides which merge command to use. All logs wil be written to ``dbserver.internal.sapsailing.com:/var/lib/mongodb/security_service/storemerge_....log``. This script can be executed after the initial filling of the security_service DB that was done by the ``copyarchivestore.sh`` command.

So the total sequence of commands for running the entire import/merge process is:

```
        ssh -A root@dbserver.internal.sapsailing.com
        cd /var/lib/mongodb/security_service
        ./copyarchivestore.sh
        ./mergedbs.sh
```
This produces all ``storemerge_....log`` files and a merge result in the ``mongodb://localhost:10203/security_service?replicaSet=live&retryWrites=true`` database. Again, search for WARNING and Exception in the logs, understand dropped objects and do some spot checks. Also validate by launching the ``security-service.sapsailing.com`` server and inspect security-related objects in the AdminConsole.

### Updating the server instances

The first step is to restart the security-service.sapsailing.com instance based on the new DB. It uses the following configuration options in its env.sh to expose the shared services:

```
ADDITIONAL_JAVA_ARGS=... -Dsecurity.sharedAcrossSubdomainsOf=sapsailing.com -Dsecurity.baseUrlForCrossDomainStorage=https://security-service.sapsailing.com -Dgwt.acceptableCrossDomainStorageRequestOriginRegexp=https?://(.*\.)?sapsailing\.com(:[0-9]*)?$
REPLICATION_CHANNEL=security_service
```

A new user ``security-service-replicator`` has been added that has replication permissions for the ``security-service`` server. Other servers need to obtain an access token for that user and add it in their startup options as follows:

```
ADDITIONAL_JAVA_ARGS="$ADDITIONAL_JAVA_ARGS -Dsecurity.sharedAcrossSubdomainsOf=sapsailing.com -Dsecurity.baseUrlForCrossDomainStorage=https://security-service.sapsailing.com -Dgwt.acceptableCrossDomainStorageRequestOriginRegexp=https?://(.*\.)?sapsailing\.com(:[0-9]*)?$"
REPLICATE_ON_START=com.sap.sse.security.impl.SecurityServiceImpl,com.sap.sailing.shared.server.impl.SharedSailingDataImpl
REPLICATE_MASTER_SERVLET_HOST=security-service.sapsailing.com
REPLICATE_MASTER_SERVLET_PORT=443
REPLICATE_MASTER_EXCHANGE_NAME=security_service
REPLICATE_MASTER_BEARER_TOKEN="Gecx+W/dwFKRAxFbIvC/IMafEnJ8kTQF+MlYNVhEwD4="
```

These properties have now been appended to the env.sh files of the existing servers on the multi-instance set-up (including swisstimingtest, sailtracks, and tractractest). 