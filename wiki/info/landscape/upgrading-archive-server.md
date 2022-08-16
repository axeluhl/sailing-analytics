# Upgrading the Archive Server

[[_TOC_]]

For the archive server we currently use two EC2 instances: one as the production server, and a second one as a failover instance. The failover server is usually one version "behind" the production server which is meant to allow for recovering fast from regressions introduced accidentally by an upgrade. With startup cycles that take approximately 24h as of this writing it is no option to hope to recover by starting another instance "fast."

The mapping for which archive server to use is encoded in the central web server's Apache reverse proxy configuration under ``/etc/httpd/conf.d/000-macros.conf``.  At its head there is a configuration like this:

```
<Macro ArchiveRewrite>
# ARCHIVE, based on i3.2xlarge, 64GB RAM and 1.9TB swap
        Use Rewrite 172.31.26.254 8888
</Macro>
```

It defines the ``ArchiveRewrite`` macro which is then used by other macros such as ``Series-ARCHIVE`` and ``Event-ARCHIVE``, used then in ``/etc/httpd/conf.d/001-events.conf``.

The process of upgrading the archive server works in the following steps:
* Scale out the underlying ``archive`` MongoDB replica set
* Launch a new EC2 instance with the necessary archive server configuration for the new release you'd like to upgrade to
* Create a temporary mapping in ``/etc/httpd/conf.d/001-events.conf`` to make new server accessible before switching
* Wait for the new candidate server to have finished loading
* Compare contents to running archive
* Switch ``ArchiveRewrite`` macro to new instance
* Reload Apache httpd service
* Re-label EC2 instances and shut down old failover server

The following sections explain the steps in a bit more detail.

## Scale out the underlying ``archive`` MongoDB replica set

When not launching an archive server, the ``archive`` replica set runs on a small, inexpensive but slow instance ``dbserver.internal.sapsailing.com:10201`` with which launching a new archive server takes days and weeks. In order to accelerate loading all content, first two or three replicas need to be added. Either use the ``configuration/aws-automation/launch-mongodb-replica.sh`` script from your git workspace as many times as you want to have replicas:

```
   ./configuration/aws-automation/launch-mongodb-replica.sh -r archive -p dbserver.internal.sapsailing.com:10201 -P 0 -t i3.2xlarge -k {your-key-name}
```

