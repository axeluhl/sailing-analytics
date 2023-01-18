# Downloading and Archiving TracTrac Events

Usually we obtain the TracTrac data straight from the TracTrac site using their API ("TracAPI"). The API is initialized using a URL that points to a specific event, such as `https://event.tractrac.com/events/event_20210826_FinnishSai/jsonservice.php` which points to an event of the Finnish sailing league of 2021.

Each time our archive server re-starts it fetches the content from these sites. This has two disadvantages:

- The TracTrac site keeps seeing massive traffic each time we launch an archive server
- We depend on TracTrac's availability when starting / loading our archive

The benefit in loading straight from the TracTrac side is certainly that any improvements in quality, such as filtering outliers or updating mark passings, will reach us upon the next archive server re-start. Likewise, should TracTrac ever change anything about the format of the `params_url` files or the binary `.mtb` archives holding a race worth of data and publish a corresponding new TracAPI release, working off an archived copy of the data may break.

Yet, we would like to keep a backup copy of the data around and hence need some process for how to create such a backup copy. See also [bug 5801](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=5801).

## Creating a Backup Copy of a Single Event

TracTrac events are usually identified by the event's DB name, such as `event_20220626_KielerWoch` for the 2022 Kieler Woche event. Together with the TracTrac server hosting the event, this makes for the URL pointing to the entry point for the event: `https://event.tractrac.com/events/event_20220626_KielerWoch/jsonservice.php`.

In our Git repository's `configuration/` folder we have a script `downloadTracTracEvent` that can be called like this, assuming the shell is located in the Git workspace's root folder:

```
  configuration/downloadTracTracEvent {TracTracURL} {TargetDirectory}
```

For example:

```
  configuration/downloadTracTracEvent https://event.tractrac.com/events/event_20220626_KielerWoch/jsonservice.php /home/trac/static/TracTracTracks
```

This will produce a folder `/home/trac/static/TracTracTracks/event_20220626_KielerWoch` that holds a file `event_20220626_KielerWoch.json`, the URL to which then serves as an entry point for loading the races of that event.

The `sapsailing.com:/home/trac/static` folder (a link to `sapsailing.com:/var/www/static`) is exposed as `https://static.sapsailing.com`. Hence, the example above leads to a JSON URL that can be used with the SAP Sailing Analytics as follows: `https://static.sapsailing.com/TracTracTracks/event_20220626_KielerWoch/event_20220626_KielerWoch.json`.

## Archiving Several Events at Once

Assume you have a file with one TracTrac event JSON URL per line, called `tractrac-json-urls`. You can then download a copy of the data of all these events as follows:

```
  for i in `cat tractrac-json-urls`; do
    configuration/downloadTracTracEvent $i /home/trac/static/TracTracTracks
  done
```

This will obtain copies of all events listed in the `tractrac-json-urls` file. Such a file is provided in Git at `configuration/tractrac-json-urls` and may be updated with new events every once in a while.

To obtain a list of such JSON URLs from a SAP Sailing Analytics server's MongoDB, the following command can be used:

```
  mongo --quiet "{mongodb-url}" --eval 'db.TRACTRAC_CONFIGURATIONS.find({}, {TT_CONFIG_JSON_URL : 1}).toArray()' | grep -v ObjectId | jq -r '.[].TT_CONFIG_JSON_URL' | sort -u
```

For example, within the production landscape of sapsailing.com, you could try this to obtain all TracTrac URLs of all events contained in the ARCHIVE server:

```
  mongo --quiet "mongodb://dbserver.internal.sapsailing.com:10201/winddb?replicaSet=archive" --eval 'db.TRACTRAC_CONFIGURATIONS.find({}, {TT_CONFIG_JSON_URL : 1}).toArray()' | grep -v ObjectId | jq -r '.[].TT_CONFIG_JSON_URL' | sort -u
```

## Automation

For `wiki@sapsailing.com` the process of updating the `configuration/tractrac-json-urls` file from the ARCHIVE server is automated by means of two cron jobs as specified in `wiki@sapsailing.com:crontab`:

```
10 12 * * * /home/wiki/gitwiki/configuration/update-tractrac-urls-to-archive.sh >/home/wiki/update-tractrac-urls-to-archive.out 2>/home/wiki/update-tractrac-urls-to-archive.err
15 12 * * * /home/wiki/gitwiki/configuration/downloadNewArchivedTracTracEvents.sh /home/trac/static/TracTracTracks >/home/wiki/downloadNewArchivedTracTracEvents.out 2>/home/wiki/downloadNewArchivedTracTracEvents.err
```

Every day the first job calls the `configuration/update-tractrac-urls-to-archive.sh` script which fetches those URLs from the `mongodb://dbserver.internal.sapsailing.com:10201/winddb?replicaSet=archive` database and if changed commits it to the Git repository. The second job calls `configuration/downloadNewArchivedTracTracEvents.sh` which fetches only those events that do not have a representation yet in `/home/trac/static/TracTracTracks`.

With this, `/home/trac/static/TracTracTracks` will always have a representation of all archived events as a backup copy from which we may be able to load the data if the TracTrac content becomes unavailable for whatever reason.

## Background, Details

The `downloadTracTracEvent` script creates the sub-folder in the target folder specified, based on the event's database name, such as `event_20220626_KielerWoch` in the example above, and within that the sub-folder listed by the `stored-uri` property in the `params_url` properties, typically `datafiles/`. Based on the contents of the JSON file to which URL provided as the first argument to the `downloadTracTracEvent` script points, the `params_url` file (usually in `.txt` format) as well as the file referenced by the `stored-uri` property inside the `params_url` file are downloaded.

The JSON file is stored in the event-specific sub-folder of the target folder, named after the event database, e.g., `event_20220626_KielerWoch.json`. It is patched such that the `params_url` properties all use relative "URLs" (just as the base filename of the `.txt` file) which resolve to the race's `.txt` file when interpreted in the context of the URL pointing to the event's JSON file.

With this, the JSON file can be used to load the races from the backup copies.