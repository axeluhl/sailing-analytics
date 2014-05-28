# Amazon EC2 Backup Strategy

[[_TOC_]]

# Rationale

It is crucial that all data stored on various instances of our infrastructure is backup'd regularly so that one can recover in case of data loss or hardware failure. Each data store must be represented in the backup and it must be possible to recover all data at any time. This also holds for configurations that define how applications and instances run. 

With respect to the available admin resources for this project the aim of a backup solution is not find a way between perfection and chaos that keeps the amount of work to a minimum level without compromising the aim. One building block to achieve this is to make the setup of a backup for a data store (or whole instance) as easy as possible. That is being made possible by providing all administrators a simple backup script that can be configured in 5 minutes. Another building block is to define that backups are not accurate to the minute or even hour but are executed once every day. That means that you can loose at most 23 hours of data if hardware fails 23 hours after the last backup. Considering the non linear increasing amount of additional work required to build a solution that would give minute accuracy the current timeframe is deemed ok for the data we're dealing with.

The technology behind our backup solution is based on a customized use of GIT repositories that are located on a central backup server. Each client creates an index of all files to backup and then pushes them to that server. That approach guarantees that we have a full history for each file including diffs for plaintext and that space consumption is kept to a minimum by storing only the differences. In addition to that our tool will split and pack files to save space.

The central backup server is configured with enough space to hold a large amount of backup data. To be able to also recover data in case of a failure or data loss all repositories are sync'd to a S3 bucket. The following image depicts the current structure. On the left hand you see the instances involved. For each of these instances you see the directories or data stores being backup'd. In addition to that you can see the time each of the backups runs. On the right hand you see the structure of the backup server with separate git repositories for each instance.

<img src="/wiki/images/amazon/EC2BackupStrategy.jpg" width="100%" height="100%"/>

This image only depicts backup of data. For each instance there should be an AMI containing at least the system configuration and binaries. The process of creating these AMIs is a manual one and has no defined schedule.

## Technology

BUP is the tool that is being used to create the backups. You can access it's documentation and code here: https://github.com/bup/bup. If you look at it in detail then this is just a very sophisticated wrapper around GIT. Unlike git, it writes packfiles directly (instead of having a separate garbage collection / repacking stage) so it's fast even with gratuitously huge amounts of data. bup's improved index formats also allow you to track far more filenames than git (millions) and keep track of far more objects (hundreds or thousands of gigabytes). It uses a rolling checksum algorithm (similar to rsync) to split large files into chunks. The most useful result of this is you can backup huge virtual machine (VM) disk images, databases, and XML files incrementally, even though they're typically all in one huge file, and not use tons of disk space for multiple versions.

As with git you have three stages when starting a backup.

- First you need to initialize the repository (comparable to `git init`). This is also needed when using a remote repository. In that case the local repository is used to hold the index of files and to determine wether files have changed. The initialization is executed by invoking `bup [-d /path/to/repo] init`. In most cases you can just omit the directory - bup will then set it to $HOME/.bup.
- Second you need to tell bup to create or update the index (comparable to `git add`). That operation will list all files and add them to the index. That index contains information about changes to the tree structure. At this stage you can specify wether to ignore certain files (--exclude and --exclude-rx). An index command could look like this: `bup index /etc`.
- Third all indexed files need to be saved (comparable to `git commit`). That process can either store files locally (comparable to `git commit`) or push them to a remote repository (`git commit && git push`).

The last stage involves some magic because bup does not expose the notion of branches or HEAD to the user. In most cases one will save files by specifying the name of a backup (`bup save -n <name>`). Internally this will create or update a branch that matches the name given. More importantly this operation will remove all files not matching the index just created. 

Assuming that you save _/etc_ by specifying mybackup as the name of the backup. Then you save _/var/log_ by using the same backup name. That will lead to _/etc_ not being included into the commit on that branch because it does not match path _/var/log_. It will be recognized as deleted as /etc is no longer contained in the index. As a general rule keep in mind that you for each backup-name the index should be stable at least in terms of paths. 

Unfortunately there is no way of changing bup's behaviour. To make sure that one can still restore files on different branches being connected somehow one must provide a date parameter during save. This date parameter will then be used to distinguish the different backups. A typical backup run could then look like this:

<pre>
$ bup index
Reinitialized existing Git repository in /home/.bup/
$ bup add /etc
Indexing: 1188, done (2196 paths/s).
$ bup save --date=`date +%s` -n etc /etc
Reading index: 1188, done.
Saving: 100.00% (24426/24426k, 1188/1188 files), done.    
bloom: creating from 1 file (3635 objects). 
</pre>

To procedure to store a backup to a remote server involves just one additional parameter to save. Remember that building the index is still done locally. The remote server has to be reachable by SSH, have bup available and in most cases it makes sense to having setup ssh password-less authentication. A typical operation would look like this:

<pre>
$ bup index
Reinitialized existing Git repository in /home/.bup/
$ bup add /etc
Indexing: 1188, done (2196 paths/s).
$ bup save -r backup@123.254.254.0:/home/backup/gitrepository --date=`date +%s` -n etc /etc
Reading index: 1188, done.
Saving: 100.00% (24426/24426k, 1188/1188 files), done.    
bloom: creating from 1 file (3635 objects). 
</pre>

