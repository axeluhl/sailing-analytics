This page descrips how to manage the media content like images and videos for the events on www.sapsailing.com, providing a guidance based on user's best experiences. For information about where to get media content itself, please refer to the [[media|wiki/events/Sailing-events-media-content]] page.

[[_TOC_]]

# Images

## Image types and their impact
There are four image types which can be added to an event where they carry different roles:

* **Logo:** the sailing event or league logo which gets displayed e.g. in the upper left corner next to the name of the sailing event.
* **Stage:** used as a banner for the sailing event and gets displayed e.g. on the landing page.
* **Teaser:** used to give a preview about the sailing event and gets displayed e.g. in the event overview where all events are grouped by years.    
* **Gallery:** representing the images which are displayed under the _media_ tab of each event.

## Scale guidelines for several purposes
For performance reasons, images should be scaled before uploading them. The following image resolutions showed up as good manner:

* **logo.png:** about 150 x 150 pixel (file size < 100 kB)
* **teaser.jpeg:** about 640 x 480 pixel (file size < 100 kB)
* **stage.jpeg:** about 1800 x 500 pixel (file size < 500 kB)
* **gallery.jpeg:** about 1400 x 1000 pixel (file size < 350 kB)  

Note: the _logo_ requires the .png file format with transparent background. _Teaser_, _stage_ and _gallery_ images require the .jpg (.jpeg) file format.

## S3 folder structure
Before uploading the images an appropirate folder needs to be created at the S3 storage. For a good contribution please stick to the following conventions:     

