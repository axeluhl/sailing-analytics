# Log File Compression

Servers have a logrotate configuration that ensures that during normal operation the httpd log files from ``/var/log/httpd`` are regularly copied to ``/var/log/old/<SERVER_NAME>/<SERVER_EXTERNAL_IP>``. Note that before terminating an instance, this step needs to be carried out manually one more time to ensure all latest logs are saved before the instance and its file systems go away.

The logrotate configuration asks for the logs to be compressed. However, due to ``/var/log/old`` residing on a separate file system partition, this usually does not work, unfortunately. It seems logrotate works with simple file handles or inodes when moving and trying to compress the log files, but when moving across file systems, the inodes and file handles don't survive.

After a while, ``//var/log/old`` fills up with uncompressed files. These files are picked up by our own log analysis tool ``unique-ips-per-referrer`` which keeps track of the files already analyzed in a file located at ``sapsailing.com:/var/log/old/cache/unique-ips-per-referrer/visited``. The file contains the paths/names of the log files already analyzed.

When manually compressing log files the file names will change, usually by appending the ``.gz`` suffix. Make sure that this file name change also happens in the ``visited`` file.

I typically use the following command to compress all log files uncompressed so far, based on the assumption that the majority of the log files ends with a number that represents part of the time stamp, or are named ``access_log`` in case they were the last file moved away manually without "rotating" the logs:

```cd /var/log/old; find . -type f | grep "\([0-9]$\)|\(access_log$\)" | while read i; do echo $i; gzip $i; done```

I then open the ``visited`` file using ``vi`` and then apply the following substitution command to append ``.gz`` to all files now compressed:

```:1,$s/\([0-9]\)$/\1\.gz/```

And then for the plain ``access_log`` files:

```:1,$s/access_log$/access_log.gz/```