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