As bup is using the standard GIT repository layout one does not need to use bup to access files. But yuo have to keep in mind that as bup is splitting the files it would require some additional effort to put these files together again. The following chain shows how to access a repository. It assumes that one has logged into the backup server.

<pre>
[backup@ip-172-31-25-136 ~]$ git clone file:///home/backup/database dbtest
Initialized empty Git repository in /home/backup/dbtest/.git/
remote: Counting objects: 217784, done.
remote: Compressing objects: 100% (217147/217147), done.
remote: Total 217784 (delta 107), reused 217677 (delta 0)
Receiving objects: 100% (217784/217784), 444.86 MiB | 20.11 MiB/s, done.
Resolving deltas: 100% (107/107), done.
warning: remote HEAD refers to nonexistent ref, unable to checkout.
</pre>

The error message is ok and can be ignored because bup has not created any HEAD reference but only branches. Checking out the given branch makes the error go away.

<pre>
[backup@ip-172-31-25-136 ~]$ cd dbtest/
[backup@ip-172-31-25-136 dbtest]$ git checkout database--etc
Branch database--etc set up to track remote branch database--etc from origin.
Switched to a new branch 'database--etc'
[backup@ip-172-31-25-136 dbtest]$ ls -lah
total 20K
drwxr-xr-x.  4 backup backup 4.0K May 27 13:20 .
drwxr-xr-x. 10 backup backup 4.0K May 27 13:18 ..
-rw-rw-r--.  1 backup backup   98 May 27 13:20 .bupm
drwxrwxr-x. 77 backup backup 4.0K May 27 13:20 etc
drwxrwxr-x.  8 backup backup 4.0K May 27 13:20 .git
</pre>

# Backup

The first requirement to backup data from an instance is that it is running on CentOS. The backup script hasn't been tested on other systems. If you want to run it on other systems you most probably will need some effort to recompile bup and adapt some properties.

## Setup

- First you need to download the script and the required binaries. There is a template that can be downloaded from S3 by using the following URL: s3://backup-template/backup-template-<version>.tar.gz. That template contains everything needed to backup. It is a good idea to extract it to _/opt_. In addition to that one can find the script in the main GIT repository under _configuration/backup.sh_. You can download a version of bup here: https://github.com/bup/bup/releases. Be aware of the fact that you need to compile the whole thing.

- After having extracted or installed the script and binaries you need to adapt some settings. Make sure to at least configure PREFIX and BACKUP_DIRECTORIES. Check the documentation in the script - it should be self explaining. Then you could add the following entry to _/etc/crontab_: `0 22 * * * root /opt/backup.sh`. 

- Now make sure to copy the SSH key to the backup server by using `ssh-copy-id -i /root/.ssh/id_dsa backup@172.31.25.136`. The remote repositories are stored at _backup@172.31.25.136:/home/backup_. The password for the backup user is the same as for the trac user on the webserver. 

- To make sure everything is right you should execute the script once before having it automatically executed - just run `/opt/backup.sh`.

## Check

After having successfully run a first backup you can check that your backup has been stored correctly. Unfortunately listing the contents of your backup is not possible from the client. You need to log into the backup server with the user backup.

According to the PREFIX that you have configured the backup script will have created a new directory at /home/backup/PREFIX. The following example assumes that your prefix is build and that you have created a backup named dir-home-hudson-repo-jobs-SAPSailingAnalytics-master.

<pre>
[backup@ip-172-31-25-136 build]$ /opt/bup/bup -d /home/backup/build/ ls dir-home-hudson-repo-jobs-SAPSailingAnalytics-master
2014-05-27-170544  latest                                                                                                                                                                                                                              
</pre>

The output will contain all runs of the backup. There will always be a symbolic link that points to the latest backup. Using ls you can easily navigate through the tree structure.

<pre>
[backup@ip-172-31-25-136 build]$ /opt/bup/bup -d /home/backup/build/ ls dir-home-hudson-repo-jobs-SAPSailingAnalytics-master/latest/home/hudson/repo/jobs/SAPSailingAnalytics-master
builds           config.xml       lastStable       lastSuccessful   nextBuildNumber  scm-polling.log                                                                                                                                                           
</pre>

You can also display any text files by replacing `ls` by `cat-file`.

<pre>
[backup@ip-172-31-25-136 webserver]$ /opt/bup/bup -d /home/backup/webserver/ cat-file dir-home-scores/latest/home/scores/dummy.txt
this is dummy content to test the backup
</pre>

# Restore

Depending on what has crashed or where data got lost you need to look at different places to restore content and functionality.

## Amazon Ireland is not available / has crashed

Go on vacation for a week and mute your phone and emails. If situation has not recovered after your return then quit your job.

## One Instance has crashed and can not be recovered

Look out for an AMI that represents the system setup and binaries. If there is one then you can create a new instance from that AMI. Please keep in mind that not all AMIs contain all volumes needed for operation. Normally very large volumes containing databases and such are not persisted along with an AMI. In case of a recovery you most probably need to recreate these volumes and then restore data from backup.

## Volume has crashed or data has been lost
