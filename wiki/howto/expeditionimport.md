# Importing Sessions from Expedition

The SAP Sailing Analytics support importing data coming from the [Expedition](http://www.expeditionmarine.com/about.htm) tool. While there is also live connectivity that can be used to feed Expedition data streamed live through UDP, it is also possible to import log files in CSV format after a session.

Three types of data can be imported:

* Wind data
* GPS tracks
* Additional boat sensor data (heel, pitch, ...)

While in "manual" mode these can all be imported separately, into an event/regatta/race structure set up by the user beforehand, there is also an automatic mode that imports all data available and understood from an entire Expedition log file. The remainder of this document will cover this all-in-one automatic import functionality. For the interested user suffice it to say that the wind data import is available under the AdminConsole tab "Tracked races / Wind" in the compartment "Import Wind from Expedition". The GPS tracks can be imported going to "Connectors / Smartphone tracking" and then clicking the grey phone-shaped action icon entitles "Map Devices to Competitors and Marks", then in the popup dialog clicking "Import Fixes" and selecting "Expedition" as the file type, then choosing the log file from the local file system, pressing the "Import Fixes" button and mapping the GPS track(s) found in the log file to the regatta competitor to which it belongs. Similarly, the additional sensor data is reachable by instead of "Import Fixes" pressing the "Import additional sensor data" button and selecting "EXPEDITION_EXTENDED" for the type of data to import.

## All-in-One Import of an Expedition Log

In the AdminConsole go to "Tracked races / Wind" and scroll down to the "Import full Expedition data" compartment. Choose the log file you'd like to import. It can be the ``.csv`` file or a compressed version of it, e.g., ``something.zip`` containing one or more ``.csv`` files or ``something.csv.gz`` in GZIP compression format.

There are 3 different modes for the upload you can choose of:
* Create New Event: You need to choose this mode for your first session or whenever you need an empty setup with only the new data. You need to choose the boat class to which the log pertains. During import, this mode will create an event named after the name of the log file with the time stamp of the upload appended, e.g., ``2017Dec15_2018-01-22T13:39:48.411``, also a leaderboard group of that name, a "regatta", a regatta leaderboard and a "race" within that regatta, representing the session to which the log belongs. Most of these names can be edited afterwards. It's just intended to give you a quick start.
* Add a new Competitor: You need to choose this mode in case you want to add the log of another competitor to your already imported tracks. In this case you need to select the regatta you intend to import the new track to. It is expected that the track files for several competitors cover approximately the same time range. The start and end times for generated races are automatically determined for the first uploaded log file. If you need to extend that time range afterwards, you can do that under "Connectors > Smartphone Tracking" by clicking the "Set tracking times" button for a race.
* Create a new race: You need to choose this mode to import a new distinct session (e.g. new day of trainig) into an existing regatta. In this case you need to select the regatta you intend to import the new track to. The name of the generated leaderboard column and race is determined from the uploaded file name. If the same name is already used in the given regatta, a unique timestamp is appended to ensure uniqueness of race names.

When uploading an expedition file using any of the described cases, a spinning animation will appear next to the upload button which indicates that the upload is running. This may take a while (some minutes) to finish depending on the size of the uploaded file.

A popup should appear after a few minutes that lets you add or choose a competitor to which the data shall be associated. Either pick an existing competitor from the pool on the right by selecting one and pressing the little angular bracket pointing left ("<") or click the "Add" button at the top of the dialog to create a new competitor which is then added automatically. After adding any participating competitor, select exactly one entry in the left table to specify the competitor to whom the just uploaded log file will be associated. Press the "Save" button to accept the addition of competitors and mapping of the log file to the single selected one.

After another short while another little popup dialog should be shown that offers two links: one to the overview page of the event just created, the other one navigating straight to the "race" viewer for the data just imported. As it is possible that the session recorded does not have a valid "race" start/finish date and no course defined, the session may not be listed in the event's "Races" tab under the "List Format." In this case choose "Competition Format" and your session should show up as a greyish "Planned" icon that you may click to enter the viewer (RaceBoard.html). This is where the race link from the popup will take you straight away.

You can now start working on more detailed configurations, such as editing the course, setting timing parameters such as start/finish times if they weren't recognized from the log files, and much more. See [here](https://static.sapsailing.com/SAPSailingAnalytics_Administrator_Training.pptx) for the full documentation on what's possible.

## Adding Video

The general procedure for linking a video to a race is explained [[here|wiki/howto/eventmanagers/linking-race-videos]]. The explanations there assume that you have a video available from YouTube. Things will work almost the same for other online videos available as an ``.mp4`` stream or download, panoramic (360°) or regular.

### Local Video (no Upload Required)

First of all, you'll need a web server running locally which allows you to make a directory on your local hard disk containing your video files accessible through HTTP. [[Here|wiki/howto/setup/webserver/nginx-webserver]] is a description of how to set this up, using a little web server that runs on all platforms.

### Uploading through AdminConsole

When you have a video that you would like to make available and you don't want to upload it to YouTube then there is also a way to upload it to the SAP Sailing Analytics. When your server has a file storage configured properly, you can go into the edit page of your event (AdminConsole's "Events" tab, then clicking that little pen/paper icon in the Actions column), go to the "Videos" tab in the event editing popup dialog and click on "Add gallery video." Use the "Choose File" and then the "Send" button to upload a file. After the upload completes the resulting URL will be inserted. You may choose or not choose to save the event with the video attached, but most importantly you can copy the URL to the dialog where you attach the video.