Access the S3 folder of the corresponding event year. Create an event root folder and set its name to the name of the league or series of events (see **figure 1**).  
![An exemplary event overview for 2017](https://s3-eu-west-1.amazonaws.com/media.sapsailing.com/wiki/how%20to/media%20content/year-and-event.jpeg)  
**Figure 1: An exemplary event overview for 2017**   

Within this folder add a new subfolder, set its name to the event's name and add a preceding number which indicates the order of it (see **figure 2**).  
![folder structure](https://s3-eu-west-1.amazonaws.com/media.sapsailing.com/wiki/how%20to/media%20content/folder-structure.jpeg)  
**Figure 2: folder structure with preceding event numbers within an event series**  

According to this description result in following folder structure:  
**pattern:** `~`/`event year`/`series name`/`event name with preceding number`/    
e.g. `~`/`2017`/`1. Segel-Bundesliga`/`01 Prien am Chiemsee`/  

Access the corresponding event folder and create two subfolders: _Images\_Homepage_ and _Images\_Photos_. The first one is for the _stage_, _teaser_ and _logo_ images. The second one will carry _gallery_ images.    
   
**pattern:** `~`/`event year`/`series name`/`event name with preceding number`/`Images_Homepage`/  
e.g. `2017`/`1. Segel-Bundesliga`/`01 Prien am Chiemsee`/`Images_Homepage`/  

**pattern:** `~`/`event year`/`series name`/`event name with preceding number`/`Images_Photos`/    
e.g. `2017`/`1. Segel-Bundesliga`/`01 Prien am Chiemsee`/`Images_Photos`/

## Image manipulation with GIMP
The following guide is based on the tool [GIMP](https://www.gimp.org/) and its [plugin](http://registry.gimp.org/node/26259) for batch manipulation.  

1. Start up GIMP go to File -> Batch Image Manipulation
2. Add -> Resize -> select the option _set exact size in pixel_ and _preserve aspect ratio_ -> insert the desired width & height -> Ok
3. Add -> Add a watermark -> leave defaults -> insert copyright information -> Ok
4. Add images -> ... -> Apply

## Exemplary instructions
1. Access the administration console _eventname(-master?)_.sapsailing.com/gwt/AdminConsle.html and login with your user credentials.
2. Select the corresponding event and click at the _edit_ button (see **figure 3**)  
![Events overview on admin console](https://s3-eu-west-1.amazonaws.com/media.sapsailing.com/wiki/how%20to/media%20content/events-overview-admin-console.JPG)  
**Figure 3: the edit button on the admin console**
3. By selecting the _Images_ tab the possibilities to add the four image types like described [above](https://wiki.sapsailing.com/preview#images_image-types-and-their-impact) show up (see **figure 4**).
![The four image types](https://s3-eu-west-1.amazonaws.com/media.sapsailing.com/wiki/how%20to/media%20content/edit-media-content.JPG)  
**Figure 4: the four image types**
4. Click on the desired _add `image type` button_ and insert the _Image URL_ according to the memory location of the image. Usually, no further settings are required. 

# Videos
A guidance about how to synchronize races with livestream videos can be found [here](https://wiki.sapsailing.com/wiki/howto/eventmanagers/linking-race-videos).

## Video types and their impact
We distinguish between _highlight_, _livestream_ and _gallery_ videos and a special _featured_ option.

* **Highlight:** most event organizers provide highlight videos of their sailing events which summarize the best moments of the event. Such videos belong to the _highlight_ category.  
* **Livestream:** some event organizers record livestreams of the races during the event. This videos belong to the _livestream_ category and can be linked to the _Race Analytics_ solution.
* **Gallery:** Some times interviews with sailors or other insights of the sailing event are provided by the organizers. These videos belong to the _gallery_ category.
* **Featured:** is a special option for an event video. This tag can be additionally added to one of the three categories _highlight_, _livestream_ and _gallery_. A video with the _featured_ tag is presented at the event's page instead of the _stage image_.  
Note: Always define the most populare video to be _featured_ e.g. the current livestream during the event. Never define more than _one_ featured video.

## Exemplary instructions
1. Access the administration console _eventname(-master?)_.sapsailing.com/gwt/AdminConsle.html and login with your user credentials.

2. Select the corresponding event and click at the _edit_ button (see **figure 5**).    
![Events overview on admin console](https://s3-eu-west-1.amazonaws.com/media.sapsailing.com/wiki/how%20to/media%20content/events-overview-admin-console.JPG)  
**Figure 5: the edit button on the admin console**

3. By selecting the _Videos_ tab the possibilities to add the three video types like described [above](https://wiki.sapsailing.com/wiki/howto/eventmanagers/Manage-media-content#videos_video-types-and-their-impact) show up (see **figure 6**).  
![Figure 6: the three video types](https://s3-eu-west-1.amazonaws.com/media.sapsailing.com/wiki/how%20to/media%20content/edit-media-content-video.JPG)  
**Figure 6: the three video types**

4. Click on the desired _add_ `video type` _button_ and set the _Video URL_, the _Mime Type_, the _Title_ and the _Laguage_ of the video (see **figure 7**).     
![insert a video](https://s3-eu-west-1.amazonaws.com/media.sapsailing.com/wiki/how%20to/media%20content/add-video.JPG)  
**Figure 7: link a video with _Video URL_, _Mime Type_, _Title_ and _Language_**

5. In the case you want to set a _featured_ video, select the _Enter tags for the video_ input field and enter _Featured_ (a small popup supports your input). When you're done, you'll need to click the _Add button_ next to the text box that attaches this tag to the video (see **figure 8**).  
![Add a featured video](https://s3-eu-west-1.amazonaws.com/media.sapsailing.com/wiki/how%20to/media%20content/add-featured-video.JPG)  
**Figure 8: add a featured video**

# Competitor logos
The competitors of the sailing events can be equipped with their club flags. The following steps provide an exemplary guidance about how to add a club flag to its corresponding competitor. All available clubflags are located at the S3 storage under the folder `~`/`clubflags`/.

## Required file format 
* **File format:** .png with transparent background
* **File resolution:** about 100 x 50 pixel (file size < 25 kB)
* **Storage location:** `~`/`clubflags`/`country`/  
e.g. `~`/`clubflags`/`germany`/`clubflag1.png`

## Exemplary instructions
1. Access the administration console _eventname(-master?)_.sapsailing.com/gwt/AdminConsle.html and login with your user credentials.

2. Select _Tracked races_ and click on the _Competitors_ tab (see **figure 9**).  

3. Select the competitor, click on the _Edit_ button (see **figure 9**) and insert the Flag image URL of the corresponding club flag.   
![Select the competitors tab within the admin console](https://s3-eu-west-1.amazonaws.com/media.sapsailing.com/wiki/how%20to/media%20content/clubflag-selection.JPG)  
**Figure 9: competitors tab of the admin console**