Or use [https://security-service.sapsailing.com/gwt/AdminConsole.html?locale=en#LandscapeManagementPlace:](https://security-service.sapsailing.com/gwt/AdminConsole.html?locale=en#LandscapeManagementPlace:) and log on with your account, make sure you have an EC2 key registered, then select the ``archive`` replica set under "MongoDB Endpoints" and click on the "Scale in/out" action button, then choose the number of replicas you'd like to launch and confirm.

Once triggered, you can watch the instances start up in the AWS console under EC2 instances. The filling of those replicas may take a few hours; you may check progress by connecting to a host in the landscape with the ``mongo`` client installed and try something like this:

```
    $ mongo "mongodb://dbserver.internal.sapsailing.com:10201/?replicaSet=archive&retryWrites=true&readPreference=nearest"
    ...
    archive:PRIMARY> rs.status()
    archive:PRIMARY> quit()
```

This will output a JSON document listing the members of the ``archive`` replica set. You should see a ``stateStr`` attribute which usually is one of ``PRIMARY``, ``SECONDARY``, or ``STARTUP2``. Those ``STARTUP2`` instances are in the process of receiving an initial load from the replica set and are not yet ready to receive requests. If you'd like to minimize the actual start-up time of your archive server process later, wait until all replicas have reached the ``SECONDARY`` state. But it's no problem, either, to launch the new archive server before that, only it will take longer to launch, and probably the additional load on the replica set will make the replicas reach the ``SECONDARY`` state even later.

## Launch a new EC2 instance

Use an instance of type ``i3.2xlarge`` or larger. The most important thing is to pick an "i" instance type that has fast NVMe disks attached. Those will be configured automatically as swap space. You may select "Launch more like this" based on the current production archive server to start with. Use the following as the user data:

```
INSTALL_FROM_RELEASE={build-...}
USE_ENVIRONMENT=archive-server
REPLICATE_MASTER_BEARER_TOKEN="***"
```

Obtain the actual build version, e.g., from looking at [https://releases.sapsailing.com](https://releases.sapsailing.com), picking the newest ``build-...`` release. The Java heap size is currently set at 200GB in [https://releases.sapsailing.com/environments/archive-server](https://releases.sapsailing.com/environments/archive-server). You can adjust this by either adding a ``MEMORY=...`` assignment to your user data, overriding the value coming from the ``archive-server`` environment, or you may consider adjusting the ``archive-server`` environment for good by editing ``trac@sapsailing.com:releases/environments/archive-server``.

Launching the instance in our default availability zone (AZ) ``eu-west-1c`` may be preferable as it minimizes cross-AZ traffic since the central web server / reverse proxy is also located in that AZ. However, this is not absolutely necessary, and you may also consider it a good choice to launch the new candidate in an AZ explicitly different from the current production archive server's AZ in order to increase chances for high availability should an AZ die.

Tag the instance with ``Name=SL Archive (New Candidate)`` and ``sailing-analytics-server=ARCHIVE``. You may have inherited these tags (except with the wrong name) in case you used "Launch more like this" before.

## Create a temporary mapping in ``/etc/httpd/conf.d/001-events.conf`` to make new server accessible before switching

Grab the internal IP address of your freshly launched archive server candidate (something like 172.31.x.x) and ensure you have a line of the form

```
    Use Plain archive-candidate.sapsailing.com 172.31.35.213 8888
```

in the file ``root@sapsailing.com:/etc/httpd/conf.d/001-events.conf``, preferably towards the top of the file where it can be quickly found. Save the changes and check the configuration using the ``apachectl configtest`` command. It should give an output saying ``Syntax OK``. Only in this case reload the configuration by issuing the ``service httpd reload`` command as user ``root`. After this command has completed, you can watch your archive server candidate start up at [https://archive-candidate.sapsailing.com/gwt/status](https://archive-candidate.sapsailing.com/gwt/status).

## Wait for the new candidate server to have finished loading

At the top of the ``/gwt/status`` output you'll see the fields ``numberofracestorestore`` and the ``numberofracesrestored``. The ``/gwt/status`` health check will respond with a status 200 only after the ``numberofracesrestored`` has reached the ``numberofracestorestore``. But even after that the new archive server candidate will be busy for a few more hours. The ``numberofracesrestored`` count only tells you the loading of how many races has been *triggered*, not necessarily how many of those have already *completed* loading. Furthermore, even when all content has been loaded, calculations will keep going on for a few more hours, re-establishing information about wind estimation, mark passings, and maneuvers. You can follow the CPU load of the candidate server in the AWS EC2 console. Select your new instance and switch to the Monitoring tab, there watch for the "CPU Utilization" and wait for it to consistently drop from approximately 100% down to 0%. This indicates the end of the loading process, and you can now proceed to the next step.

## Compare contents to running archive

It is a good idea to ensure all races really have loaded successfully, and all wind data and GPS data is available again, just like in the current production archive server. For this purpose, the ``java/target/compareServers`` script exists in your git workspace. Call like this:

```
   java/target/compareServers -el https://www.sapsailing.com https://archive-candidate.sapsailing.com
```

This assumes, of course, that you have completed the step explained above for establishing HTTPS access to ``archive-candidate.sapsailing.com``.

The script will fetch leaderboard group by leaderboard group and compare their contents, race loading state, and presence or absence of tracking and wind data for all races reachable through those leaderboard groups. The names of the leaderboard groups will be printed to the standard output. If a difference is found, the script will exit, showing the difference on the console. You then have a chance to fix the problem by logging on to [https://archive-candidate.sapsailing.com/gwt/AdminConsole.html](https://archive-candidate.sapsailing.com/gwt/AdminConsole.html). Assuming you were able to fix the problem, continue the comparison by calling the ``compareServers`` script again, adding the ``-c`` option ("continue"), like this:

 ```
   java/target/compareServers -cel https://www.sapsailing.com https://archive-candidate.sapsailing.com
```

This will continue with the comparison starting with the leaderboard group where a difference was found in the previous run.

Should you encounter a situation that you think you cannot or don't want to fix, you have to edit the ``leaderboardgroups.old.sed`` and ``leaderboardgroups.new.sed`` files and remove the leaderboard group(s) you want to exclude from continued comparisons. You find the leaderboard group that produced the last difference usually at the top of the two files. Once saved, continue again with the ``-c`` option as shown above.

Once the script has completed successfully you may want to do a few spot checks on [https://archive-candidate.sapsailing.com](https://archive-candidate.sapsailing.com). Note, that when you navigate around, some links may throw you back to ``www.sapsailing.com``. There, you manually have to adjust your browser address bar to ``archive-candidate.sapsailing.com`` to continue looking at your new candidate archive server contents.

## Switch ``ArchiveRewrite`` macro to new instance and reload httpd

Once you're satisfied with the contents of your new candidate it's time to switch. Log on as ``root@sapsailing.com`` and adjust the ``/etc/httpd/conf.d/000-macros.conf`` file, providing the new internal server IP address in the ``ArchiveRewrite`` macro at the top of the file. Save and check the configuration again using ``apachectl configtest``. If the syntax is OK, reload the configuration with ``service httpd reload`` and inform the community by a corresponding JAM post, ideally providing the release to which you upgraded.

## Re-label EC2 instances and shut down old failover server

Finally, it's time to shut down the old failover instance which is now no longer needed. Go to the AWS EC2 console, look at the running instances and identify the one labeled "SL Archive (Failover)". In the "Instance Settings" disable the termination protection for this instance, then terminate.

Rename the "SL Archive" instance to "SL Archive (Failover)", then rename "SL Archive (New Candidate)" to "SL Archive".

Then, enable termination protection for your new "SL Archive" server if it isn't already enabled because you used "Launch more like this" to create the instance using the previous production archive server as a template.